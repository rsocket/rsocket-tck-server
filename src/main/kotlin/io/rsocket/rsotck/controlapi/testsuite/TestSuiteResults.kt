package io.rsocket.rsotck.controlapi.testsuite

import io.rsocket.rsotck.controlapi.api.Runner
import io.rsocket.rsotck.controlapi.test.TestResult

data class TestSuiteResults(
    val client: Runner,
    val server: Runner,
    val setup: TestSuiteSetup,
    val tests: List<TestResult>
)