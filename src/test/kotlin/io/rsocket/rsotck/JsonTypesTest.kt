package io.rsocket.rsotck

import io.rsocket.rsotck.controlapi.TCKMessage
import io.rsocket.rsotck.controlapi.api.SimpleResponse
import org.junit.Test
import kotlin.test.assertEquals

class JsonTypesTest {
  @Test
  fun testTCKMessage() {
    assertEquals("{\"simple\":{\"success\":true}}", JsonTypes.tckMessage.toJson(TCKMessage(simple = SimpleResponse(true))))
    assertEquals(TCKMessage(simple = SimpleResponse(true)), JsonTypes.tckMessage.fromJson("{\"simple\":{\"success\":true}}"))
  }
}