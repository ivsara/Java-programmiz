package com.example

import com.example.core.jdk.JdkCatalogAndDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun verifyCompleteJdkVersionRangeFrom1_0To25Plus() {
        val catalog = JdkCatalogAndDetector.getDefaultJdkCatalog()
        assertEquals(26, catalog.size)
        assertTrue(catalog.any { it.versionId == "JDK_1_0" })
        assertTrue(catalog.any { it.versionId == "JDK_1_2" })
        assertTrue(catalog.any { it.versionId == "JDK_5" })
        assertTrue(catalog.any { it.versionId == "JDK_8" })
        assertTrue(catalog.any { it.versionId == "JDK_11" })
        assertTrue(catalog.any { it.versionId == "JDK_17" })
        assertTrue(catalog.any { it.versionId == "JDK_21" })
        assertTrue(catalog.any { it.versionId == "JDK_25" })

        val future26 = JdkCatalogAndDetector.createCustomJdkInstallation(26, "26.0.0", "OpenJDK", "/opt/jdk26")
        assertEquals("JDK_26", future26.versionId)
        assertEquals("70.0 (0x46)", future26.bytecodeVersion)
    }
}
