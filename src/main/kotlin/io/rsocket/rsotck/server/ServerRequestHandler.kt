package io.rsocket.rsotck.server

import io.rsocket.RSocket
import io.rsocket.rsotck.TckServer
import io.rsocket.rsotck.controlapi.ApiRequestHandler
import io.rsocket.rsotck.controlapi.TCKMessage
import io.rsocket.rsotck.controlapi.api.ListRunnersResponse
import io.rsocket.rsotck.controlapi.api.TestCaseList
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.RuntimeException

class ServerRequestHandler(connectionToClient: RSocket, val tckServer: TckServer) : ApiRequestHandler(connectionToClient) {

  override fun executeTckRequest(request: TCKMessage, connectionToClient: RSocket): Mono<TCKMessage> =
      when {
        request.listRunnersRequest != null -> listRunners()
        request.registerRunner != null -> tckServer.registerRunner(request.registerRunner, connectionToClient)
        request.listTestCases != null -> listTestCases()
        else -> Mono.error(RuntimeException("unknown method $request"))
      }

  override fun executeTckRequestStream(request: TCKMessage, connectionToClient: RSocket): Flux<TCKMessage> =
      when {
        request.selfTestRequest != null -> tckServer.selfTest(request.selfTestRequest, connectionToClient).map { TCKMessage(testSuiteResults = it) }
        request.executeTestSuites != null -> tckServer.executeTestSuites().map { TCKMessage(testSuiteResults = it) }
        else -> Flux.error(RuntimeException("unknown method $request"))
      }

  private fun listTestCases(): Mono<TCKMessage> {
    return Mono.just(TCKMessage(testCases = TestCaseList(TckServer.tests.tck1Tests)))
  }

  private fun listRunners() = Mono.just(TCKMessage(listRunnersResponse = ListRunnersResponse(tckServer.listAllRunners())))
}
