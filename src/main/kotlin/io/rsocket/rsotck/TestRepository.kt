package io.rsocket.rsotck

import io.rsocket.rsotck.controlapi.test.Tck1Test
import io.rsocket.rsotck.controlapi.test.TestCase
import io.rsocket.tckdrivers.common.TckTestSuite
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class TestRepository(val folder: File) {
  var tck1Tests: List<TestCase>

  init {
    tck1Tests = loadTck1Files()
  }

  private fun loadTck1Files(): List<TestCase> =
      TckTestSuite.loadAll(folder).flatMap { s ->
        s.clientTests().map { c ->
          TestCase(s.suiteName + "." + c.name, Tck1Test(c.testLines().joinToString("\n"), s.testLines().joinToString("\n")))
        }
      }

  companion object {
    val logger: Logger = LoggerFactory.getLogger(TestRepository::class.java)
  }
}