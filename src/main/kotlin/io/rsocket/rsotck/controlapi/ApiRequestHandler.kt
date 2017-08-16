package io.rsocket.rsotck.controlapi

import io.rsocket.AbstractRSocket
import io.rsocket.Payload
import io.rsocket.RSocket
import io.rsocket.rsotck.TckServer
import io.rsocket.util.PayloadImpl
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class ApiRequestHandler(val connectionToClient: RSocket) : AbstractRSocket() {
  override fun requestResponse(payload: Payload?): Mono<Payload> {
    val dataUtf8 = payload!!.dataUtf8
    val request = TCKMessage.parse(dataUtf8)
//    TckServer.logger.info("request: $dataUtf8")
    return executeTckRequest(request!!, connectionToClient).map { x ->
      val response = x.json()
//      TckServer.logger.info("response: $response")
      PayloadImpl(response)
    }
  }

  abstract fun executeTckRequest(request: TCKMessage, connectionToClient: RSocket): Mono<TCKMessage>

  override fun requestStream(payload: Payload?): Flux<Payload> {
    val dataUtf8 = payload!!.dataUtf8
    val request = TCKMessage.parse(dataUtf8)
//    TckServer.logger.info("request: $dataUtf8")
    return executeTckRequestStream(request!!, connectionToClient).map { x ->
      val response = x.json()
//      TckServer.logger.info("response: $response")
      PayloadImpl(response)
    }
  }

  abstract fun executeTckRequestStream(request: TCKMessage, connectionToClient: RSocket): Flux<TCKMessage>
}