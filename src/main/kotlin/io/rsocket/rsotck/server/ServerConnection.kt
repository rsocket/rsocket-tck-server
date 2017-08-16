package io.rsocket.rsotck.server

import io.rsocket.Payload
import io.rsocket.RSocket
import io.rsocket.rsotck.JsonTypes
import io.rsocket.rsotck.controlapi.TCKMessage
import io.rsocket.rsotck.controlapi.api.ListRunnersRequest
import io.rsocket.rsotck.controlapi.api.ListTestCases
import io.rsocket.rsotck.controlapi.testsuite.ExecuteTestSuites
import io.rsocket.util.PayloadImpl
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ServerConnection(val serverConnection: RSocket) {
  private fun executeRequest(client: RSocket?, tckRequest: TCKMessage): Mono<TCKMessage> {
    val requestPayload = requestPayload(tckRequest)
//    TckCli.logger.info("request: ${requestPayload.dataUtf8}")
    return client!!.requestResponse(requestPayload).map { x ->
      val dataUtf8 = x.dataUtf8
//      TckCli.logger.info("response: $dataUtf8")
      TCKMessage.parse(dataUtf8)
    }
  }

  private fun executeStream(client: RSocket?, tckRequest: TCKMessage): Flux<TCKMessage> {
    val requestPayload = requestPayload(tckRequest)
//    TckCli.logger.info("stream: ${requestPayload.dataUtf8}")
    return client!!.requestStream(requestPayload).map { x ->
      val dataUtf8 = x.dataUtf8
//      TckCli.logger.info("response: $dataUtf8")
      TCKMessage.parse(dataUtf8)
    }
  }

  private fun requestPayload(tckRequest: TCKMessage): Payload {
    return PayloadImpl(JsonTypes.tckMessage.toJson(tckRequest))
  }

  fun listRunners() = executeRequest(serverConnection, TCKMessage(listRunnersRequest = ListRunnersRequest())).map { it.listRunnersResponse!! }!!
  fun listTests() = executeRequest(serverConnection, TCKMessage(listTestCases = ListTestCases())).map { it.testCases!! }!!
  fun executeTestSuites() = executeStream(serverConnection, TCKMessage(executeTestSuites = ExecuteTestSuites())).map { it.testSuiteResults!! }!!
}