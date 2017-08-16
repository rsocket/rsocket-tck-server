package io.rsocket.rsotck.controlapi.api

data class ListRunnersRequest(
    val filters: RunnerCapabilities? = null
)