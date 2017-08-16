package io.rsocket.rsotck.controlapi

import io.rsocket.Payload
import io.rsocket.rsotck.JsonTypes
import io.rsocket.rsotck.controlapi.api.*
import io.rsocket.rsotck.controlapi.runner.RunnerExecuteTest
import io.rsocket.rsotck.controlapi.runner.RunnerServerReady
import io.rsocket.rsotck.controlapi.runner.RunnerTestResult
import io.rsocket.rsotck.controlapi.testsuite.ExecuteTestSuites
import io.rsocket.rsotck.controlapi.testsuite.SelfTestRequest
import io.rsocket.rsotck.controlapi.testsuite.TestSuiteResults
import io.rsocket.util.PayloadImpl

data class TCKMessage(
    val simple: SimpleResponse? = null,
    val executeTestSuites: ExecuteTestSuites? = null,
    val testSuiteResults: TestSuiteResults? = null,
    val listRunnersResponse: ListRunnersResponse? = null,
    val listRunnersRequest: ListRunnersRequest? = null,
    val listTestCases: ListTestCases? = null,
    val testCases: TestCaseList? = null,
    val registerRunner: Runner? = null,
    val selfTestRequest: SelfTestRequest? = null,
    val runnerExecuteTest: RunnerExecuteTest? = null,
    val runnerTestResult: RunnerTestResult? = null,
    val runnerServerReady: RunnerServerReady? = null
) {
  fun json(): String = JsonTypes.tckMessage.toJson(this)
  fun payload(): Payload = PayloadImpl(json())

  companion object {
    fun parse(json: String) = JsonTypes.tckMessage.fromJson(json)
    fun parse(payload: Payload) = parse(payload.dataUtf8)
  }
}