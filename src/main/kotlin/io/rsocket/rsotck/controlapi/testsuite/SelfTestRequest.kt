package io.rsocket.rsotck.controlapi.testsuite

import io.rsocket.rsotck.controlapi.api.Runner

data class SelfTestRequest(
    val runner: Runner
)