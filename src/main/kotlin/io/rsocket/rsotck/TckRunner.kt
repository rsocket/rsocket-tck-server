package io.rsocket.rsotck

import io.airlift.airline.Command
import io.airlift.airline.Option
import io.airlift.airline.SingleCommand
import io.rsocket.rsotck.TckRunner.Companion.NAME
import io.rsocket.rsotck.controlapi.TCKMessage
import io.rsocket.rsotck.controlapi.api.Runner
import io.rsocket.rsotck.controlapi.api.RunnerCapabilities
import io.rsocket.rsotck.runner.RunnerRequestHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Command(name = NAME, description = "TCK Runner")
class TckRunner {

  @Option(name = arrayOf("-u", "--url"), description = "url")
  var uri = "tcp://localhost:30007"

  private fun run(): Int {
    val client = TckClient.connect(uri, fun(r) = RunnerRequestHandler(r))

    logger.info("client connected to " + uri)

    val runner = Runner(TckClient.uuid(), TckClient.codeVersion(), capabilities)
    client.requestResponse(TCKMessage(registerRunner = runner).payload()).block()

    logger.info("registered runner " + runner)

    client.onClose().block()

    return 0
  }

  companion object {
    const val NAME = "tck-runner"
    val logger: Logger = LoggerFactory.getLogger(TckRunner::class.java)
    val supportedPlatforms = listOf("rsocket-java")
    val supportedVersions = listOf("1.0")
    val supportedTransports = listOf("tcp", "ws", "local")
    val supportedModes = listOf("client", "server")
    val capabilities = RunnerCapabilities(supportedPlatforms, supportedVersions, supportedTransports, supportedModes)

    private fun fromArgs(vararg args: String): TckRunner {
      return SingleCommand.singleCommand(TckRunner::class.java).parse(*args)
    }

    @JvmStatic fun main(args: Array<String>) {
      val result = fromArgs(*args).run()
      System.exit(result)
    }
  }
}