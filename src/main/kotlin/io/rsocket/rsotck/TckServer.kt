package io.rsocket.rsotck

import io.airlift.airline.Command
import io.airlift.airline.Option
import io.airlift.airline.SingleCommand
import io.rsocket.RSocket
import io.rsocket.RSocketFactory
import io.rsocket.rsotck.TckServer.Companion.NAME
import io.rsocket.rsotck.controlapi.TCKMessage
import io.rsocket.rsotck.controlapi.api.Runner
import io.rsocket.rsotck.controlapi.api.SimpleResponse
import io.rsocket.rsotck.controlapi.runner.RunnerExecuteTest
import io.rsocket.rsotck.controlapi.test.TestCase
import io.rsocket.rsotck.controlapi.testsuite.SelfTestRequest
import io.rsocket.rsotck.controlapi.testsuite.TestSuiteResults
import io.rsocket.rsotck.controlapi.testsuite.TestSuiteSetup
import io.rsocket.rsotck.runner.RunnerConnection
import io.rsocket.rsotck.server.ServerRequestHandler
import io.rsocket.transport.netty.server.NettyContextCloseable
import io.rsocket.uri.UriTransportRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.File
import java.time.Duration
import java.util.concurrent.atomic.AtomicReference

@Command(name = NAME, description = "TCK Server")
class TckServer {

  @Option(name = arrayOf("-u", "--url"), description = "url")
  var uri = "tcp://localhost:30007"

  val runners: MutableMap<String, RunnerConnection> = mutableMapOf()

  private fun run(): Int {
    val server = RSocketFactory.receive()
        .acceptor { _, clientRSocket -> Mono.just(ServerRequestHandler(clientRSocket, this)) }
        .transport(UriTransportRegistry.serverForUri(uri))
        .start().block()

    if (server is NettyContextCloseable) {
      logger.info("server started at " + server.address().port)
    } else {
      logger.info("server started")
    }

    server!!.onClose().block(Duration.ofDays(365))

    return 0
  }

  fun selfTest(selfTestRequest: SelfTestRequest, connectionToRunner: RSocket): Flux<TestSuiteResults> {
    val target = RunnerConnection(selfTestRequest.runner, connectionToRunner)
    return executeTestSuites(target, target)
  }

  fun executeTestSuites(): Flux<TestSuiteResults> {
    val combinations = synchronized(runners) {
      runners.values.flatMap { c -> runners.values.map { s -> Pair(c, s) } }
    }.toList()

    return Flux.merge(combinations.map { (c, s) -> executeTestSuites(c, s) }).doOnTerminate({println("Finished " + combinations.size)})
  }

  private fun executeTestSuites(client: RunnerConnection, server: RunnerConnection): Flux<TestSuiteResults> {
    val versions = client.runner.capabilities.versions.intersect(server.runner.capabilities.versions)
    val excluded = if (client.runner.uuid != server.runner.uuid) setOf("local") else setOf()
    val transports = client.runner.capabilities.transports.intersect(server.runner.capabilities.transports).minus(excluded)

    val setups = versions.flatMap { v -> transports.map { t -> TestSuiteSetup(version = v, transport = t) } }

    return Flux.merge(tests.tck1Tests.flatMap { test -> setups.map { setup -> executeTestSuite(client, server, test, setup) } })
  }

  private fun executeTestSuite(client: RunnerConnection, server: RunnerConnection, test: TestCase, setup: TestSuiteSetup): Mono<TestSuiteResults> {
    val serverStream = server.requestStream(TCKMessage(runnerExecuteTest = RunnerExecuteTest(setup, "server", test)))

    val clientResult = serverStream.transform { s ->
      val closeable = AtomicReference<Disposable>()
      val hotServer = s.publish().autoConnect(1, { closeable.set(it) })

      hotServer.take(1).single().flatMap({ serverMessage ->
        client.requestResponse(TCKMessage(runnerExecuteTest = RunnerExecuteTest(setup.copy(url = serverMessage.runnerServerReady!!.url), "client", test)))
      }).doFinally({ _ -> closeable.get().dispose() })
    }.single()

    return clientResult.map { x -> x.runnerTestResult!!.result }.map { result ->
      TestSuiteResults(client.runner, server.runner, setup, listOf(result))
    }.doOnError({ x -> logger.error("error running test", x) })
  }

  fun registerRunner(runner: Runner, connectionToRunner: RSocket): Mono<TCKMessage> {
    addRunner(runner, connectionToRunner)
    connectionToRunner.onClose().doFinally({ removeRunner(runner) }).subscribe()
    return Mono.just(TCKMessage(simple = SimpleResponse()))
  }

  fun listAllRunners(): List<Runner> {
    synchronized(runners) {
      return runners.values.map(RunnerConnection::runner).toList()
    }
  }

  private fun removeRunner(runner: Runner) {
    synchronized(runners) {
      runners.remove(runner.uuid)
    }

    TckRunner.logger.info("Removed runner " + runner)
  }

  private fun addRunner(runner: Runner, connectionToRunner: RSocket) {
    synchronized(runners) {
      runners[runner.uuid] = RunnerConnection(runner, connectionToRunner)
    }

    TckRunner.logger.info("Added runner " + runner)
  }

  companion object {
    const val NAME = "tck-server"
    val logger: Logger = LoggerFactory.getLogger(TckServer::class.java)

    val tests = TestRepository(File("src/main/resources"))

    private fun fromArgs(vararg args: String): TckServer {
      return SingleCommand.singleCommand(TckServer::class.java).parse(*args)
    }

    @JvmStatic fun main(args: Array<String>) {
      val result = fromArgs(*args).run()
      System.exit(result)
    }
  }
}