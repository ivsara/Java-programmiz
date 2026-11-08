package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.compiler.JavaCompilerEngine
import com.example.core.editor.JavaVersionSyntaxLexer
import com.example.core.execution.JavaExecutionEngine
import com.example.core.jdk.JdkCatalogAndDetector
import com.example.core.testing.StudioVerificationSuite
import com.example.data.content.ApiDocsCatalog
import com.example.data.content.PracticeLabCatalog
import com.example.data.content.VersionExplorerCatalog
import com.example.data.db.JDKInstallations
import com.example.data.db.LearningProgress
import com.example.data.db.Projects
import com.example.data.db.StudioDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun verifyAppNameAndResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Universal Java Studio", appName)
    }

    @Test
    fun verifyAll35StudioVerificationTestsPass() {
        val report = StudioVerificationSuite.runAll35VerificationTests()
        assertEquals(35, report.size)
        val failed = report.filter { it.status == "FAILED" }
        assertTrue("Expected 0 failed tests, but found: $failed", failed.isEmpty())
    }

    @Test
    fun verifyRoomDatabaseEntitiesProjectsJdkInstallationsAndLearningProgress() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, StudioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.studioDao()

        // 1. Insert and query Projects entity
        val projectId = dao.insertProject(
            Projects(
                name = "OfflineJava17Lab",
                description = "Java 17 Records and Sealed Classes Project",
                jdkVersionId = "JDK_17",
                packageName = "com.studio",
                mainClass = "Main",
                templateType = "Console"
            )
        )
        val storedProjects = dao.getAllProjectsOnce()
        val storedProject = storedProjects.find { it.id == projectId }
        assertNotNull(storedProject)
        assertEquals("OfflineJava17Lab", storedProject?.name)
        assertEquals("JDK_17", storedProject?.jdkVersionId)

        // 2. Insert and query JDKInstallations entity
        val jdk17: JDKInstallations = JdkCatalogAndDetector.getDefaultJdkCatalog().first { it.versionId == "JDK_17" }
        dao.insertJdk(jdk17)
        val storedJdks = dao.getAllJdksOnce()
        val storedJdk = storedJdks.find { it.versionId == "JDK_17" }
        assertNotNull(storedJdk)
        assertEquals("61.0 (0x3D)", storedJdk?.bytecodeVersion)

        // 3. Insert and query LearningProgress entity
        dao.upsertLearningProgress(
            LearningProgress(
                itemId = "L_16_1",
                moduleType = "TUTORIAL",
                chapterOrCategory = "CH_16",
                targetJdkVersion = "JDK_17",
                isCompleted = true,
                masteryScore = 100
            )
        )
        val progressList = dao.getAllLearningProgress().first()
        assertEquals(1, progressList.size)
        assertTrue(progressList.first().isCompleted)
        assertEquals("JDK_17", progressList.first().targetJdkVersion)

        db.close()
    }

    @Test
    fun verifyVersionSpecificSyntaxHighlightingAcrossJavaVersions() {
        val sampleSource = """
            import java.applet.Applet;
            record Point(int x, int y) {}
            public strictfp class Demo extends Applet {
                @Override
                public void run() {
                    var msg = "Hello";
                    Thread.currentThread().stop();
                }
            }
        """.trimIndent()

        // Under JDK 1.2 (Java 2, major = 2):
        // - 'record', 'var', and '@Override' are unsupported future features
        // - '.stop()' is deprecated since JDK 1.2
        val summaryJava2 = JavaVersionSyntaxLexer.analyzeAndSummarize(
            source = sampleSource,
            jdkMajor = 2,
            jdkDisplayName = "JDK 1.2 (Java 2)"
        )
        assertTrue(
            "Expected unsupported future tokens under JDK 1.2, got ${summaryJava2.unsupportedTokensDetected}",
            summaryJava2.unsupportedTokensDetected.isNotEmpty()
        )
        assertTrue(summaryJava2.unsupportedTokensDetected.any { it.contains("record") })
        assertTrue(summaryJava2.unsupportedTokensDetected.any { it.contains("var") })
        assertTrue(summaryJava2.deprecatedTokensDetected.isNotEmpty())

        // Under JDK 21 (major = 21):
        // - 'record', 'var', and '@Override' are supported (0 unsupported tokens)
        // - 'Applet', 'strictfp', and '.stop()' are highlighted as deprecated/obsolete
        val summaryJava21 = JavaVersionSyntaxLexer.analyzeAndSummarize(
            source = sampleSource,
            jdkMajor = 21,
            jdkDisplayName = "Java 21 LTS"
        )
        assertEquals(0, summaryJava21.unsupportedTokensDetected.size)
        assertTrue(summaryJava21.modernFeatureTokensDetected.size >= 2)
        assertTrue(summaryJava21.deprecatedTokensDetected.any { it.contains("Applet") })
        assertTrue(summaryJava21.deprecatedTokensDetected.any { it.contains("strictfp") })
    }

    @Test
    fun verifyJavaVersionExplorerComparisonJava8VsJava17() {
        val java8 = VersionExplorerCatalog.findProfile("JDK_8")
        val java17 = VersionExplorerCatalog.findProfile("JDK_17")

        assertEquals(52, java8.bytecodeMajor)
        assertEquals(61, java17.bytecodeMajor)
        assertTrue(java8.isLts)
        assertTrue(java17.isLts)
        assertTrue(java8.languageChanges.contains("Lambda"))
        assertTrue(java17.languageChanges.contains("Sealed Classes"))
        assertTrue(java17.jvmAndGcChanges.contains("ZGC"))
        assertTrue(VersionExplorerCatalog.presetPairs.contains("JDK_8" to "JDK_17"))
    }

    @Test
    fun verifyJava2AndModernCompilerAndExecutionEngine() {
        val jdks = JdkCatalogAndDetector.getDefaultJdkCatalog().associateBy { it.versionId }
        val jdk12 = jdks.getValue("JDK_1_2")
        val jdk21 = jdks.getValue("JDK_21")

        val java2Source = """
            public class HelloWorld {
                public static void main(String args[]) {
                    System.out.println("Hello World");
                }
            }
        """.trimIndent()
        val (comp12, exec12) = JavaExecutionEngine.compileAndExecute("HelloWorld.java", java2Source, jdk12)
        assertTrue(comp12.success)
        assertEquals(46, comp12.generatedClasses.first().bytecodeMajor)
        assertEquals("Hello World", exec12?.stdout)

        val genericsSource = """
            import java.util.*;
            public class GenTest {
                public static void main(String args[]) {
                    List<String> items = new ArrayList<String>();
                }
            }
        """.trimIndent()
        val badComp12 = JavaCompilerEngine.compileSource("GenTest.java", genericsSource, jdk12)
        assertFalse("Generics must fail under JDK 1.2", badComp12.success)

        val modernSource = """
            record Metric(String label, int value) {}
            public class ModernApp {
                public static void main(String[] args) {
                    Metric m = new Metric("JDK", 21);
                    System.out.println(m.label() + "-" + m.value());
                }
            }
        """.trimIndent()
        val (comp21, exec21) = JavaExecutionEngine.compileAndExecute("ModernApp.java", modernSource, jdk21)
        assertTrue(comp21.success)
        assertEquals(65, comp21.generatedClasses.first().bytecodeMajor)
        assertEquals("JDK-21", exec21?.stdout)
    }

    @Test
    fun verifyApiDocsVersionBoundaryAnd25PracticeCategories() {
        val java2Available = ApiDocsCatalog.entries.filter { it.introducedMajor <= 2 }
        assertFalse(java2Available.any { it.typeName == "Scanner" })
        assertFalse(java2Available.any { it.typeName.startsWith("Stream") })
        assertTrue(java2Available.any { it.typeName == "Vector" })
        assertTrue(java2Available.any { it.typeName == "ArrayList" })
        assertEquals(25, PracticeLabCatalog.exercises.size)
    }
}
