package io.rsocket.rsotck

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.rsocket.rsotck.controlapi.TCKMessage

class JsonTypes {
  companion object {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()!!
    val tckMessage = moshi.adapter(TCKMessage::class.java)!!
  }
}