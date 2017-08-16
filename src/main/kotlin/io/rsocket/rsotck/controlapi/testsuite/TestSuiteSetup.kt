package io.rsocket.rsotck.controlapi.testsuite

data class TestSuiteSetup(
    val url: String? = null,
    val version: String,
    val transport: String
)
