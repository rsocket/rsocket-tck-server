package io.rsocket.rsotck.runner

import io.rsocket.Closeable
import io.rsocket.RSocket
import io.rsocket.RSocketFactory
import io.rsocket.rsotck.controlapi.ApiRequestHandler
import io.rsocket.rsotck.controlapi.TCKMessage
import io.rsocket.rsotck.controlapi.runner.RunnerExecuteTest
import io.rsocket.rsotck.controlapi.runner.RunnerServerReady
import io.rsocket.rsotck.controlapi.runner.RunnerTestResult
import io.rsocket.rsotck.controlapi.test.TestResult
import io.rsocket.tckdrivers.client.JavaClientDriver
import io.rsocket.tckdrivers.common.TckClientTest
import io.rsocket.tckdrivers.server.JavaServerDriver
import io.rsocket.transport.netty.server.NettyContextCloseable
import io.rsocket.uri.UriTransportRegistry
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.atomic.AtomicInteger

class RunnerRequestHandler(connectionToClient: RSocket) : ApiRequestHandler(connectionToClient) {
  override fun executeTckRequest(request: TCKMessage, connectionToClient: RSocket): Mono<TCKMessage> {
    val runnerExecuteTest = request.runnerExecuteTest!!

    return executeClientTest(runnerExecuteTest).map { TCKMessage(runnerTestResult = it) }
  }

  private fun executeClientTest(runnerExecuteTest: RunnerExecuteTest): Mono<RunnerTestResult> {
    if (runnerExecuteTest.setup.version != "1.0")
      throw IllegalArgumentException("version ${runnerExecuteTest.setup.version} not supported")

    if (runnerExecuteTest.mode != "client")
      throw IllegalArgumentException("mode ${runnerExecuteTest.mode} not supported for request")

    val url = runnerExecuteTest.setup.url
    val testName = runnerExecuteTest.test.testName
    val testScript = runnerExecuteTest.test.tck1Definition?.clientScript ?: throw IllegalArgumentException("tck1 client script expected")

    val client = RSocketFactory.connect().transport(UriTransportRegistry.clientForUri(url)).start()

    val jd2 = JavaClientDriver(client)

    return Mono.create<RunnerTestResult>({
      try {
        jd2.runTest(TckClientTest(testName, testScript.split("\n")))
        it.success(RunnerTestResult(TestResult.passed(testName)))
      } catch (e: Exception) {
        it.success(RunnerTestResult(TestResult.failed(testName, e.toString())))
      }
    }).subscribeOn(Schedulers.elastic())
  }

  override fun executeTckRequestStream(request: TCKMessage, connectionToClient: RSocket): Flux<TCKMessage> {
    val runnerExecuteTest = request.runnerExecuteTest!!

    if (runnerExecuteTest.setup.version != "1.0")
      throw IllegalArgumentException("version ${runnerExecuteTest.setup.version} not supported")

    if (runnerExecuteTest.mode != "server")
      throw IllegalArgumentException("mode ${runnerExecuteTest.mode} not supported for stream")

    val testScript = runnerExecuteTest.test.tck1Definition?.serverScript ?: throw IllegalArgumentException("tck1 client script expected")

    val transport = runnerExecuteTest.setup.transport

    val javaServerDriver = JavaServerDriver()
    javaServerDriver.parse(testScript.split("\n"))

    val uri = urlForTransport(transport)
    val server = RSocketFactory.receive()
        .acceptor(javaServerDriver.acceptor())
        .transport(UriTransportRegistry.serverForUri(uri))
        .start()

    return server
        .flatMapMany { closeable ->
          val actualUri = actualLocalUrl(transport, uri, closeable)
          Flux.just(TCKMessage(runnerServerReady = RunnerServerReady(actualUri)))
              .concatWith(Flux.never())
              .doFinally({ _ -> closeable.close() })
        }
        .doOnError({ it.printStackTrace() })
  }


  fun actualLocalUrl(transport: String, uri: String, closeable: Closeable): String {
    return if (transport == "tcp") {
      // TODO get external IP?
      "tcp://localhost:" + nettyPort(closeable)
    } else if (transport == "ws") {
      // TODO get external IP?
      "ws://localhost:" + nettyPort(closeable)
    } else {
      return uri
    }
  }

  fun nettyPort(closeable: Closeable): Int {
    return (closeable as NettyContextCloseable).address().port
  }

  fun urlForTransport(transport: String): String {
    if (transport == "local") {
      return "local:tck" + localCounter.incrementAndGet()
    } else if (transport == "tcp") {
      // TODO get external IP?
      return "tcp://localhost:0"
    } else if (transport == "ws") {
      // TODO get external IP?
      return "ws://localhost:0"
    } else {
      throw UnsupportedOperationException("unknown transport '$transport'")
    }
  }

  companion object {
    private val localCounter = AtomicInteger()
  }
}