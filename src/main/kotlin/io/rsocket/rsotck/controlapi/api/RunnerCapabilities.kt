package io.rsocket.rsotck.controlapi.api

data class RunnerCapabilities(
    val platform: List<String>,
    val versions: List<String>,
    val transports: List<String>,
    val modes: List<String> = listOf("client", "server"),
    val testFormats: List<String> = listOf("tck1")
)