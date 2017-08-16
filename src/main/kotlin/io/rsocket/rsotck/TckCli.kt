package io.rsocket.rsotck

import io.airlift.airline.Command
import io.airlift.airline.Option
import io.airlift.airline.SingleCommand
import io.rsocket.rsotck.TckCli.Companion.NAME
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Command(name = NAME, description = "TCK CLI")
class TckCli {

  @Option(name = arrayOf("-u", "--url"), description = "url")
  var uri = "tcp://localhost:30007"

  @Option(name = arrayOf("--list-runners"), description = "List runners")
  var listRunners = false

  @Option(name = arrayOf("--list-tests"), description = "List tests")
  var listTests = false

  @Option(name = arrayOf("-x", "--execute-tests"), description = "Execute Tests")
  var executeTest = false

  private fun run(): Int {
    val client = TckClient.api(uri)

    if (listRunners) {
      val runners = client.listRunners().block()!!.runners
      println("runners")
      runners.forEach({ (uuid, _, capabilities) -> println("$uuid\t${capabilities.platform}\t${capabilities.transports}\t${capabilities.versions}") })
    } else if (listTests) {
      val tests = client.listTests().block()!!.tests
      println("tests")
      tests.forEach { (testName) -> println(testName) }
    } else if (executeTest) {
      client.executeTestSuites().doOnNext { (client1, server, setup, tests) ->
        tests.forEach {
          println(client1.uuid + "\t" + server.uuid + "\t" + setup.transport + "\t" + it.testName + "\t" + it.result)
        }
      }.then().block()
    }

    return 0
  }

  companion object {
    const val NAME = "tck-cli"
    val logger: Logger = LoggerFactory.getLogger(TckCli::class.java)

    private fun fromArgs(vararg args: String): TckCli {
      return SingleCommand.singleCommand(TckCli::class.java).parse(*args)
    }

    @JvmStatic fun main(args: Array<String>) {
      val result = fromArgs("-x").run()
      System.exit(result)
    }
  }
}