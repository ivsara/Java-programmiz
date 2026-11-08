package com.example.core.compiler

import com.example.data.db.JdkInstallationEntity

enum class DiagnosticSeverity {
    ERROR, WARNING, INFO
}

data class CompilerDiagnostic(
    val line: Int,
    val column: Int,
    val severity: DiagnosticSeverity,
    val code: String,
    val message: String,
    val minimumJdkRequired: String? = null
)

data class GeneratedClassArtifact(
    val className: String,
    val fileName: String,
    val bytecodeMajor: Int,
    val bytecodeMinor: Int,
    val hexHeader: String,
    val detectedMethods: List<String>,
    val constantPoolCount: Int,
    val disassemblyPreview: String
)

data class CompilationResult(
    val success: Boolean,
    val jdkUsed: JdkInstallationEntity,
    val compilerCommand: String,
    val diagnostics: List<CompilerDiagnostic>,
    val generatedClasses: List<GeneratedClassArtifact>,
    val formattedConsoleOutput: String,
    val compilationTimeMs: Long
)

object JavaCompilerEngine {

    fun compileSource(
        fileName: String,
        sourceCode: String,
        jdk: JdkInstallationEntity,
        additionalProjectSources: Map<String, String> = emptyMap(),
        customClasspath: String = "."
    ): CompilationResult {
        val startMs = System.currentTimeMillis()
        val diagnostics = mutableListOf<CompilerDiagnostic>()

        // 1. Validate Compiler Availability
        if (jdk.status.contains("Unavailable", ignoreCase = true)) {
            val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(4L)
            val errDiag = CompilerDiagnostic(
                line = 1,
                column = 1,
                severity = DiagnosticSeverity.ERROR,
                code = "JDK_UNAVAILABLE",
                message = "Compiler binary '${jdk.compilerPath}' for ${jdk.displayName} is marked Unavailable. Configure a valid installation path in JDK Manager."
            )
            return CompilationResult(
                success = false,
                jdkUsed = jdk,
                compilerCommand = "${jdk.compilerPath} $fileName",
                diagnostics = listOf(errDiag),
                generatedClasses = emptyList(),
                formattedConsoleOutput = "[COMPILER ERROR] ${errDiag.message}",
                compilationTimeMs = elapsed
            )
        }

        // 2. Build version-authentic compiler command
        val compilerCmd = buildCompilerCommand(fileName, jdk, customClasspath)

        // 3. Perform lexical, syntactic, and version-specific semantic analysis
        if (sourceCode.isBlank()) {
            diagnostics.add(
                CompilerDiagnostic(
                    line = 1,
                    column = 1,
                    severity = DiagnosticSeverity.ERROR,
                    code = "EMPTY_SOURCE",
                    message = "$fileName:1: error: Source file is empty; expected class, interface, enum, or record declaration."
                )
            )
        } else {
            analyzeSyntaxAndStructure(fileName, sourceCode, jdk, diagnostics)
            analyzeVersionCompatibility(fileName, sourceCode, jdk, diagnostics)
        }

        val hasErrors = diagnostics.any { it.severity == DiagnosticSeverity.ERROR }
        val generatedClasses = if (!hasErrors) {
            generateClassArtifacts(fileName, sourceCode, jdk, additionalProjectSources)
        } else {
            emptyList()
        }

        val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(8L)
        val formattedOutput = formatCompilerOutput(
            fileName = fileName,
            jdk = jdk,
            compilerCmd = compilerCmd,
            diagnostics = diagnostics,
            generatedClasses = generatedClasses,
            elapsedMs = elapsed
        )

        return CompilationResult(
            success = !hasErrors,
            jdkUsed = jdk,
            compilerCommand = compilerCmd,
            diagnostics = diagnostics,
            generatedClasses = generatedClasses,
            formattedConsoleOutput = formattedOutput,
            compilationTimeMs = elapsed
        )
    }

    fun buildCompilerCommand(
        fileName: String,
        jdk: JdkInstallationEntity,
        classpath: String = "."
    ): String {
        val major = jdk.majorNumber
        return when {
            major <= 1 -> {
                "${jdk.compilerPath} -classpath $classpath -d build/classes $fileName"
            }
            major in 2..3 -> {
                "${jdk.compilerPath} -target 1.$major -bootclasspath ${jdk.javaHome}/jre/lib/rt.jar -classpath $classpath -d build/classes $fileName"
            }
            major == 4 -> {
                "${jdk.compilerPath} -source 1.4 -target 1.4 -bootclasspath ${jdk.javaHome}/jre/lib/rt.jar -classpath $classpath -d build/classes $fileName"
            }
            major in 5..8 -> {
                "${jdk.compilerPath} -source 1.$major -target 1.$major -bootclasspath ${jdk.javaHome}/jre/lib/rt.jar -classpath $classpath -d build/classes $fileName"
            }
            else -> {
                "${jdk.compilerPath} --release $major -classpath $classpath -d build/classes $fileName"
            }
        }
    }

    private fun analyzeSyntaxAndStructure(
        fileName: String,
        sourceCode: String,
        jdk: JdkInstallationEntity,
        diagnostics: MutableList<CompilerDiagnostic>
    ) {
        val lines = sourceCode.lines()
        var braceBalance = 0
        var parenBalance = 0
        var bracketBalance = 0
        var inBlockComment = false
        var inTextBlock = false
        var lastOpenBraceLine = 1
        var hasTypeDeclaration = false

        val typeDeclRegex = Regex("""\b(class|interface|enum|record)\s+([A-Za-z_][A-Za-z0-9_]*)""")

        lines.forEachIndexed { idx, rawLine ->
            val lineNum = idx + 1
            var i = 0
            val cleanLineBuilder = StringBuilder()

            while (i < rawLine.length) {
                if (!inBlockComment && i + 2 < rawLine.length && rawLine.substring(i, i + 3) == "\"\"\"") {
                    inTextBlock = !inTextBlock
                    i += 3
                    continue
                }
                if (inTextBlock) {
                    i++
                    continue
                }
                if (!inBlockComment && i + 1 < rawLine.length && rawLine[i] == '/' && rawLine[i + 1] == '/') {
                    break // Rest of line is single-line comment
                }
                if (!inBlockComment && i + 1 < rawLine.length && rawLine[i] == '/' && rawLine[i + 1] == '*') {
                    inBlockComment = true
                    i += 2
                    continue
                }
                if (inBlockComment) {
                    if (i + 1 < rawLine.length && rawLine[i] == '*' && rawLine[i + 1] == '/') {
                        inBlockComment = false
                        i += 2
                    } else {
                        i++
                    }
                    continue
                }
                // Handle string literal on single line
                if (rawLine[i] == '"') {
                    val startCol = i + 1
                    i++
                    var closed = false
                    while (i < rawLine.length) {
                        if (rawLine[i] == '\\') {
                            i += 2
                            continue
                        }
                        if (rawLine[i] == '"') {
                            closed = true
                            i++
                            break
                        }
                        i++
                    }
                    if (!closed) {
                        diagnostics.add(
                            CompilerDiagnostic(
                                line = lineNum,
                                column = startCol,
                                severity = DiagnosticSeverity.ERROR,
                                code = "UNCLOSED_STRING_LITERAL",
                                message = "$fileName:$lineNum:$startCol: error: unclosed string literal"
                            )
                        )
                    }
                    cleanLineBuilder.append("\"STR\"")
                    continue
                }

                val ch = rawLine[i]
                when (ch) {
                    '{' -> {
                        braceBalance++
                        lastOpenBraceLine = lineNum
                    }
                    '}' -> {
                        braceBalance--
                        if (braceBalance < 0) {
                            diagnostics.add(
                                CompilerDiagnostic(
                                    line = lineNum,
                                    column = i + 1,
                                    severity = DiagnosticSeverity.ERROR,
                                    code = "UNMATCHED_CLOSING_BRACE",
                                    message = "$fileName:$lineNum:${i + 1}: error: class, interface, or enum expected (unmatched '}')"
                                )
                            )
                            braceBalance = 0
                        }
                    }
                    '(' -> parenBalance++
                    ')' -> {
                        parenBalance--
                        if (parenBalance < 0) {
                            diagnostics.add(
                                CompilerDiagnostic(
                                    line = lineNum,
                                    column = i + 1,
                                    severity = DiagnosticSeverity.ERROR,
                                    code = "UNMATCHED_PAREN",
                                    message = "$fileName:$lineNum:${i + 1}: error:illegal start of expression (unmatched ')')"
                                )
                            )
                            parenBalance = 0
                        }
                    }
                    '[' -> bracketBalance++
                    ']' -> bracketBalance--
                }
                cleanLineBuilder.append(ch)
                i++
            }

            val stripped = cleanLineBuilder.toString().trim()
            if (typeDeclRegex.containsMatchIn(stripped)) {
                hasTypeDeclaration = true
            }

            // Check obvious missing semicolon on standard statements
            if (stripped.isNotEmpty() && !inBlockComment && !inTextBlock) {
                val isStatementNeedingSemicolon =
                    (stripped.startsWith("System.out.") ||
                        stripped.startsWith("return ") ||
                        stripped == "return" ||
                        stripped.startsWith("package ") ||
                        stripped.startsWith("import ") ||
                        stripped.startsWith("throw ") ||
                        stripped.startsWith("break") ||
                        stripped.startsWith("continue")) &&
                        !stripped.endsWith(";") &&
                        !stripped.endsWith("{") &&
                        !stripped.endsWith("}") &&
                        !stripped.endsWith("+") &&
                        !stripped.endsWith(",") &&
                        !stripped.endsWith("(")
                if (isStatementNeedingSemicolon) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = rawLine.length,
                            severity = DiagnosticSeverity.ERROR,
                            code = "MISSING_SEMICOLON",
                            message = "$fileName:$lineNum: error: ';' expected at end of statement"
                        )
                    )
                }
            }
        }

        if (braceBalance > 0) {
            diagnostics.add(
                CompilerDiagnostic(
                    line = lines.size,
                    column = 1,
                    severity = DiagnosticSeverity.ERROR,
                    code = "UNCLOSED_BRACE",
                    message = "$fileName:${lines.size}: error: reached end of file while parsing (missing '}' for block opened near line $lastOpenBraceLine)"
                )
            )
        }
        if (parenBalance > 0) {
            diagnostics.add(
                CompilerDiagnostic(
                    line = lines.size,
                    column = 1,
                    severity = DiagnosticSeverity.ERROR,
                    code = "UNCLOSED_PAREN",
                    message = "$fileName:${lines.size}: error: ')' expected before end of file"
                )
            )
        }

        // In JDK 25+, JEP 512 allows compact source files with instance main(), while JDK <= 24 requires class/interface/enum/record
        if (!hasTypeDeclaration && jdk.majorNumber < 25) {
            diagnostics.add(
                CompilerDiagnostic(
                    line = 1,
                    column = 1,
                    severity = DiagnosticSeverity.ERROR,
                    code = "MISSING_CLASS_DECL",
                    message = "$fileName:1: error: class, interface, or enum expected (Compact source files without class declarations require JDK 25+)",
                    minimumJdkRequired = "JDK 25"
                )
            )
        }
    }

    /**
     * Checks language grammar and API usage against the exact selected JDK version.
     */
    fun analyzeVersionCompatibility(
        fileName: String,
        sourceCode: String,
        jdk: JdkInstallationEntity,
        diagnostics: MutableList<CompilerDiagnostic>
    ) {
        val major = jdk.majorNumber
        val lines = sourceCode.lines()

        lines.forEachIndexed { idx, rawLine ->
            val lineNum = idx + 1
            val trimmed = rawLine.trim()
            if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) {
                return@forEachIndexed
            }
            val codePart = trimmed.substringBefore("//")

            // 1. Inner classes check for JDK 1.0
            if (major < 1 && Regex("""\bclass\b.*\bclass\b""").containsMatchIn(codePart)) {
                diagnostics.add(
                    CompilerDiagnostic(
                        line = lineNum,
                        column = 1,
                        severity = DiagnosticSeverity.ERROR,
                        code = "INNER_CLASS_1_1",
                        message = "$fileName:$lineNum: error: inner classes are not supported in -source 1.0 (use -source 1.1 or higher)",
                        minimumJdkRequired = "JDK 1.1"
                    )
                )
            }

            // 2. Collections Framework & Swing check for JDK 1.0 / 1.1 (Introduced in Java 2 / JDK 1.2)
            if (major < 2) {
                val java2Apis = listOf("ArrayList", "LinkedList", "HashMap", "TreeMap", "HashSet", "Collection", "Collections", "javax.swing", "JFrame", "JButton", "strictfp")
                for (api in java2Apis) {
                    if (Regex("""\b$api\b""").containsMatchIn(codePart)) {
                        diagnostics.add(
                            CompilerDiagnostic(
                                line = lineNum,
                                column = 1,
                                severity = DiagnosticSeverity.ERROR,
                                code = "JAVA_2_API_REQUIRED",
                                message = "$fileName:$lineNum: error: symbol '$api' is not available in ${jdk.displayName}. Collections Framework, Swing, and 'strictfp' were introduced in Java 2 (JDK 1.2).",
                                minimumJdkRequired = "JDK 1.2 (Java 2)"
                            )
                        )
                    }
                }
            }

            // 3. 'assert' keyword & NIO / Regex check (Introduced in JDK 1.4)
            if (major < 4) {
                if (Regex("""\bassert\s+""").containsMatchIn(codePart)) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "ASSERT_1_4_REQUIRED",
                            message = "$fileName:$lineNum: error: as of release 1.4, 'assert' is a keyword; not supported in ${jdk.versionId} (-source 1.$major)",
                            minimumJdkRequired = "JDK 1.4"
                        )
                    )
                }
                if (codePart.contains("java.nio.") || codePart.contains("java.util.regex.")) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "API_1_4_REQUIRED",
                            message = "$fileName:$lineNum: error: package java.nio / java.util.regex does not exist in ${jdk.displayName} rt.jar (Introduced in JDK 1.4)",
                            minimumJdkRequired = "JDK 1.4"
                        )
                    )
                }
            }

            // 4. Java 5 Features: Generics <T>, Enhanced for-each, Enums, Annotations, Varargs, Scanner, StringBuilder
            if (major < 5) {
                // Generics e.g. List<String>, ArrayList<Integer>, Map<String, Integer>
                if (Regex("""\b[A-Z][A-Za-z0-9_]*\s*<\s*[A-Z?][A-Za-z0-9_,\s<>?]*>""").containsMatchIn(codePart)) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "GENERICS_JDK5_REQUIRED",
                            message = "$fileName:$lineNum: error: generics are not supported in -source 1.$major (use -source 5 or higher to enable generics; in Java 2 use raw types such as Vector or List)",
                            minimumJdkRequired = "JDK 5"
                        )
                    )
                }
                // Enhanced for-each loop: for (Type var : expr)
                if (Regex("""\bfor\s*\(\s*[A-Za-z0-9_<>\[\]]+\s+[A-Za-z_][A-Za-z0-9_]*\s*:""").containsMatchIn(codePart)) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "FOREACH_JDK5_REQUIRED",
                            message = "$fileName:$lineNum: error: for-each loops are not supported in -source 1.$major (use standard index loop or Iterator/Enumeration in Java 2)",
                            minimumJdkRequired = "JDK 5"
                        )
                    )
                }
                // Enums
                if (Regex("""\benum\s+[A-Za-z_][A-Za-z0-9_]*""").containsMatchIn(codePart)) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "ENUM_JDK5_REQUIRED",
                            message = "$fileName:$lineNum: error: enums are not supported in -source 1.$major (use -source 5 or higher)",
                            minimumJdkRequired = "JDK 5"
                        )
                    )
                }
                // Annotations (@Override, @Deprecated, @SuppressWarnings)
                if (Regex("""@[A-Z][A-Za-z0-9_]*""").containsMatchIn(codePart)) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "ANNOTATIONS_JDK5_REQUIRED",
                            message = "$fileName:$lineNum: error: annotations are not supported in -source 1.$major (use -source 5 or higher)",
                            minimumJdkRequired = "JDK 5"
                        )
                    )
                }
                // Java 5 APIs: Scanner, StringBuilder, printf, concurrent
                for (jdk5Api in listOf("Scanner", "StringBuilder", "java.util.concurrent")) {
                    if (Regex("""\b$jdk5Api\b""").containsMatchIn(codePart)) {
                        val replacementHint = when (jdk5Api) {
                            "StringBuilder" -> "Use StringBuffer in JDK 1.0–1.4"
                            "Scanner" -> "Use BufferedReader + InputStreamReader in JDK 1.1–1.4"
                            else -> "Use synchronized / wait / notify in JDK 1.0–1.4"
                        }
                        diagnostics.add(
                            CompilerDiagnostic(
                                line = lineNum,
                                column = 1,
                                severity = DiagnosticSeverity.ERROR,
                                code = "API_JDK5_REQUIRED",
                                message = "$fileName:$lineNum: error: cannot find symbol '$jdk5Api' in ${jdk.displayName} API. Introduced in JDK 5. ($replacementHint)",
                                minimumJdkRequired = "JDK 5"
                            )
                        )
                    }
                }
            }

            // 5. Java 7 Features: Diamond operator <>, try-with-resources, java.nio.file.Files
            if (major < 7) {
                if (codePart.contains("<>")) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "DIAMOND_JDK7_REQUIRED",
                            message = "$fileName:$lineNum: error: diamond operator <> is not supported in -source 1.$major (use -source 7 or higher)",
                            minimumJdkRequired = "JDK 7"
                        )
                    )
                }
                if (Regex("""\btry\s*\(""").containsMatchIn(codePart)) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "TWR_JDK7_REQUIRED",
                            message = "$fileName:$lineNum: error: try-with-resources is not supported in -source 1.$major (use -source 7 or higher)",
                            minimumJdkRequired = "JDK 7"
                        )
                    )
                }
            }

            // 6. Java 8 Features: Lambda expressions (->), Method references (::), Stream API, Optional, java.time
            if (major < 8) {
                val isSwitchArrow = codePart.startsWith("case ") || codePart.startsWith("default ")
                if (codePart.contains("->") && !isSwitchArrow) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "LAMBDA_JDK8_REQUIRED",
                            message = "$fileName:$lineNum: error: lambda expressions are not supported in -source 1.$major (use -source 8 or higher)",
                            minimumJdkRequired = "JDK 8"
                        )
                    )
                }
                if (codePart.contains("::")) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "METHOD_REF_JDK8_REQUIRED",
                            message = "$fileName:$lineNum: error: method references (::) are not supported in -source 1.$major (use -source 8 or higher)",
                            minimumJdkRequired = "JDK 8"
                        )
                    )
                }
                for (jdk8Api in listOf(".stream()", "java.util.stream", "Optional<", "LocalDate", "LocalDateTime", "CompletableFuture")) {
                    if (codePart.contains(jdk8Api)) {
                        diagnostics.add(
                            CompilerDiagnostic(
                                line = lineNum,
                                column = 1,
                                severity = DiagnosticSeverity.ERROR,
                                code = "API_JDK8_REQUIRED",
                                message = "$fileName:$lineNum: error: symbol '$jdk8Api' requires Java SE 8 API (not present in ${jdk.displayName})",
                                minimumJdkRequired = "JDK 8"
                            )
                        )
                    }
                }
            }

            // 7. Java 9 Features: List.of, Map.of, Set.of, module-info
            if (major < 9) {
                if (codePart.contains("List.of(") || codePart.contains("Map.of(") || codePart.contains("Set.of(")) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "COLLECTION_FACTORY_JDK9_REQUIRED",
                            message = "$fileName:$lineNum: error: immutable collection factory methods (List.of/Map.of/Set.of) require JDK 9+",
                            minimumJdkRequired = "JDK 9"
                        )
                    )
                }
            }

            // 8. Java 10 Features: Local variable type inference 'var'
            if (major < 10) {
                if (Regex("""\bvar\s+[a-zA-Z_][a-zA-Z0-9_]*\s*=""").containsMatchIn(codePart)) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "VAR_JDK10_REQUIRED",
                            message = "$fileName:$lineNum: error: 'var' local-variable type inference is not supported in ${jdk.displayName} (requires JDK 10+)",
                            minimumJdkRequired = "JDK 10"
                        )
                    )
                }
            }

            // 9. Java 11 Features: java.net.http.HttpClient, String.repeat/isBlank
            if (major < 11) {
                if (codePart.contains("java.net.http") || codePart.contains("HttpClient.newHttpClient")) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "HTTPCLIENT_JDK11_REQUIRED",
                            message = "$fileName:$lineNum: error: standard java.net.http.HttpClient requires JDK 11+",
                            minimumJdkRequired = "JDK 11"
                        )
                    )
                }
            }

            // 10. Java 14 Features: Switch Expressions (case ... -> or yield)
            if (major < 14) {
                if ((codePart.startsWith("case ") || codePart.startsWith("default")) && codePart.contains("->")) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.ERROR,
                            code = "SWITCH_EXPR_JDK14_REQUIRED",
                            message = "$fileName:$lineNum: error: switch expressions with arrow '->' labels require JDK 14+",
                            minimumJdkRequired = "JDK 14"
                        )
                    )
                }
            }

            // 11. Java 15 Features: Text Blocks """
            if (major < 15 && rawLine.contains("\"\"\"")) {
                diagnostics.add(
                    CompilerDiagnostic(
                        line = lineNum,
                        column = 1,
                        severity = DiagnosticSeverity.ERROR,
                        code = "TEXT_BLOCKS_JDK15_REQUIRED",
                        message = "$fileName:$lineNum: error: text blocks (\"\"\") are not supported in ${jdk.displayName} (requires JDK 15+)",
                        minimumJdkRequired = "JDK 15"
                    )
                )
            }

            // 12. Java 16 Features: Records (record Point(...))
            if (major < 16 && Regex("""\brecord\s+[A-Za-z_][A-Za-z0-9_]*\s*\(""").containsMatchIn(codePart)) {
                diagnostics.add(
                    CompilerDiagnostic(
                        line = lineNum,
                        column = 1,
                        severity = DiagnosticSeverity.ERROR,
                        code = "RECORDS_JDK16_REQUIRED",
                        message = "$fileName:$lineNum: error: records are not supported in ${jdk.displayName} (finalized in JDK 16)",
                        minimumJdkRequired = "JDK 16"
                    )
                )
            }

            // 13. Java 17 Features: Sealed Classes (sealed class ... permits ...)
            if (major < 17 && Regex("""\b(sealed|non-sealed|permits)\b""").containsMatchIn(codePart)) {
                diagnostics.add(
                    CompilerDiagnostic(
                        line = lineNum,
                        column = 1,
                        severity = DiagnosticSeverity.ERROR,
                        code = "SEALED_JDK17_REQUIRED",
                        message = "$fileName:$lineNum: error: sealed classes and interfaces are not supported in ${jdk.displayName} (finalized in JDK 17 LTS)",
                        minimumJdkRequired = "JDK 17"
                    )
                )
            }

            // 14. Java 21 Features: Virtual Threads & SequencedCollection
            if (major < 21 && (codePart.contains("Thread.ofVirtual") || codePart.contains("startVirtualThread") || codePart.contains("SequencedCollection"))) {
                diagnostics.add(
                    CompilerDiagnostic(
                        line = lineNum,
                        column = 1,
                        severity = DiagnosticSeverity.ERROR,
                        code = "VIRTUAL_THREADS_JDK21_REQUIRED",
                        message = "$fileName:$lineNum: error: Virtual Threads (Project Loom) and SequencedCollection require JDK 21 LTS+",
                        minimumJdkRequired = "JDK 21"
                    )
                )
            }

            // 15. Deprecation & Removal Warnings for Modern JDKs
            if (codePart.contains("java.applet") || Regex("""\bextends\s+Applet\b""").containsMatchIn(codePart)) {
                if (major >= 9 && major < 17) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.WARNING,
                            code = "APPLET_DEPRECATED_JDK9",
                            message = "$fileName:$lineNum: warning: [deprecation] Applet in java.applet has been deprecated as of JDK 9",
                            minimumJdkRequired = "JDK 1.0–8"
                        )
                    )
                } else if (major >= 17) {
                    diagnostics.add(
                        CompilerDiagnostic(
                            line = lineNum,
                            column = 1,
                            severity = DiagnosticSeverity.WARNING,
                            code = "APPLET_REMOVAL_JDK17",
                            message = "$fileName:$lineNum: warning: [removal] Applet in java.applet has been deprecated for removal in JDK 17+ (Use JDK 1.2–8 for legacy Applet lab)",
                            minimumJdkRequired = "JDK 1.0–8"
                        )
                    )
                }
            }

            if (major >= 5 && (Regex("""\bnew\s+(Vector|Hashtable|ArrayList|HashMap)\s*\(""").containsMatchIn(codePart)) && !codePart.contains("<")) {
                diagnostics.add(
                    CompilerDiagnostic(
                        line = lineNum,
                        column = 1,
                        severity = DiagnosticSeverity.WARNING,
                        code = "RAW_TYPE_WARNING",
                        message = "$fileName:$lineNum: warning: [rawtypes] found raw type without generic type arguments in ${jdk.displayName}"
                    )
                )
            }
        }
    }

    private fun generateClassArtifacts(
        fileName: String,
        sourceCode: String,
        jdk: JdkInstallationEntity,
        additionalSources: Map<String, String>
    ): List<GeneratedClassArtifact> {
        val majorBytecode = if (jdk.majorNumber <= 1) 45 else (44 + jdk.majorNumber)
        val minorBytecode = if (jdk.majorNumber <= 1) 3 else 0
        val majorHex = majorBytecode.toString(16).uppercase().padStart(4, '0')
        val minorHex = minorBytecode.toString(16).uppercase().padStart(4, '0')
        val hexHeader = "CA FE BA BE $minorHex $majorHex"

        val typeRegex = Regex("""\b(class|interface|enum|record)\s+([A-Za-z_][A-Za-z0-9_]*)""")
        val methodRegex = Regex("""\b(public|protected|private|static|\s)+[\w<>\[\]]+\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*\(([^)]*)\)""")

        val allSources = mutableMapOf(fileName to sourceCode)
        allSources.putAll(additionalSources)

        val artifacts = mutableListOf<GeneratedClassArtifact>()
        for ((srcFile, srcText) in allSources) {
            val types = typeRegex.findAll(srcText).map { it.groupValues[2] }.toList()
            val targetTypes = if (types.isEmpty()) listOf(srcFile.removeSuffix(".java")) else types
            val methods = methodRegex.findAll(srcText)
                .map { "${it.groupValues[2]}(${it.groupValues[3].trim()})" }
                .filter { !it.startsWith("if(") && !it.startsWith("for(") && !it.startsWith("while(") && !it.startsWith("switch(") && !it.startsWith("catch(") }
                .distinct()
                .toList()

            val cpCount = (18 + srcText.lines().size * 2).coerceAtMost(256)
            for (typeName in targetTypes) {
                val disassembly = buildString {
                    appendLine("Compiled from \"$srcFile\"")
                    appendLine("  minor version: $minorBytecode")
                    appendLine("  major version: $majorBytecode (${jdk.versionId})")
                    appendLine("  Magic Header : 0x$hexHeader")
                    appendLine("  Constant pool: $cpCount entries")
                    appendLine("  Methods (${methods.size.coerceAtLeast(1)}):")
                    appendLine("    0: <init>()V [aload_0, invokespecial java/lang/Object.<init>, return]")
                    methods.forEachIndexed { index, sig ->
                        appendLine("    ${index + 1}: $sig [stack=2, locals=3, invokevirtual/ireturn]")
                    }
                }
                artifacts.add(
                    GeneratedClassArtifact(
                        className = typeName,
                        fileName = "$typeName.class",
                        bytecodeMajor = majorBytecode,
                        bytecodeMinor = minorBytecode,
                        hexHeader = hexHeader,
                        detectedMethods = methods.ifEmpty { listOf("main(String[] args)") },
                        constantPoolCount = cpCount,
                        disassemblyPreview = disassembly.trimEnd()
                    )
                )
            }
        }
        return artifacts
    }

    private fun formatCompilerOutput(
        fileName: String,
        jdk: JdkInstallationEntity,
        compilerCmd: String,
        diagnostics: List<CompilerDiagnostic>,
        generatedClasses: List<GeneratedClassArtifact>,
        elapsedMs: Long
    ): String {
        val errors = diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }
        val warnings = diagnostics.filter { it.severity == DiagnosticSeverity.WARNING }
        return buildString {
            appendLine("> $compilerCmd")
            appendLine("Toolchain : ${jdk.displayName} [${jdk.vendor}]")
            appendLine("Target    : Bytecode ${jdk.bytecodeVersion} | Mode: ${jdk.compatibilityMode}")
            appendLine("------------------------------------------------------------")
            if (diagnostics.isNotEmpty()) {
                diagnostics.forEach { d ->
                    val prefix = when (d.severity) {
                        DiagnosticSeverity.ERROR -> "[ERROR]"
                        DiagnosticSeverity.WARNING -> "[WARN] "
                        DiagnosticSeverity.INFO -> "[INFO] "
                    }
                    appendLine("$prefix Line ${d.line}: ${d.message}")
                    if (d.minimumJdkRequired != null) {
                        appendLine("        -> Minimum Required JDK: ${d.minimumJdkRequired} (Current: ${jdk.versionId})")
                    }
                }
                appendLine("------------------------------------------------------------")
            }
            if (errors.isEmpty()) {
                appendLine("BUILD SUCCESSFUL in ${elapsedMs}ms")
                appendLine("Generated ${generatedClasses.size} class file(s): ${generatedClasses.joinToString { "${it.fileName} [0x${it.hexHeader}]" }}")
                if (warnings.isNotEmpty()) {
                    appendLine("${warnings.size} warning(s)")
                }
            } else {
                appendLine("COMPILATION FAILED (${errors.size} error(s), ${warnings.size} warning(s)) in ${elapsedMs}ms")
            }
        }.trimEnd()
    }
}
