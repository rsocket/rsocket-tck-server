package io.rsocket.rsotck.controlapi.testsuite

data class ExecuteTestSuites(
    val client: List<String>? = null,
    val server: List<String>? = null,
    val testcases: List<String>? = null,
    val version: List<String>? = null,
    val transport: List<String>? = null
)