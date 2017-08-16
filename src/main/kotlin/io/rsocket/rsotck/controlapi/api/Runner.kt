package io.rsocket.rsotck.controlapi.api

data class Runner(
    val uuid: String,
    val codeversion: String = "unknown",
    val capabilities: RunnerCapabilities
)