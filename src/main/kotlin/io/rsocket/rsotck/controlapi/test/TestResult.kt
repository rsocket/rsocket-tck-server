package io.rsocket.rsotck.controlapi.test;

data class TestResult(
    val testName: String,
    val result: String,
    val clientDetail: String?,
    val serverDetail: String?
) {
  companion object {
    fun passed(testName: String) = TestResult(testName, "passed", null, null)
    fun failed(testName: String, detail: String) = TestResult(testName, "failed", detail, null)
    fun skipped(testName: String, reason: String) = TestResult(testName, "skipped", reason, null)
  }
}