package io.rsocket.rsotck.controlapi.runner

import io.rsocket.rsotck.controlapi.test.TestCase
import io.rsocket.rsotck.controlapi.testsuite.TestSuiteSetup

data class RunnerExecuteTest(
    val setup: TestSuiteSetup,
    val mode: String,
    val test: TestCase
)