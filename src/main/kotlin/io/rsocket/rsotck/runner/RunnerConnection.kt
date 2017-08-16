package io.rsocket.rsotck.runner

import io.rsocket.RSocket
import io.rsocket.rsotck.controlapi.TCKMessage
import io.rsocket.rsotck.controlapi.api.Runner
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class RunnerConnection(val runner: Runner, val rsocket: RSocket) {
    fun requestStream(tckMessage: TCKMessage): Flux<TCKMessage> {
//      logger.info("runner requestStream: " + tckMessage.json())
      return rsocket.requestStream(tckMessage.payload()).map({ p ->
        //        logger.info("runner stream response: " + p.dataUtf8)
        TCKMessage.parse(p)
      })
    }

    fun requestResponse(tckMessage: TCKMessage): Mono<TCKMessage> {
//      logger.info("runner requestResponse: " + tckMessage.json())
      return rsocket.requestResponse(tckMessage.payload()).map({ p ->
        //        logger.info("runner response: " + p.dataUtf8)
        TCKMessage.parse(p)
      })
    }
  }