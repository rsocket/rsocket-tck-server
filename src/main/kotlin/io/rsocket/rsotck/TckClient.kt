package io.rsocket.rsotck

import io.rsocket.AbstractRSocket
import io.rsocket.RSocket
import io.rsocket.RSocketFactory
import io.rsocket.rsotck.server.ServerConnection
import io.rsocket.uri.UriTransportRegistry
import java.util.*

object TckClient {
  fun uuid() = UUID.randomUUID().toString()

  fun connect(uri: String, requestHandler: (RSocket) -> RSocket = fun(_) = noopHandler()): RSocket =
      RSocketFactory.connect().keepAlive()
          .acceptor { r -> requestHandler(r) }
          .transport(UriTransportRegistry.clientForUri(uri))
          .start().block()!!

  fun api(uri: String) = ServerConnection(connect(uri))

  fun codeVersion(): String = "0.9-SNAPSHOT"

  fun noopHandler(): RSocket = object : AbstractRSocket() {}
}