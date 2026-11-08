package com.example.core.jdk

import com.example.data.db.JdkInstallationEntity
import java.io.File

data class JdkCompatibilityBreakdown(
    val versionId: String,
    val languageCompatibility: String,
    val apiCompatibility: String,
    val bytecodeCompatibility: String,
    val actualCompilerCompatibility: String,
    val actualRuntimeCompatibility: String,
    val historicalAuthenticityNote: String
)

data class HostEnvironmentReport(
    val detectedVmName: String,
    val detectedJavaVersion: String,
    val detectedVendor: String,
    val detectedArch: String,
    val detectedJavaHome: String,
    val scannedSystemJdks: List<String>
)

object JdkCatalogAndDetector {

    fun probeHostEnvironment(): HostEnvironmentReport {
        val vmName = System.getProperty("java.vm.name") ?: "Android Runtime / OpenJDK VM"
        val javaVer = System.getProperty("java.version") ?: "21.0"
        val vendor = System.getProperty("java.vendor") ?: "OpenJDK / Google"
        val arch = System.getProperty("os.arch") ?: "aarch64 / x86_64"
        val javaHome = System.getProperty("java.home") ?: "/system"

        val commonDirs = listOf(
            "/usr/lib/jvm",
            "/opt/jdk",
            "/opt/java",
            System.getenv("JAVA_HOME") ?: ""
        ).filter { it.isNotBlank() }

        val discoveredPaths = mutableListOf<String>()
        for (dirPath in commonDirs) {
            try {
                val f = File(dirPath)
                if (f.exists() && f.isDirectory) {
                    f.listFiles()?.forEach { child ->
                        if (child.isDirectory && (File(child, "bin/javac").exists() || File(child, "bin/java").exists())) {
                            discoveredPaths.add(child.absolutePath)
                        }
                    }
                }
            } catch (_: Exception) {
                // Restricted directory on mobile sandbox
            }
        }
        return HostEnvironmentReport(
            detectedVmName = vmName,
            detectedJavaVersion = javaVer,
            detectedVendor = vendor,
            detectedArch = arch,
            detectedJavaHome = javaHome,
            scannedSystemJdks = discoveredPaths
        )
    }

    fun getFiveLayerCompatibility(jdk: JdkInstallationEntity): JdkCompatibilityBreakdown {
        val major = jdk.majorNumber
        return when {
            major <= 1 -> JdkCompatibilityBreakdown(
                versionId = jdk.versionId,
                languageCompatibility = "JDK 1.${major} Grammar: Basic classes, interfaces, packages, exceptions. Inner classes added in 1.1. NO strictfp, assert, generics, enums, or lambdas.",
                apiCompatibility = "JDK 1.${major} Core API: java.lang, java.util (Vector, Hashtable, Enumeration), java.io, java.net, java.awt, java.applet. Reflection & JDBC 1.0 added in 1.1. NO Collections Framework (List/Map/ArrayList).",
                bytecodeCompatibility = "Class file major.minor 45.3 (0x2D). Classic stack frame layout without StackMapTable.",
                actualCompilerCompatibility = if (jdk.status.contains("Configured") || jdk.status.contains("Legacy VM"))
                    "Legacy sun.tools.javac.Main (-classpath, -d, -O). Does not accept -source or --release."
                else
                    "Historical JDK 1.${major} binary not natively mounted on 64-bit host; Strict Language/API/Bytecode AST Verifier active (Cross-compile warning: modern javac --release only supports >= 8).",
                actualRuntimeCompatibility = "Requires 32-bit Legacy Classic VM or Compatibility Interpreter. Classic green-threads/native-threads runtime.",
                historicalAuthenticityNote = "IMPORTANT: Modern javac with -source cannot target JDK 1.${major}. Universal Java Studio enforces genuine JDK 1.${major} grammar and API boundaries without misrepresenting modern --release as an original 1996/1997 binary."
            )
            major in 2..4 -> JdkCompatibilityBreakdown(
                versionId = jdk.versionId,
                languageCompatibility = when (major) {
                    2 -> "Java 2 (JDK 1.2) Grammar: Adds 'strictfp' keyword. Inner classes & anonymous classes supported. NO 'assert' (1.4), NO Generics/Enums/For-Each/Autoboxing (5.0)."
                    3 -> "Java 2 (JDK 1.3) Grammar: Identical to 1.2 language syntax. Dynamic proxies & JNDI at API level."
                    else -> "Java 2 (JDK 1.4) Grammar: Adds 'assert' keyword (requires -source 1.4). NO Generics, Enums, Annotations, or For-Each."
                },
                apiCompatibility = when (major) {
                    2 -> "Java 2 SE 1.2 API: Introduces Collections Framework (Collection, List, ArrayList, LinkedList, Map, HashMap, Set, TreeSet), Swing (javax.swing.*), Java 2D, WeakReference. NO java.nio, NO java.util.Scanner, NO StringBuilder."
                    3 -> "Java 2 SE 1.3 API: Adds Timer/TimerTask, Java Sound API, JNDI, RMI-IIOP. NO java.nio or regex."
                    else -> "Java 2 SE 1.4 API: Adds java.nio (Buffers/Channels), java.util.regex (Pattern/Matcher), java.util.logging, ImageIO, XML JAXP. NO Scanner, StringBuilder, or java.util.concurrent."
                },
                bytecodeCompatibility = "Class file major.minor ${44 + major}.0 (0x${(44 + major).toString(16).uppercase()}). Pre-StackMapTable verifier.",
                actualCompilerCompatibility = "Legacy javac (-target 1.${major} ${if (major == 4) "-source 1.4 " else ""}-bootclasspath <jdk1.${major}/jre/lib/rt.jar> -classpath -d). Note: Modern JDK 9+ javac removed 1.2/1.3/1.4 targets.",
                actualRuntimeCompatibility = "HotSpot 1.${major} Client/Server VM (or Isolated Legacy VM / Studio AST Runtime with strict Java 2 classpath isolation).",
                historicalAuthenticityNote = "DISTINCTION: Compiling with a newer javac -source 1.${major} without JDK 1.${major} rt.jar bootclasspath leaks modern APIs. Universal Java Studio explicitly enforces the JDK 1.${major} API symbol table and flags any post-1.${major} class or method."
            )
            major in 5..8 -> JdkCompatibilityBreakdown(
                versionId = jdk.versionId,
                languageCompatibility = when (major) {
                    5, 6 -> "Java ${major} Grammar: Generics <T>, Enhanced for-each loop, Autoboxing/Unboxing, Enums, Varargs (...), Annotations (@Override), Static imports."
                    7 -> "Java 7 (Project Coin) Grammar: Diamond operator <>, try-with-resources, Strings in switch, multi-catch (catch A | B), binary literals (0b), numeric underscores."
                    else -> "Java 8 Grammar: Lambda expressions (->), Method references (::), Default & static interface methods, Type annotations, Repeating annotations."
                },
                apiCompatibility = when (major) {
                    5 -> "Java SE 5 API: java.util.Scanner, StringBuilder, java.util.concurrent, Formatter (printf), Instrumentation."
                    6 -> "Java SE 6 API: Compiler API (javax.tools.JavaCompiler), Console, Scripting (JSR 223), JDBC 4.0, Deque/ArrayDeque, NavigableSet."
                    7 -> "Java SE 7 API: NIO.2 (java.nio.file.Path, Files, Paths), ForkJoinPool, Objects utility class, InvokeDynamic."
                    else -> "Java SE 8 API: Stream API (java.util.stream), Optional<T>, java.time (LocalDate, LocalDateTime, Instant), CompletableFuture, Base64."
                },
                bytecodeCompatibility = "Class file major.minor ${44 + major}.0 (0x${(44 + major).toString(16).uppercase()}). ${if (major >= 6) "Requires StackMapTable frames for type-checking verifier." else "Classic verifier."}",
                actualCompilerCompatibility = "javac -source 1.${major} -target 1.${major} -bootclasspath <rt.jar> -classpath -d (or --release ${major} when cross-compiled from JDK 9–21).",
                actualRuntimeCompatibility = "HotSpot 64-bit Server VM (JRE 1.${major} rt.jar monolithic classpath before JPMS modularity).",
                historicalAuthenticityNote = "Pre-JPMS monolithic runtime (rt.jar + tools.jar). Applets and AWT/Swing fully present without module flags."
            )
            major in 9..17 -> JdkCompatibilityBreakdown(
                versionId = jdk.versionId,
                languageCompatibility = buildString {
                    append("Java $major Grammar: JPMS module-info.java (9)")
                    if (major >= 10) append(", Local-Variable Type Inference 'var' (10)")
                    if (major >= 14) append(", Switch Expressions '->' & 'yield' (14)")
                    if (major >= 15) append(", Text Blocks '\"\"\"' (15)")
                    if (major >= 16) append(", Records & Pattern Matching for instanceof (16)")
                    if (major >= 17) append(", Sealed Classes & Interfaces 'permits' (17 LTS)")
                },
                apiCompatibility = buildString {
                    append("Modular JDK $major API: Collection Factory Methods List.of/Map.of (9), StackWalker (9)")
                    if (major >= 11) append(", Standard java.net.http.HttpClient (11), String.isBlank/lines/repeat (11)")
                    if (major >= 16) append(", Stream.toList() (16)")
                    if (major >= 17) append(", RandomGenerator API, HexFormat (17). Applet API marked for removal.")
                },
                bytecodeCompatibility = "Class file major.minor ${44 + major}.0 (0x${(44 + major).toString(16).uppercase()}). Nest-based access control (11+) & Record/PermittedSubclasses attributes (16/17).",
                actualCompilerCompatibility = "javac --release $major --module-path -classpath -d (Enforces exact historical symbol table via ct.sym).",
                actualRuntimeCompatibility = "Modular HotSpot JVM (jimage modules lib/modules instead of rt.jar). G1GC default garbage collector.",
                historicalAuthenticityNote = "Uses javac --release $major ct.sym historical symbol descriptors for combined language, API, and bytecode verification."
            )
            else -> JdkCompatibilityBreakdown(
                versionId = jdk.versionId,
                languageCompatibility = buildString {
                    append("Modern Java $major Grammar: All JDK 17 LTS features")
                    if (major >= 21) append(", Record Patterns & Pattern Matching for switch (21 LTS), Unnamed Patterns '_'")
                    if (major >= 22) append(", Unnamed Variables & Patterns (22)")
                    if (major >= 23) append(", Markdown Documentation Comments /// (23)")
                    if (major >= 25) append(", Compact Source Files & Instance Main Methods, Flexible Constructor Bodies, Module Import Declarations (25 LTS)")
                },
                apiCompatibility = buildString {
                    append("Java SE $major API: UTF-8 by Default (18), Simple Web Server (18)")
                    if (major >= 21) append(", Virtual Threads Thread.ofVirtual() (Project Loom 21), SequencedCollection (21)")
                    if (major >= 22) append(", Foreign Function & Memory API (22)")
                    if (major >= 24) append(", Stream Gatherers (24), Class-File API (24)")
                    if (major >= 25) append(", Scoped Values (25), Key Derivation Function API (25)")
                },
                bytecodeCompatibility = "Class file major.minor ${44 + major}.0 (0x${(44 + major).toString(16).uppercase()}).",
                actualCompilerCompatibility = "javac --release $major -classpath --module-path -d -Xlint:all",
                actualRuntimeCompatibility = "Modern 64-bit HotSpot JVM (Generational ZGC / G1GC, Virtual Thread carrier scheduler).",
                historicalAuthenticityNote = if (major > 25)
                    "Extensible Future JDK Profile: Automatically maps major version $major to bytecode ${44 + major}.0 and --release $major."
                else
                    "Modern LTS/Feature Toolchain supporting Project Loom, Amber, and Panama capabilities."
            )
        }
    }

    fun getDefaultJdkCatalog(): List<JdkInstallationEntity> {
        val host = probeHostEnvironment()
        return listOf(
            // 3.1 Historical JDK Versions (1.0 - 1.4)
            JdkInstallationEntity(
                versionId = "JDK_1_0",
                displayName = "JDK 1.0.2 (Oak / First Release)",
                majorNumber = 0,
                versionString = "1.0.2",
                vendor = "Sun Microsystems",
                javaHome = "/opt/legacy-jdks/jdk1.0.2",
                compilerPath = "/opt/legacy-jdks/jdk1.0.2/bin/javac",
                runtimePath = "/opt/legacy-jdks/jdk1.0.2/bin/java",
                architecture = "32-bit x86 (Legacy VM Container)",
                bytecodeVersion = "45.3 (0x2D)",
                status = "Legacy VM / Strict AST Mode",
                compatibilityMode = "Strict JDK 1.0 Language + API + Bytecode 45.3",
                supportedCompilerFlags = "-classpath -d -O -g -nowarn",
                releaseYear = 1996
            ),
            JdkInstallationEntity(
                versionId = "JDK_1_1",
                displayName = "JDK 1.1.8 (Inner Classes, JNI, Reflection, JDBC)",
                majorNumber = 1,
                versionString = "1.1.8_010",
                vendor = "Sun Microsystems",
                javaHome = "/opt/legacy-jdks/jdk1.1.8",
                compilerPath = "/opt/legacy-jdks/jdk1.1.8/bin/javac",
                runtimePath = "/opt/legacy-jdks/jdk1.1.8/bin/java",
                architecture = "32-bit x86 (Legacy VM Container)",
                bytecodeVersion = "45.3 (0x2D)",
                status = "Legacy VM / Strict AST Mode",
                compatibilityMode = "Strict JDK 1.1 Language + API + Bytecode 45.3",
                supportedCompilerFlags = "-classpath -d -O -g -deprecation",
                releaseYear = 1997
            ),
            JdkInstallationEntity(
                versionId = "JDK_1_2",
                displayName = "JDK 1.2.2 – Java 2 (Playground, Collections, Swing)",
                majorNumber = 2,
                versionString = "1.2.2_017",
                vendor = "Sun Microsystems",
                javaHome = "/opt/legacy-jdks/jdk1.2.2",
                compilerPath = "/opt/legacy-jdks/jdk1.2.2/bin/javac",
                runtimePath = "/opt/legacy-jdks/jdk1.2.2/bin/java",
                architecture = "32-bit x86 (Legacy VM Container)",
                bytecodeVersion = "46.0 (0x2E)",
                status = "Configured Legacy Toolchain",
                compatibilityMode = "Genuine Java 2 (JDK 1.2) Language + rt.jar API + Bytecode 46.0",
                supportedCompilerFlags = "-target 1.2 -bootclasspath /opt/legacy-jdks/jdk1.2.2/jre/lib/rt.jar -classpath -d",
                releaseYear = 1998
            ),
            JdkInstallationEntity(
                versionId = "JDK_1_3",
                displayName = "JDK 1.3.1 – Java 2 (HotSpot VM, JNDI, Java Sound)",
                majorNumber = 3,
                versionString = "1.3.1_28",
                vendor = "Sun Microsystems",
                javaHome = "/opt/legacy-jdks/jdk1.3.1",
                compilerPath = "/opt/legacy-jdks/jdk1.3.1/bin/javac",
                runtimePath = "/opt/legacy-jdks/jdk1.3.1/bin/java",
                architecture = "32-bit x86 (Legacy VM Container)",
                bytecodeVersion = "47.0 (0x2F)",
                status = "Legacy VM / Strict AST Mode",
                compatibilityMode = "Strict JDK 1.3 Language + API + Bytecode 47.0",
                supportedCompilerFlags = "-target 1.3 -bootclasspath /opt/legacy-jdks/jdk1.3.1/jre/lib/rt.jar -classpath -d",
                releaseYear = 2000
            ),
            JdkInstallationEntity(
                versionId = "JDK_1_4",
                displayName = "JDK 1.4.2 – Java 2 (assert, NIO, Regex, Logging)",
                majorNumber = 4,
                versionString = "1.4.2_19",
                vendor = "Sun Microsystems",
                javaHome = "/opt/legacy-jdks/j2sdk1.4.2",
                compilerPath = "/opt/legacy-jdks/j2sdk1.4.2/bin/javac",
                runtimePath = "/opt/legacy-jdks/j2sdk1.4.2/bin/java",
                architecture = "32-bit x86 (Legacy VM Container)",
                bytecodeVersion = "48.0 (0x30)",
                status = "Configured Legacy Toolchain",
                compatibilityMode = "Strict JDK 1.4 (-source 1.4 -target 1.4) + Bytecode 48.0",
                supportedCompilerFlags = "-source 1.4 -target 1.4 -bootclasspath /opt/legacy-jdks/j2sdk1.4.2/jre/lib/rt.jar -classpath -d",
                releaseYear = 2002
            ),

            // 3.2 Java 5 to Java 8
            JdkInstallationEntity(
                versionId = "JDK_5",
                displayName = "JDK 5.0 (Tiger – Generics, Enums, Annotations, Autoboxing)",
                majorNumber = 5,
                versionString = "1.5.0_22",
                vendor = "Sun Microsystems",
                javaHome = "/opt/jdks/jdk1.5.0_22",
                compilerPath = "/opt/jdks/jdk1.5.0_22/bin/javac",
                runtimePath = "/opt/jdks/jdk1.5.0_22/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "49.0 (0x31)",
                status = "Configured Toolchain",
                compatibilityMode = "Java 5 Language + API + Bytecode 49.0",
                supportedCompilerFlags = "-source 1.5 -target 1.5 -classpath -d -Xlint:unchecked",
                releaseYear = 2004
            ),
            JdkInstallationEntity(
                versionId = "JDK_6",
                displayName = "JDK 6 (Mustang – Compiler API, JDBC 4.0, Scripting)",
                majorNumber = 6,
                versionString = "1.6.0_45",
                vendor = "Oracle / Sun",
                javaHome = "/opt/jdks/jdk1.6.0_45",
                compilerPath = "/opt/jdks/jdk1.6.0_45/bin/javac",
                runtimePath = "/opt/jdks/jdk1.6.0_45/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "50.0 (0x32)",
                status = "Configured Toolchain",
                compatibilityMode = "Java 6 Language + API + StackMapTable Bytecode 50.0",
                supportedCompilerFlags = "-source 1.6 -target 1.6 -classpath -d -proc:none",
                releaseYear = 2006
            ),
            JdkInstallationEntity(
                versionId = "JDK_7",
                displayName = "JDK 7 (Dolphin – Try-with-resources, Diamond <>, NIO.2)",
                majorNumber = 7,
                versionString = "1.7.0_80",
                vendor = "Oracle Corporation",
                javaHome = "/opt/jdks/jdk1.7.0_80",
                compilerPath = "/opt/jdks/jdk1.7.0_80/bin/javac",
                runtimePath = "/opt/jdks/jdk1.7.0_80/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "51.0 (0x33)",
                status = "Configured Toolchain",
                compatibilityMode = "Java SE 7 Language + API + InvokeDynamic Bytecode 51.0",
                supportedCompilerFlags = "-source 1.7 -target 1.7 -classpath -d -Xlint:all",
                releaseYear = 2011
            ),
            JdkInstallationEntity(
                versionId = "JDK_8",
                displayName = "JDK 8 LTS (Lambdas, Streams, Optional, java.time)",
                majorNumber = 8,
                versionString = "1.8.0_412",
                vendor = "Eclipse Adoptium (Temurin)",
                javaHome = "/usr/lib/jvm/temurin-8-jdk",
                compilerPath = "/usr/lib/jvm/temurin-8-jdk/bin/javac",
                runtimePath = "/usr/lib/jvm/temurin-8-jdk/bin/java",
                architecture = "64-bit ${host.detectedArch}",
                bytecodeVersion = "52.0 (0x34)",
                status = "Available (LTS Toolchain)",
                compatibilityMode = "Full JDK 8 LTS Language + API + Bytecode 52.0",
                supportedCompilerFlags = "-source 1.8 -target 1.8 -classpath -d -parameters",
                releaseYear = 2014
            ),

            // 3.3 Java 9 to Java 17
            JdkInstallationEntity(
                versionId = "JDK_9",
                displayName = "JDK 9 (JPMS Modules, JShell, Collection Factories)",
                majorNumber = 9,
                versionString = "9.0.4",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-9",
                compilerPath = "/usr/lib/jvm/openjdk-9/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-9/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "53.0 (0x35)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 9 + Modular API + Bytecode 53.0",
                supportedCompilerFlags = "--release 9 --module-path -classpath -d",
                releaseYear = 2017
            ),
            JdkInstallationEntity(
                versionId = "JDK_10",
                displayName = "JDK 10 (Local-Variable Type Inference 'var')",
                majorNumber = 10,
                versionString = "10.0.2",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-10",
                compilerPath = "/usr/lib/jvm/openjdk-10/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-10/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "54.0 (0x36)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 10 + Bytecode 54.0",
                supportedCompilerFlags = "--release 10 -classpath -d",
                releaseYear = 2018
            ),
            JdkInstallationEntity(
                versionId = "JDK_11",
                displayName = "JDK 11 LTS (Standard HttpClient, Single-File Source Run)",
                majorNumber = 11,
                versionString = "11.0.24",
                vendor = "Eclipse Adoptium (Temurin)",
                javaHome = "/usr/lib/jvm/temurin-11-jdk",
                compilerPath = "/usr/lib/jvm/temurin-11-jdk/bin/javac",
                runtimePath = "/usr/lib/jvm/temurin-11-jdk/bin/java",
                architecture = "64-bit ${host.detectedArch}",
                bytecodeVersion = "55.0 (0x37)",
                status = "Available (LTS Toolchain)",
                compatibilityMode = "Full JDK 11 LTS (--release 11) + Bytecode 55.0",
                supportedCompilerFlags = "--release 11 --module-path -classpath -d",
                releaseYear = 2018
            ),
            JdkInstallationEntity(
                versionId = "JDK_12",
                displayName = "JDK 12 (Shenandoah GC, String indent/transform)",
                majorNumber = 12,
                versionString = "12.0.2",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-12",
                compilerPath = "/usr/lib/jvm/openjdk-12/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-12/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "56.0 (0x38)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 12 + Bytecode 56.0",
                supportedCompilerFlags = "--release 12 -classpath -d",
                releaseYear = 2019
            ),
            JdkInstallationEntity(
                versionId = "JDK_13",
                displayName = "JDK 13 (Dynamic CDS Archives, ZGC Uncommit)",
                majorNumber = 13,
                versionString = "13.0.2",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-13",
                compilerPath = "/usr/lib/jvm/openjdk-13/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-13/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "57.0 (0x39)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 13 + Bytecode 57.0",
                supportedCompilerFlags = "--release 13 -classpath -d",
                releaseYear = 2019
            ),
            JdkInstallationEntity(
                versionId = "JDK_14",
                displayName = "JDK 14 (Switch Expressions, Helpful NullPointerExceptions)",
                majorNumber = 14,
                versionString = "14.0.2",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-14",
                compilerPath = "/usr/lib/jvm/openjdk-14/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-14/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "58.0 (0x3A)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 14 + Bytecode 58.0",
                supportedCompilerFlags = "--release 14 -classpath -d",
                releaseYear = 2020
            ),
            JdkInstallationEntity(
                versionId = "JDK_15",
                displayName = "JDK 15 (Text Blocks \"\"\", Edwards-Curve Crypto, ZGC)",
                majorNumber = 15,
                versionString = "15.0.2",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-15",
                compilerPath = "/usr/lib/jvm/openjdk-15/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-15/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "59.0 (0x3B)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 15 + Bytecode 59.0",
                supportedCompilerFlags = "--release 15 -classpath -d",
                releaseYear = 2020
            ),
            JdkInstallationEntity(
                versionId = "JDK_16",
                displayName = "JDK 16 (Records, Pattern Matching instanceof, Stream.toList)",
                majorNumber = 16,
                versionString = "16.0.2",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-16",
                compilerPath = "/usr/lib/jvm/openjdk-16/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-16/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "60.0 (0x3C)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 16 + Bytecode 60.0",
                supportedCompilerFlags = "--release 16 -classpath -d",
                releaseYear = 2021
            ),
            JdkInstallationEntity(
                versionId = "JDK_17",
                displayName = "JDK 17 LTS (Sealed Classes, Strong Encapsulation, HexFormat)",
                majorNumber = 17,
                versionString = "17.0.12",
                vendor = "Eclipse Adoptium (Temurin)",
                javaHome = "/usr/lib/jvm/temurin-17-jdk",
                compilerPath = "/usr/lib/jvm/temurin-17-jdk/bin/javac",
                runtimePath = "/usr/lib/jvm/temurin-17-jdk/bin/java",
                architecture = "64-bit ${host.detectedArch}",
                bytecodeVersion = "61.0 (0x3D)",
                status = "Available (LTS Toolchain)",
                compatibilityMode = "Full JDK 17 LTS (--release 17) + Bytecode 61.0",
                supportedCompilerFlags = "--release 17 --module-path -classpath -d",
                releaseYear = 2021
            ),

            // 3.4 Java 18 to Java 25
            JdkInstallationEntity(
                versionId = "JDK_18",
                displayName = "JDK 18 (UTF-8 by Default, Simple Web Server jwebserver)",
                majorNumber = 18,
                versionString = "18.0.2",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-18",
                compilerPath = "/usr/lib/jvm/openjdk-18/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-18/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "62.0 (0x3E)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 18 + Bytecode 62.0",
                supportedCompilerFlags = "--release 18 -classpath -d",
                releaseYear = 2022
            ),
            JdkInstallationEntity(
                versionId = "JDK_19",
                displayName = "JDK 19 (Loom/Panama Preview, New HashMap.newHashMap)",
                majorNumber = 19,
                versionString = "19.0.2",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-19",
                compilerPath = "/usr/lib/jvm/openjdk-19/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-19/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "63.0 (0x3F)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 19 + Bytecode 63.0",
                supportedCompilerFlags = "--release 19 -classpath -d",
                releaseYear = 2022
            ),
            JdkInstallationEntity(
                versionId = "JDK_20",
                displayName = "JDK 20 (Scoped Values & Record Patterns Incubator/Preview)",
                majorNumber = 20,
                versionString = "20.0.2",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-20",
                compilerPath = "/usr/lib/jvm/openjdk-20/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-20/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "64.0 (0x40)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 20 + Bytecode 64.0",
                supportedCompilerFlags = "--release 20 -classpath -d",
                releaseYear = 2023
            ),
            JdkInstallationEntity(
                versionId = "JDK_21",
                displayName = "JDK 21 LTS (Virtual Threads, Switch Patterns, SequencedCollection)",
                majorNumber = 21,
                versionString = "21.0.4",
                vendor = "Eclipse Adoptium (Temurin)",
                javaHome = host.detectedJavaHome,
                compilerPath = "${host.detectedJavaHome}/bin/javac",
                runtimePath = "${host.detectedJavaHome}/bin/java",
                architecture = "64-bit ${host.detectedArch}",
                bytecodeVersion = "65.0 (0x41)",
                status = "Native Runtime Detected (Default LTS)",
                compatibilityMode = "Full JDK 21 LTS (--release 21) + Bytecode 65.0",
                supportedCompilerFlags = "--release 21 --module-path -classpath -d -Xlint:all",
                releaseYear = 2023,
                isDefault = true
            ),
            JdkInstallationEntity(
                versionId = "JDK_22",
                displayName = "JDK 22 (Unnamed Variables '_', Foreign Function & Memory API)",
                majorNumber = 22,
                versionString = "22.0.2",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-22",
                compilerPath = "/usr/lib/jvm/openjdk-22/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-22/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "66.0 (0x42)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 22 + Bytecode 66.0",
                supportedCompilerFlags = "--release 22 -classpath -d",
                releaseYear = 2024
            ),
            JdkInstallationEntity(
                versionId = "JDK_23",
                displayName = "JDK 23 (Markdown Javadoc ///, Generational ZGC Default)",
                majorNumber = 23,
                versionString = "23.0.1",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-23",
                compilerPath = "/usr/lib/jvm/openjdk-23/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-23/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "67.0 (0x43)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 23 + Bytecode 67.0",
                supportedCompilerFlags = "--release 23 -classpath -d",
                releaseYear = 2024
            ),
            JdkInstallationEntity(
                versionId = "JDK_24",
                displayName = "JDK 24 (Stream Gatherers, Class-File API, Quantum-Resistant Crypto)",
                majorNumber = 24,
                versionString = "24.0.1",
                vendor = "OpenJDK",
                javaHome = "/usr/lib/jvm/openjdk-24",
                compilerPath = "/usr/lib/jvm/openjdk-24/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-24/bin/java",
                architecture = "64-bit x86_64",
                bytecodeVersion = "68.0 (0x44)",
                status = "Configured Release Profile",
                compatibilityMode = "javac --release 24 + Bytecode 68.0",
                supportedCompilerFlags = "--release 24 -classpath -d",
                releaseYear = 2025
            ),
            JdkInstallationEntity(
                versionId = "JDK_25",
                displayName = "JDK 25 LTS (Compact Source Files, Scoped Values, Flexible Constructors)",
                majorNumber = 25,
                versionString = "25.0.1",
                vendor = "OpenJDK / Oracle",
                javaHome = "/usr/lib/jvm/openjdk-25",
                compilerPath = "/usr/lib/jvm/openjdk-25/bin/javac",
                runtimePath = "/usr/lib/jvm/openjdk-25/bin/java",
                architecture = "64-bit ${host.detectedArch}",
                bytecodeVersion = "69.0 (0x45)",
                status = "Available (Latest LTS Toolchain)",
                compatibilityMode = "Full JDK 25 LTS (--release 25) + Bytecode 69.0",
                supportedCompilerFlags = "--release 25 --enable-preview -classpath -d",
                releaseYear = 2025
            )
        )
    }

    /**
     * Extensible factory for any future or custom JDK release (e.g. JDK 26, JDK 27, custom vendor).
     */
    fun createCustomJdkInstallation(
        majorNumber: Int,
        versionString: String,
        vendor: String,
        javaHome: String,
        architecture: String = "64-bit x86_64 / ARM64"
    ): JdkInstallationEntity {
        val classMajor = 44 + majorNumber
        val hex = classMajor.toString(16).uppercase()
        val versionId = if (majorNumber <= 4) "JDK_1_$majorNumber" else "JDK_$majorNumber"
        val flags = if (majorNumber < 9) {
            "-source 1.$majorNumber -target 1.$majorNumber -bootclasspath $javaHome/jre/lib/rt.jar -classpath -d"
        } else {
            "--release $majorNumber --module-path -classpath -d"
        }
        return JdkInstallationEntity(
            versionId = versionId,
            displayName = "JDK $majorNumber ($versionString - $vendor)",
            majorNumber = majorNumber,
            versionString = versionString,
            vendor = vendor,
            javaHome = javaHome,
            compilerPath = "$javaHome/bin/javac",
            runtimePath = "$javaHome/bin/java",
            architecture = architecture,
            bytecodeVersion = "$classMajor.0 (0x$hex)",
            status = "Configured Custom Toolchain",
            compatibilityMode = "JDK $majorNumber Language + API + Bytecode $classMajor.0",
            supportedCompilerFlags = flags,
            releaseYear = 1996 + majorNumber + 4,
            isDefault = false,
            isCustomAdded = true
        )
    }
}
