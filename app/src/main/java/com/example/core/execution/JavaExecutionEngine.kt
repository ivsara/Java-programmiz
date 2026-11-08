package com.example.core.execution

import com.example.core.compiler.CompilationResult
import com.example.core.compiler.JavaCompilerEngine
import com.example.data.db.JdkInstallationEntity
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

data class ExecutionResult(
    val executed: Boolean,
    val exitCode: Int,
    val runtimeCommand: String,
    val stdout: String,
    val stderr: String,
    val exceptionTrace: String?,
    val executionTimeMs: Long,
    val guiSnapshotTitle: String? = null,
    val guiSnapshotComponents: List<String> = emptyList()
)

private class RuntimeThrownException(
    val exceptionType: String,
    override val message: String,
    val line: Int
) : Exception(message)

private class ReturnSignal(val value: Any?) : Exception()
private class BreakSignal : Exception()
private class ContinueSignal : Exception()

data class ParsedClassDef(
    val name: String,
    val superClass: String?,
    val isRecord: Boolean = false,
    val recordComponents: List<Pair<String, String>> = emptyList(),
    val fields: MutableMap<String, String> = mutableMapOf(), // fieldName -> initialExpr
    val methods: MutableMap<String, MutableList<ParsedMethodDef>> = mutableMapOf(),
    val constructors: MutableList<ParsedMethodDef> = mutableListOf()
)

data class ParsedMethodDef(
    val name: String,
    val params: List<Pair<String, String>>, // (type, name)
    val bodyLines: List<Pair<Int, String>>
)

class JavaObjectInstance(
    val classDef: ParsedClassDef,
    val fields: MutableMap<String, Any?> = mutableMapOf()
) {
    override fun toString(): String {
        return if (classDef.isRecord) {
            val props = classDef.recordComponents.joinToString(", ") { (_, name) ->
                "$name=${fields[name]}"
            }
            "${classDef.name}[$props]"
        } else {
            val customToString = fields["__toString"]
            if (customToString != null) customToString.toString()
            else "${classDef.name}@${Integer.toHexString(hashCode())}"
        }
    }
}

object JavaExecutionEngine {

    private const val MAX_EXECUTION_STEPS = 15_000

    fun compileAndExecute(
        fileName: String,
        sourceCode: String,
        jdk: JdkInstallationEntity,
        stdinInput: String = "",
        programArgs: List<String> = emptyList(),
        additionalProjectSources: Map<String, String> = emptyMap(),
        timeoutMs: Long = 5000L
    ): Pair<CompilationResult, ExecutionResult?> {
        val compileResult = JavaCompilerEngine.compileSource(
            fileName = fileName,
            sourceCode = sourceCode,
            jdk = jdk,
            additionalProjectSources = additionalProjectSources
        )
        if (!compileResult.success) {
            return compileResult to null
        }

        val execResult = executeCompiledProgram(
            mainFileName = fileName,
            mainSourceCode = sourceCode,
            jdk = jdk,
            stdinInput = stdinInput,
            programArgs = programArgs,
            additionalProjectSources = additionalProjectSources,
            timeoutMs = timeoutMs
        )
        return compileResult to execResult
    }

    fun executeCompiledProgram(
        mainFileName: String,
        mainSourceCode: String,
        jdk: JdkInstallationEntity,
        stdinInput: String = "",
        programArgs: List<String> = emptyList(),
        additionalProjectSources: Map<String, String> = emptyMap(),
        timeoutMs: Long = 5000L
    ): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val mainClassName = extractMainClassName(mainFileName, mainSourceCode)
        val argsDisplay = if (programArgs.isEmpty()) "" else " " + programArgs.joinToString(" ")
        val runtimeCmd = "${jdk.runtimePath} -classpath build/classes $mainClassName$argsDisplay"

        val stdoutBuilder = StringBuilder()
        val stderrBuilder = StringBuilder()
        val guiComponents = mutableListOf<String>()
        var guiWindowTitle: String? = null

        val stdinTokens = stdinInput
            .split(Regex("""\s+"""))
            .filter { it.isNotEmpty() }
            .toMutableList()
        val stdinLines = stdinInput
            .lines()
            .filter { it.isNotEmpty() }
            .toMutableList()

        try {
            val allSources = mutableMapOf(mainFileName to mainSourceCode)
            allSources.putAll(additionalProjectSources)

            val classes = parseAllClasses(allSources)
            val entryClass = classes[mainClassName]
                ?: classes.values.firstOrNull { it.methods.containsKey("main") }
                ?: classes.values.firstOrNull()

            if (entryClass == null) {
                val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(3L)
                val err = "Error: Could not find or load main class $mainClassName"
                return ExecutionResult(
                    executed = false,
                    exitCode = 1,
                    runtimeCommand = runtimeCmd,
                    stdout = "",
                    stderr = err,
                    exceptionTrace = err,
                    executionTimeMs = elapsed
                )
            }

            val mainMethods = entryClass.methods["main"]
            val mainMethod = mainMethods?.firstOrNull()
            if (mainMethod == null) {
                // Check if it's an Applet
                if (mainSourceCode.contains("extends Applet") || mainSourceCode.contains("extends JApplet")) {
                    stdoutBuilder.appendLine("[AppletViewer ${jdk.versionId}] Initializing Applet: ${entryClass.name}")
                    stdoutBuilder.appendLine("[AppletViewer] Calling init() -> start() -> paint(Graphics g)")
                    val paintMethod = entryClass.methods["paint"]?.firstOrNull()
                    val initMethod = entryClass.methods["init"]?.firstOrNull()
                    val env = InterpreterContext(
                        classes = classes,
                        stdout = stdoutBuilder,
                        stderr = stderrBuilder,
                        stdinTokens = stdinTokens,
                        stdinLines = stdinLines,
                        startTimeMs = startTime,
                        timeoutMs = timeoutMs,
                        guiComponents = guiComponents,
                        onGuiTitle = { guiWindowTitle = it }
                    )
                    val appletScope = mutableMapOf<String, Any?>()
                    if (initMethod != null) env.executeBlock(initMethod.bodyLines, appletScope, entryClass)
                    if (paintMethod != null) env.executeBlock(paintMethod.bodyLines, appletScope, entryClass)
                    val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(5L)
                    return ExecutionResult(
                        executed = true,
                        exitCode = 0,
                        runtimeCommand = "${jdk.javaHome}/bin/appletviewer $mainFileName",
                        stdout = stdoutBuilder.toString().trimEnd(),
                        stderr = stderrBuilder.toString().trimEnd(),
                        exceptionTrace = null,
                        executionTimeMs = elapsed,
                        guiSnapshotTitle = guiWindowTitle ?: "AppletViewer: ${entryClass.name}",
                        guiSnapshotComponents = guiComponents
                    )
                }

                val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(3L)
                val err = "Error: Main method not found in class ${entryClass.name}, please define the main method as:\n   public static void main(String[] args)"
                return ExecutionResult(
                    executed = false,
                    exitCode = 1,
                    runtimeCommand = runtimeCmd,
                    stdout = "",
                    stderr = err,
                    exceptionTrace = err,
                    executionTimeMs = elapsed
                )
            }

            val env = InterpreterContext(
                classes = classes,
                stdout = stdoutBuilder,
                stderr = stderrBuilder,
                stdinTokens = stdinTokens,
                stdinLines = stdinLines,
                startTimeMs = startTime,
                timeoutMs = timeoutMs,
                guiComponents = guiComponents,
                onGuiTitle = { guiWindowTitle = it }
            )

            val rootScope = mutableMapOf<String, Any?>()
            val argParamName = mainMethod.params.firstOrNull()?.second ?: "args"
            rootScope[argParamName] = programArgs.toMutableList()

            // Initialize static fields of entryClass
            for ((fName, initExpr) in entryClass.fields) {
                rootScope[fName] = if (initExpr.isNotBlank()) env.evalExpr(initExpr, rootScope, entryClass, 1) else 0
            }

            try {
                env.executeBlock(mainMethod.bodyLines, rootScope, entryClass)
            } catch (_: ReturnSignal) {
                // Normal return from main
            }

            val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(5L)
            return ExecutionResult(
                executed = true,
                exitCode = 0,
                runtimeCommand = runtimeCmd,
                stdout = stdoutBuilder.toString().trimEnd(),
                stderr = stderrBuilder.toString().trimEnd(),
                exceptionTrace = null,
                executionTimeMs = elapsed,
                guiSnapshotTitle = guiWindowTitle,
                guiSnapshotComponents = guiComponents
            )
        } catch (rte: RuntimeThrownException) {
            val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(5L)
            val trace = buildString {
                appendLine("Exception in thread \"main\" ${rte.exceptionType}: ${rte.message}")
                appendLine("\tat $mainClassName.main($mainFileName:${rte.line})")
            }.trimEnd()
            stderrBuilder.appendLine(trace)
            return ExecutionResult(
                executed = true,
                exitCode = 1,
                runtimeCommand = runtimeCmd,
                stdout = stdoutBuilder.toString().trimEnd(),
                stderr = stderrBuilder.toString().trimEnd(),
                exceptionTrace = trace,
                executionTimeMs = elapsed,
                guiSnapshotTitle = guiWindowTitle,
                guiSnapshotComponents = guiComponents
            )
        } catch (e: Exception) {
            val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(5L)
            val trace = "Exception in thread \"main\" java.lang.RuntimeException: ${e.message ?: "Execution error"}\n\tat $mainClassName.main($mainFileName:1)"
            stderrBuilder.appendLine(trace)
            return ExecutionResult(
                executed = true,
                exitCode = 1,
                runtimeCommand = runtimeCmd,
                stdout = stdoutBuilder.toString().trimEnd(),
                stderr = stderrBuilder.toString().trimEnd(),
                exceptionTrace = trace,
                executionTimeMs = elapsed
            )
        }
    }

    private fun extractMainClassName(fileName: String, sourceCode: String): String {
        val publicClassMatch = Regex("""\bpublic\s+class\s+([A-Za-z_][A-Za-z0-9_]*)""").find(sourceCode)
        if (publicClassMatch != null) return publicClassMatch.groupValues[1]
        val anyClassMatch = Regex("""\b(class|record)\s+([A-Za-z_][A-Za-z0-9_]*)""").find(sourceCode)
        if (anyClassMatch != null) return anyClassMatch.groupValues[2]
        return fileName.removeSuffix(".java")
    }

    private fun parseAllClasses(sources: Map<String, String>): Map<String, ParsedClassDef> {
        val result = mutableMapOf<String, ParsedClassDef>()

        for ((fileName, rawSource) in sources) {
            val cleanedLines = stripCommentsPreservingLines(rawSource)

            // 1. Parse any single-line record definitions: record Name(int x, String y) { ... }
            val recordRegex = Regex("""\brecord\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(([^)]*)\)""")
            for (match in recordRegex.findAll(rawSource)) {
                val recName = match.groupValues[1]
                val paramsRaw = match.groupValues[2].trim()
                val components = if (paramsRaw.isBlank()) emptyList() else {
                    paramsRaw.split(",").mapNotNull { p ->
                        val parts = p.trim().split(Regex("""\s+"""))
                        if (parts.size >= 2) parts[parts.size - 2] to parts.last() else null
                    }
                }
                val recDef = ParsedClassDef(
                    name = recName,
                    superClass = "Record",
                    isRecord = true,
                    recordComponents = components
                )
                result[recName] = recDef
            }

            // 2. Parse class declarations and their bodies
            var i = 0
            while (i < cleanedLines.size) {
                val line = cleanedLines[i].trim()
                val classMatch = Regex("""\bclass\s+([A-Za-z_][A-Za-z0-9_]*)(?:\s+extends\s+([A-Za-z0-9_.]+))?""").find(line)
                if (classMatch != null && !line.startsWith("//")) {
                    val className = classMatch.groupValues[1]
                    val superName = classMatch.groupValues[2].takeIf { it.isNotBlank() }
                    val blockEnd = findMatchingBraceLine(cleanedLines, i)
                    val classLines = cleanedLines.subList(i + 1, blockEnd.coerceAtMost(cleanedLines.size))
                    val classDef = parseSingleClassBody(className, superName, classLines, i + 2)
                    result[className] = classDef
                    i = blockEnd + 1
                } else {
                    i++
                }
            }

            // 3. Support JDK 25 compact source file with top-level main()
            if (result.isEmpty()) {
                val syntheticName = fileName.removeSuffix(".java")
                val classDef = parseSingleClassBody(syntheticName, null, cleanedLines, 1)
                result[syntheticName] = classDef
            }
        }
        return result
    }

    private fun parseSingleClassBody(
        className: String,
        superClass: String?,
        classLines: List<String>,
        startLineOffset: Int
    ): ParsedClassDef {
        val classDef = ParsedClassDef(name = className, superClass = superClass)
        var idx = 0
        while (idx < classLines.size) {
            val raw = classLines[idx].trim()
            val actualLineNum = startLineOffset + idx
            if (raw.isEmpty() || raw.startsWith("@")) {
                idx++
                continue
            }

            // Check if constructor: ClassName(...) {
            val ctorRegex = Regex("""^(?:public|protected|private)?\s*$className\s*\(([^)]*)\)\s*\{?""")
            val ctorMatch = ctorRegex.find(raw)
            if (ctorMatch != null && !raw.endsWith(";")) {
                val params = parseParamList(ctorMatch.groupValues[1])
                val endIdx = findMatchingBraceLine(classLines, idx)
                val body = mutableListOf<Pair<Int, String>>()
                for (b in (idx + 1) until endIdx.coerceAtMost(classLines.size)) {
                    body.add((startLineOffset + b) to classLines[b])
                }
                classDef.constructors.add(ParsedMethodDef(className, params, body))
                idx = endIdx + 1
                continue
            }

            // Check if method declaration
            val methodRegex = Regex("""^(?:public|protected|private|static|final|synchronized|\s)*[\w<>\[\]]+\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*\(([^)]*)\)(?:\s*throws\s+[\w,\s]+)?\s*\{?""")
            val methodMatch = methodRegex.find(raw)
            if (methodMatch != null && !raw.endsWith(";") && !raw.startsWith("new ")) {
                val methodName = methodMatch.groupValues[1]
                if (methodName !in listOf("if", "for", "while", "switch", "catch")) {
                    val params = parseParamList(methodMatch.groupValues[2])
                    val endIdx = findMatchingBraceLine(classLines, idx)
                    val body = mutableListOf<Pair<Int, String>>()
                    for (b in (idx + 1) until endIdx.coerceAtMost(classLines.size)) {
                        body.add((startLineOffset + b) to classLines[b])
                    }
                    classDef.methods.getOrPut(methodName) { mutableListOf() }
                        .add(ParsedMethodDef(methodName, params, body))
                    idx = endIdx + 1
                    continue
                }
            }

            // Check if field declaration
            if (raw.endsWith(";")) {
                val fieldClean = raw.removeSuffix(";").trim()
                val fieldRegex = Regex("""^(?:public|protected|private|static|final|\s)*([\w<>\[\]]+)\s+([a-zA-Z_][a-zA-Z0-9_]*)(?:\s*=\s*(.*))?$""")
                val fm = fieldRegex.find(fieldClean)
                if (fm != null) {
                    val fName = fm.groupValues[2]
                    val fInit = fm.groupValues[3].trim()
                    classDef.fields[fName] = fInit
                }
            }
            idx++
        }
        return classDef
    }

    private fun parseParamList(paramsRaw: String): List<Pair<String, String>> {
        val trimmed = paramsRaw.trim()
        if (trimmed.isEmpty()) return emptyList()
        return trimmed.split(",").mapNotNull { part ->
            val p = part.trim().replace("final ", "")
            val tokens = p.split(Regex("""\s+"""))
            if (tokens.size >= 2) {
                val name = tokens.last().removeSuffix("[]")
                val type = tokens.dropLast(1).joinToString(" ")
                type to name
            } else null
        }
    }

    private fun stripCommentsPreservingLines(source: String): List<String> {
        val rawCommentsStripped = mutableListOf<String>()
        var inBlockComment = false
        for (line in source.lines()) {
            val sb = StringBuilder()
            var i = 0
            var inString = false
            while (i < line.length) {
                if (!inBlockComment && line[i] == '"' && (i == 0 || line[i - 1] != '\\')) {
                    inString = !inString
                    sb.append(line[i])
                    i++
                    continue
                }
                if (!inString && !inBlockComment && i + 1 < line.length && line[i] == '/' && line[i + 1] == '/') {
                    break
                }
                if (!inString && !inBlockComment && i + 1 < line.length && line[i] == '/' && line[i + 1] == '*') {
                    inBlockComment = true
                    i += 2
                    continue
                }
                if (inBlockComment) {
                    if (i + 1 < line.length && line[i] == '*' && line[i + 1] == '/') {
                        inBlockComment = false
                        i += 2
                    } else {
                        i++
                    }
                    continue
                }
                sb.append(line[i])
                i++
            }
            rawCommentsStripped.add(sb.toString())
        }

        // Normalize single-line class/method blocks into structured lines
        val normalized = mutableListOf<String>()
        for (line in rawCommentsStripped) {
            val trimmed = line.trim()
            if (trimmed.startsWith("record ") && trimmed.endsWith("{}")) {
                normalized.add(trimmed)
                continue
            }
            val current = StringBuilder()
            var inStr = false
            var parenDepth = 0
            for (idx in line.indices) {
                val c = line[idx]
                if (c == '"' && (idx == 0 || line[idx - 1] != '\\')) inStr = !inStr
                if (!inStr) {
                    if (c == '(') parenDepth++
                    else if (c == ')') parenDepth = (parenDepth - 1).coerceAtLeast(0)
                }
                if (!inStr && parenDepth == 0 && c == '{') {
                    val before = current.toString().trim()
                    // Keep array initializer {1, 2, 3} on same line
                    if (before.endsWith("=") || before.endsWith("[]")) {
                        current.append(c)
                    } else {
                        current.append(" {")
                        normalized.add(current.toString())
                        current.clear()
                    }
                } else if (!inStr && parenDepth == 0 && c == '}') {
                    val before = current.toString().trim()
                    if (before.isNotEmpty() && !before.contains(";") && current.toString().contains("{")) {
                        // Closing an inline array initializer
                        current.append(c)
                    } else {
                        if (before.isNotEmpty()) {
                            normalized.add(before)
                            current.clear()
                        }
                        normalized.add("}")
                    }
                } else if (!inStr && parenDepth == 0 && c == ';') {
                    current.append(';')
                    normalized.add(current.toString().trim())
                    current.clear()
                } else {
                    current.append(c)
                }
            }
            val rem = current.toString().trim()
            if (rem.isNotEmpty()) {
                normalized.add(rem)
            }
        }
        return normalized
    }

    private fun findMatchingBraceLine(lines: List<String>, startIdx: Int): Int {
        var depth = 0
        var foundOpen = false
        for (i in startIdx until lines.size) {
            val line = lines[i]
            var inStr = false
            for ( j in line.indices) {
                val c = line[j]
                if (c == '"' && (j == 0 || line[j - 1] != '\\')) inStr = !inStr
                if (!inStr) {
                    if (c == '{') {
                        depth++
                        foundOpen = true
                    } else if (c == '}') {
                        depth--
                        if (foundOpen && depth == 0) return i
                    }
                }
            }
        }
        return lines.size - 1
    }

    private class InterpreterContext(
        val classes: Map<String, ParsedClassDef>,
        val stdout: StringBuilder,
        val stderr: StringBuilder,
        val stdinTokens: MutableList<String>,
        val stdinLines: MutableList<String>,
        val startTimeMs: Long,
        val timeoutMs: Long,
        val guiComponents: MutableList<String>,
        val onGuiTitle: (String) -> Unit
    ) {
        var stepCount = 0

        private fun checkLimits(lineNum: Int) {
            stepCount++
            if (stepCount > MAX_EXECUTION_STEPS) {
                throw RuntimeThrownException(
                    "java.util.concurrent.TimeoutException",
                    "Execution exceeded maximum instruction step limit ($MAX_EXECUTION_STEPS steps) — possible infinite loop",
                    lineNum
                )
            }
            if (System.currentTimeMillis() - startTimeMs > timeoutMs) {
                throw RuntimeThrownException(
                    "java.util.concurrent.TimeoutException",
                    "Process terminated after exceeding timeout of ${timeoutMs}ms",
                    lineNum
                )
            }
        }

        fun executeBlock(
            lines: List<Pair<Int, String>>,
            scope: MutableMap<String, Any?>,
            currentClass: ParsedClassDef,
            thisInstance: JavaObjectInstance? = null
        ) {
            var idx = 0
            while (idx < lines.size) {
                val (lineNum, rawLine) = lines[idx]
                checkLimits(lineNum)
                val trimmed = rawLine.trim()

                if (trimmed.isEmpty() || trimmed == "{" || trimmed == "}" || trimmed.startsWith("@")) {
                    idx++
                    continue
                }

                // 1. Handle try / catch / finally
                if (trimmed.startsWith("try") && (trimmed.endsWith("{") || trimmed.contains("{"))) {
                    val tryEnd = findSubBlockEnd(lines, idx)
                    val tryBody = lines.subList(idx + 1, tryEnd)
                    var nextIdx = tryEnd + 1

                    var catchVar = "e"
                    var catchBody: List<Pair<Int, String>>? = null
                    var finallyBody: List<Pair<Int, String>>? = null

                    // Check catch on tryEnd line or nextIdx
                    val tryEndTrimmed = lines.getOrNull(tryEnd)?.second?.trim() ?: ""
                    if (tryEndTrimmed.contains("catch")) {
                        val cm = Regex("""catch\s*\(([^)]+)\)\s*\{""").find(tryEndTrimmed)
                        if (cm != null) {
                            catchVar = cm.groupValues[1].trim().split(Regex("""\s+""")).last()
                        }
                        val catchEnd = findSubBlockEnd(lines, tryEnd)
                        catchBody = lines.subList(tryEnd + 1, catchEnd)
                        nextIdx = catchEnd + 1
                        val catchEndTrimmed = lines.getOrNull(catchEnd)?.second?.trim() ?: ""
                        if (catchEndTrimmed.contains("finally")) {
                            val finEnd = findSubBlockEnd(lines, catchEnd)
                            finallyBody = lines.subList(catchEnd + 1, finEnd)
                            nextIdx = finEnd + 1
                        }
                    } else if (nextIdx < lines.size && lines[nextIdx].second.trim().startsWith("catch")) {
                        val cm = Regex("""catch\s*\(([^)]+)\)""").find(lines[nextIdx].second)
                        if (cm != null) {
                            catchVar = cm.groupValues[1].trim().split(Regex("""\s+""")).last()
                        }
                        val catchEnd = findSubBlockEnd(lines, nextIdx)
                        catchBody = lines.subList(nextIdx + 1, catchEnd)
                        nextIdx = catchEnd + 1
                    }

                    if (nextIdx < lines.size && lines[nextIdx].second.trim().startsWith("finally")) {
                        val finEnd = findSubBlockEnd(lines, nextIdx)
                        finallyBody = lines.subList(nextIdx + 1, finEnd)
                        nextIdx = finEnd + 1
                    }

                    try {
                        executeBlock(tryBody, scope, currentClass, thisInstance)
                    } catch (rte: RuntimeThrownException) {
                        if (catchBody != null) {
                            val catchScope = scope.toMutableMap()
                            catchScope[catchVar] = rte.message
                            executeBlock(catchBody, catchScope, currentClass, thisInstance)
                            scope.keys.forEach { k -> if (catchScope.containsKey(k)) scope[k] = catchScope[k] }
                        } else {
                            throw rte
                        }
                    } finally {
                        if (finallyBody != null) {
                            executeBlock(finallyBody, scope, currentClass, thisInstance)
                        }
                    }
                    idx = nextIdx
                    continue
                }

                // 2. Handle if / else if / else
                if (trimmed.startsWith("if ") || trimmed.startsWith("if(")) {
                    idx = executeIfChain(lines, idx, scope, currentClass, thisInstance)
                    continue
                }

                // 3. Handle for loop (standard & enhanced for-each)
                if (trimmed.startsWith("for ") || trimmed.startsWith("for(")) {
                    val header = extractParenContent(trimmed)
                    val hasBlock = trimmed.endsWith("{") || (idx + 1 < lines.size && lines[idx + 1].second.trim().startsWith("{"))
                    val bodyEnd = if (hasBlock) findSubBlockEnd(lines, idx) else idx + 1
                    val bodyLines = if (hasBlock) {
                        val startOffset = if (trimmed.endsWith("{")) idx + 1 else idx + 2
                        lines.subList(startOffset, bodyEnd)
                    } else {
                        listOf(lines[idx + 1])
                    }

                    if (header.contains(":") && !header.contains(";")) {
                        // Enhanced for-each: Type varName : collectionExpr
                        val lhs = header.substringBefore(":").trim()
                        val rhs = header.substringAfter(":").trim()
                        val varName = lhs.split(Regex("""\s+""")).last()
                        val iterableVal = evalExpr(rhs, scope, currentClass, lineNum, thisInstance)
                        val items: List<Any?> = when (iterableVal) {
                            is List<*> -> iterableVal
                            is Array<*> -> iterableVal.toList()
                            is IntArray -> iterableVal.toList()
                            is DoubleArray -> iterableVal.toList()
                            is String -> iterableVal.toList()
                            else -> emptyList()
                        }
                        for (item in items) {
                            scope[varName] = item
                            try {
                                executeBlock(bodyLines, scope, currentClass, thisInstance)
                            } catch (_: ContinueSignal) {
                                continue
                            } catch (_: BreakSignal) {
                                break
                            }
                        }
                    } else {
                        // Classic for (init; cond; step)
                        val parts = header.split(";")
                        val initStmt = parts.getOrNull(0)?.trim() ?: ""
                        val condExpr = parts.getOrNull(1)?.trim() ?: "true"
                        val stepStmt = parts.getOrNull(2)?.trim() ?: ""

                        if (initStmt.isNotEmpty()) {
                            executeSingleStatement(initStmt, scope, currentClass, lineNum, thisInstance)
                        }
                        while (condExpr.isEmpty() || toBoolean(evalExpr(condExpr, scope, currentClass, lineNum, thisInstance))) {
                            checkLimits(lineNum)
                            try {
                                executeBlock(bodyLines, scope, currentClass, thisInstance)
                            } catch (_: ContinueSignal) {
                                // proceed to step
                            } catch (_: BreakSignal) {
                                break
                            }
                            if (stepStmt.isNotEmpty()) {
                                executeSingleStatement(stepStmt, scope, currentClass, lineNum, thisInstance)
                            }
                        }
                    }
                    idx = bodyEnd + 1
                    continue
                }

                // 4. Handle while loop
                if (trimmed.startsWith("while ") || trimmed.startsWith("while(")) {
                    val condExpr = extractParenContent(trimmed)
                    val bodyEnd = findSubBlockEnd(lines, idx)
                    val bodyLines = lines.subList(idx + 1, bodyEnd)
                    while (toBoolean(evalExpr(condExpr, scope, currentClass, lineNum, thisInstance))) {
                        checkLimits(lineNum)
                        try {
                            executeBlock(bodyLines, scope, currentClass, thisInstance)
                        } catch (_: ContinueSignal) {
                            continue
                        } catch (_: BreakSignal) {
                            break
                        }
                    }
                    idx = bodyEnd + 1
                    continue
                }

                // 5. Handle do-while loop
                if (trimmed == "do" || trimmed.startsWith("do ") || trimmed.startsWith("do{")) {
                    val bodyEnd = findSubBlockEnd(lines, idx)
                    val bodyLines = lines.subList(idx + 1, bodyEnd)
                    val whileLine = lines.getOrNull(bodyEnd)?.second?.trim() ?: ""
                    val condExpr = if (whileLine.contains("while")) extractParenContent(whileLine) else "false"
                    do {
                        checkLimits(lineNum)
                        try {
                            executeBlock(bodyLines, scope, currentClass, thisInstance)
                        } catch (_: ContinueSignal) {
                            // continue to condition
                        } catch (_: BreakSignal) {
                            break
                        }
                    } while (toBoolean(evalExpr(condExpr, scope, currentClass, lineNum, thisInstance)))
                    idx = bodyEnd + 1
                    continue
                }

                // 6. Handle switch block
                if (trimmed.startsWith("switch ") || trimmed.startsWith("switch(")) {
                    val switchExpr = extractParenContent(trimmed)
                    val switchVal = evalExpr(switchExpr, scope, currentClass, lineNum, thisInstance)
                    val switchEnd = findSubBlockEnd(lines, idx)
                    val switchLines = lines.subList(idx + 1, switchEnd)
                    executeSwitchBlock(switchVal, switchLines, scope, currentClass, thisInstance)
                    idx = switchEnd + 1
                    continue
                }

                // 7. Handle Thread / VirtualThread inline runnable
                if (trimmed.contains("new Thread(") || trimmed.contains("Thread.ofVirtual()")) {
                    if (trimmed.endsWith("{")) {
                        val endThreadBlock = findSubBlockEnd(lines, idx)
                        val threadBody = lines.subList(idx + 1, endThreadBlock)
                        executeBlock(threadBody, scope, currentClass, thisInstance)
                        idx = endThreadBlock + 1
                        continue
                    }
                }

                // 8. Handle multi-line stream pipeline or statement ending with ';'
                var combinedStmt = trimmed
                var lookahead = idx
                while (!combinedStmt.endsWith(";") && !combinedStmt.endsWith("{") && !combinedStmt.endsWith("}") && lookahead + 1 < lines.size) {
                    lookahead++
                    combinedStmt += " " + lines[lookahead].second.trim()
                }
                idx = lookahead

                val cleanStmt = combinedStmt.removeSuffix(";").trim()
                executeSingleStatement(cleanStmt, scope, currentClass, lineNum, thisInstance)
                idx++
            }
        }

        private fun executeIfChain(
            lines: List<Pair<Int, String>>,
            startIdx: Int,
            scope: MutableMap<String, Any?>,
            currentClass: ParsedClassDef,
            thisInstance: JavaObjectInstance?
        ): Int {
            var currIdx = startIdx
            var branchTaken = false

            while (currIdx < lines.size) {
                val (lineNum, raw) = lines[currIdx]
                val trimmed = raw.trim().removePrefix("}").trim()

                val isIf = trimmed.startsWith("if ") || trimmed.startsWith("if(")
                val isElseIf = trimmed.startsWith("else if")
                val isElse = trimmed.startsWith("else") && !isElseIf

                if (!isIf && !isElseIf && !isElse) break
                if (currIdx > startIdx && isIf) break

                val blockEnd = findSubBlockEnd(lines, currIdx)
                val bodyLines = lines.subList(currIdx + 1, blockEnd)

                if (!branchTaken) {
                    val condMet = if (isElse) {
                        true
                    } else {
                        val condExpr = extractParenContent(trimmed)
                        toBoolean(evalExpr(condExpr, scope, currentClass, lineNum, thisInstance))
                    }
                    if (condMet) {
                        executeBlock(bodyLines, scope, currentClass, thisInstance)
                        branchTaken = true
                    }
                }

                val endLineTrimmed = lines.getOrNull(blockEnd)?.second?.trim() ?: ""
                if (endLineTrimmed.contains("else")) {
                    currIdx = blockEnd
                } else {
                    val nextLineTrimmed = lines.getOrNull(blockEnd + 1)?.second?.trim() ?: ""
                    if (nextLineTrimmed.startsWith("else")) {
                        currIdx = blockEnd + 1
                    } else {
                        return blockEnd + 1
                    }
                }
            }
            return currIdx + 1
        }

        private fun executeSwitchBlock(
            switchVal: Any?,
            switchLines: List<Pair<Int, String>>,
            scope: MutableMap<String, Any?>,
            currentClass: ParsedClassDef,
            thisInstance: JavaObjectInstance?
        ) {
            var matchingStarted = false
            for ((lineNum, raw) in switchLines) {
                val t = raw.trim()
                if (t.startsWith("case ") && t.contains("->")) {
                    val labelsRaw = t.substringAfter("case ").substringBefore("->").trim()
                    val rhsStmt = t.substringAfter("->").trim().removeSuffix(";")
                    val labels = labelsRaw.split(",").map { evalExpr(it.trim(), scope, currentClass, lineNum, thisInstance) }
                    if (labels.any { valuesEqual(it, switchVal) }) {
                        executeSingleStatement(rhsStmt, scope, currentClass, lineNum, thisInstance)
                        return
                    }
                } else if (t.startsWith("default") && t.contains("->")) {
                    val rhsStmt = t.substringAfter("->").trim().removeSuffix(";")
                    executeSingleStatement(rhsStmt, scope, currentClass, lineNum, thisInstance)
                    return
                } else if (t.startsWith("case ") && t.contains(":")) {
                    val labelExpr = t.substringAfter("case ").substringBefore(":").trim()
                    val labelVal = evalExpr(labelExpr, scope, currentClass, lineNum, thisInstance)
                    if (valuesEqual(labelVal, switchVal)) {
                        matchingStarted = true
                        val afterColon = t.substringAfter(":").trim().removeSuffix(";")
                        if (afterColon.isNotEmpty()) {
                            if (afterColon == "break") return
                            executeSingleStatement(afterColon, scope, currentClass, lineNum, thisInstance)
                        }
                    }
                } else if (t.startsWith("default:")) {
                    matchingStarted = true
                    val afterColon = t.substringAfter(":").trim().removeSuffix(";")
                    if (afterColon.isNotEmpty() && afterColon != "break") {
                        executeSingleStatement(afterColon, scope, currentClass, lineNum, thisInstance)
                    }
                } else if (matchingStarted) {
                    if (t == "break;" || t == "break") return
                    val clean = t.removeSuffix(";").trim()
                    if (clean.isNotEmpty()) {
                        executeSingleStatement(clean, scope, currentClass, lineNum, thisInstance)
                    }
                }
            }
        }

        private fun findSubBlockEnd(lines: List<Pair<Int, String>>, startIdx: Int): Int {
            var depth = 0
            var opened = false
            for (i in startIdx until lines.size) {
                val text = lines[i].second
                var inStr = false
                for (j in text.indices) {
                    val c = text[j]
                    if (c == '"' && (j == 0 || text[j - 1] != '\\')) inStr = !inStr
                    if (!inStr) {
                        if (c == '{') {
                            depth++
                            opened = true
                        } else if (c == '}') {
                            depth--
                            if (opened && depth == 0) return i
                        }
                    }
                }
            }
            return lines.size - 1
        }

        fun executeSingleStatement(
            stmt: String,
            scope: MutableMap<String, Any?>,
            currentClass: ParsedClassDef,
            lineNum: Int,
            thisInstance: JavaObjectInstance?
        ) {
            val s = stmt.trim()
            if (s.isEmpty() || s.startsWith("package ") || s.startsWith("import ")) return

            if (s == "break") throw BreakSignal()
            if (s == "continue") throw ContinueSignal()
            if (s == "return") throw ReturnSignal(null)
            if (s.startsWith("return ")) {
                val retVal = evalExpr(s.removePrefix("return ").trim(), scope, currentClass, lineNum, thisInstance)
                throw ReturnSignal(retVal)
            }

            // assert statement
            if (s.startsWith("assert ")) {
                val rest = s.removePrefix("assert ").trim()
                val condPart = rest.substringBefore(":").trim()
                val msgPart = if (rest.contains(":")) rest.substringAfter(":").trim() else "\"Assertion failed\""
                if (!toBoolean(evalExpr(condPart, scope, currentClass, lineNum, thisInstance))) {
                    val msg = evalExpr(msgPart, scope, currentClass, lineNum, thisInstance).toString()
                    throw RuntimeThrownException("java.lang.AssertionError", msg, lineNum)
                }
                return
            }

            // throw new Exception(...)
            if (s.startsWith("throw ")) {
                val thrownExpr = s.removePrefix("throw ").trim()
                val exMatch = Regex("""new\s+([A-Za-z0-9_.]+)\s*\((.*)\)""").find(thrownExpr)
                if (exMatch != null) {
                    val exType = exMatch.groupValues[1]
                    val msgExpr = exMatch.groupValues[2].trim()
                    val msg = if (msgExpr.isEmpty()) "" else evalExpr(msgExpr, scope, currentClass, lineNum, thisInstance).toString()
                    throw RuntimeThrownException(exType, msg, lineNum)
                }
                throw RuntimeThrownException("java.lang.RuntimeException", thrownExpr, lineNum)
            }

            // System.out.println / print / printf & System.err.println
            if (s.startsWith("System.out.println(") && s.endsWith(")")) {
                val argExpr = s.removePrefix("System.out.println(").removeSuffix(")").trim()
                val outVal = if (argExpr.isEmpty()) "" else formatJavaValue(evalExpr(argExpr, scope, currentClass, lineNum, thisInstance))
                stdout.appendLine(outVal)
                return
            }
            if (s.startsWith("System.out.print(") && s.endsWith(")")) {
                val argExpr = s.removePrefix("System.out.print(").removeSuffix(")").trim()
                val outVal = if (argExpr.isEmpty()) "" else formatJavaValue(evalExpr(argExpr, scope, currentClass, lineNum, thisInstance))
                stdout.append(outVal)
                return
            }
            if (s.startsWith("System.out.printf(") && s.endsWith(")")) {
                val inside = s.removePrefix("System.out.printf(").removeSuffix(")").trim()
                val args = splitTopLevelArgs(inside)
                if (args.isNotEmpty()) {
                    val fmt = evalExpr(args[0], scope, currentClass, lineNum, thisInstance).toString().replace("\\n", "\n")
                    val fmtArgs = args.drop(1).map { evalExpr(it, scope, currentClass, lineNum, thisInstance) }.toTypedArray()
                    try {
                        stdout.append(String.format(Locale.US, fmt, *fmtArgs))
                    } catch (_: Exception) {
                        stdout.append(fmt + " " + fmtArgs.joinToString(" "))
                    }
                }
                return
            }
            if (s.startsWith("System.err.println(") && s.endsWith(")")) {
                val argExpr = s.removePrefix("System.err.println(").removeSuffix(")").trim()
                val outVal = if (argExpr.isEmpty()) "" else formatJavaValue(evalExpr(argExpr, scope, currentClass, lineNum, thisInstance))
                stderr.appendLine(outVal)
                return
            }

            // GUI / AWT / Swing inspection support
            if (s.contains(".setTitle(")) {
                val titleExpr = extractParenContent(s)
                onGuiTitle(evalExpr(titleExpr, scope, currentClass, lineNum, thisInstance).toString())
                return
            }
            if (s.contains(".add(") && !isCollectionVar(s.substringBefore(".add(").trim(), scope)) {
                val compExpr = extractParenContent(s)
                val compVal = evalExpr(compExpr, scope, currentClass, lineNum, thisInstance)
                guiComponents.add(compVal.toString())
                return
            }
            if (s.contains("g.drawString(")) {
                val args = splitTopLevelArgs(extractParenContent(s))
                if (args.isNotEmpty()) {
                    val text = evalExpr(args[0], scope, currentClass, lineNum, thisInstance).toString()
                    guiComponents.add("Canvas Text: \"$text\"")
                    stdout.appendLine("[AWT Graphics] drawString(\"$text\")")
                }
                return
            }

            // Increment / Decrement statements: i++, ++i, i--, --i
            if (s.endsWith("++") || s.startsWith("++") || s.endsWith("--") || s.startsWith("--")) {
                val isInc = s.contains("++")
                val varName = s.replace("++", "").replace("--", "").trim()
                val current = toNumber(scope[varName] ?: thisInstance?.fields?.get(varName) ?: 0)
                val next = if (current is Double || current is Float) current.toDouble() + (if (isInc) 1.0 else -1.0)
                else current.toLong() + (if (isInc) 1L else -1L)
                assignVar(varName, next, scope, thisInstance)
                return
            }

            // Compound assignments: +=, -=, *=, /=
            for (op in listOf("+=", "-=", "*=", "/=")) {
                if (s.contains(op)) {
                    val lhs = s.substringBefore(op).trim()
                    val rhs = s.substringAfter(op).trim()
                    val curVal = evalExpr(lhs, scope, currentClass, lineNum, thisInstance)
                    val rhsVal = evalExpr(rhs, scope, currentClass, lineNum, thisInstance)
                    val updated = when (op) {
                        "+=" -> if (curVal is String || rhsVal is String) "${formatJavaValue(curVal)}${formatJavaValue(rhsVal)}"
                        else evalMathOp(curVal, rhsVal, "+", lineNum)
                        "-=" -> evalMathOp(curVal, rhsVal, "-", lineNum)
                        "*=" -> evalMathOp(curVal, rhsVal, "*", lineNum)
                        "/=" -> evalMathOp(curVal, rhsVal, "/", lineNum)
                        else -> curVal
                    }
                    assignVar(lhs, updated, scope, thisInstance)
                    return
                }
            }

            // Array element assignment: arr[idx] = val
            val arrAssignMatch = Regex("""^([a-zA-Z_][a-zA-Z0-9_]*)\[([^\]]+)\]\s*=\s*(.+)$""").find(s)
            if (arrAssignMatch != null) {
                val arrName = arrAssignMatch.groupValues[1]
                val idxExpr = arrAssignMatch.groupValues[2]
                val valExpr = arrAssignMatch.groupValues[3]
                val idxVal = toNumber(evalExpr(idxExpr, scope, currentClass, lineNum, thisInstance)).toInt()
                val newVal = evalExpr(valExpr, scope, currentClass, lineNum, thisInstance)
                val list = scope[arrName] as? MutableList<Any?>
                    ?: throw RuntimeThrownException("java.lang.NullPointerException", "Cannot store to array '$arrName' because it is null", lineNum)
                if (idxVal < 0 || idxVal >= list.size) {
                    throw RuntimeThrownException(
                        "java.lang.ArrayIndexOutOfBoundsException",
                        "Index $idxVal out of bounds for length ${list.size}",
                        lineNum
                    )
                }
                list[idxVal] = newVal
                return
            }

            // Variable declaration or assignment: [Type] varName = expr OR varName = expr
            val eqIdx = findTopLevelEquals(s)
            if (eqIdx > 0) {
                val lhs = s.substring(0, eqIdx).trim()
                val rhs = s.substring(eqIdx + 1).trim()
                val evaluatedRhs = evalExpr(rhs, scope, currentClass, lineNum, thisInstance)

                // Check if lhs is "this.field" or "obj.field"
                if (lhs.contains(".")) {
                    val objPart = lhs.substringBefore(".").trim()
                    val fieldPart = lhs.substringAfter(".").trim()
                    if (objPart == "this" && thisInstance != null) {
                        thisInstance.fields[fieldPart] = evaluatedRhs
                    } else {
                        val targetObj = scope[objPart] as? JavaObjectInstance
                        targetObj?.fields?.set(fieldPart, evaluatedRhs)
                    }
                    return
                }

                // Extract variable name from declaration like "int x", "final String name", "int[] nums", "List<String> list"
                val cleanLhs = lhs
                    .replace("final ", "")
                    .replace(Regex("""<[^>]+>"""), "")
                    .replace("[]", "")
                    .trim()
                val tokens = cleanLhs.split(Regex("""\s+"""))
                val varName = tokens.last()
                assignVar(varName, evaluatedRhs, scope, thisInstance)
                return
            }

            // Uninitialized variable declaration e.g. "int a, b;" or "String name"
            val declMatch = Regex("""^(?:final\s+)?(?:int|long|double|float|boolean|char|String|var)\s+([a-zA-Z_][a-zA-Z0-9_]*)$""").find(s)
            if (declMatch != null) {
                val varName = declMatch.groupValues[1]
                scope[varName] = 0
                return
            }

            // Otherwise evaluate as expression (e.g., method call like list.add(...), Collections.sort(...), myMethod(...))
            evalExpr(s, scope, currentClass, lineNum, thisInstance)
        }

        private fun isCollectionVar(expr: String, scope: Map<String, Any?>): Boolean {
            val v = scope[expr]
            return v is MutableList<*> || v is MutableSet<*>
        }

        private fun assignVar(name: String, value: Any?, scope: MutableMap<String, Any?>, thisInstance: JavaObjectInstance?) {
            if (scope.containsKey(name)) {
                scope[name] = value
            } else if (thisInstance != null && thisInstance.fields.containsKey(name)) {
                thisInstance.fields[name] = value
            } else {
                scope[name] = value
            }
        }

        fun evalExpr(
            rawExpr: String,
            scope: MutableMap<String, Any?>,
            currentClass: ParsedClassDef,
            lineNum: Int,
            thisInstance: JavaObjectInstance? = null
        ): Any? {
            var expr = rawExpr.trim()
            if (expr.isEmpty()) return ""

            // Strip outer redundant parentheses
            while (expr.startsWith("(") && expr.endsWith(")") && isWrappedInSingleParens(expr)) {
                expr = expr.substring(1, expr.length - 1).trim()
            }

            // Type cast prefix e.g. (int) 3.14 or (double) x or (String) list.get(0)
            val castMatch = Regex("""^\((int|long|double|float|char|String|Object)\)\s*(.+)$""").find(expr)
            if (castMatch != null) {
                val castType = castMatch.groupValues[1]
                val innerVal = evalExpr(castMatch.groupValues[2], scope, currentClass, lineNum, thisInstance)
                return when (castType) {
                    "int" -> toNumber(innerVal).toInt()
                    "long" -> toNumber(innerVal).toLong()
                    "double", "float" -> toNumber(innerVal).toDouble()
                    "char" -> if (innerVal is Number) innerVal.toInt().toChar() else innerVal.toString().firstOrNull() ?: ' '
                    "String" -> formatJavaValue(innerVal)
                    else -> innerVal
                }
            }

            // Literals
            if (expr == "null") return null
            if (expr == "true") return true
            if (expr == "false") return false
            if (expr.startsWith("\"\"\"") && expr.endsWith("\"\"\"") && expr.length >= 6) {
                return expr.substring(3, expr.length - 3).trimIndent()
            }
            if (expr.startsWith("\"") && expr.endsWith("\"") && isSingleStringLiteral(expr)) {
                return unescapeString(expr.substring(1, expr.length - 1))
            }
            if (expr.startsWith("'") && expr.endsWith("'") && expr.length in 3..4) {
                return unescapeString(expr.substring(1, expr.length - 1)).firstOrNull() ?: ' '
            }
            // Numeric literals
            val cleanNum = expr.replace("_", "")
            if (Regex("""^-?\d+[Ll]?$""").matches(cleanNum)) {
                return cleanNum.removeSuffix("L").removeSuffix("l").toLongOrNull() ?: 0L
            }
            if (Regex("""^-?\d+\.\d+[dDfF]?$""").matches(cleanNum)) {
                return cleanNum.removeSuffix("d").removeSuffix("D").removeSuffix("f").removeSuffix("F").toDoubleOrNull() ?: 0.0
            }

            // Array literal: {1, 2, 3} or new int[]{1, 2, 3}
            if (expr.startsWith("{") && expr.endsWith("}")) {
                val inner = expr.substring(1, expr.length - 1).trim()
                if (inner.isEmpty()) return mutableListOf<Any?>()
                return splitTopLevelArgs(inner).map { evalExpr(it, scope, currentClass, lineNum, thisInstance) }.toMutableList()
            }
            val newArrayInitMatch = Regex("""^new\s+\w+(?:\[\])+\s*\{(.*)\}$""").find(expr)
            if (newArrayInitMatch != null) {
                val inner = newArrayInitMatch.groupValues[1].trim()
                if (inner.isEmpty()) return mutableListOf<Any?>()
                return splitTopLevelArgs(inner).map { evalExpr(it, scope, currentClass, lineNum, thisInstance) }.toMutableList()
            }
            val newArraySizeMatch = Regex("""^new\s+(\w+)\[([^\]]+)\]$""").find(expr)
            if (newArraySizeMatch != null) {
                val elemType = newArraySizeMatch.groupValues[1]
                val size = toNumber(evalExpr(newArraySizeMatch.groupValues[2], scope, currentClass, lineNum, thisInstance)).toInt()
                val defaultElem: Any? = when (elemType) {
                    "int", "long", "short", "byte" -> 0L
                    "double", "float" -> 0.0
                    "boolean" -> false
                    else -> null
                }
                return MutableList(size.coerceAtLeast(0)) { defaultElem }
            }

            // Ternary operator: cond ? tr : fl
            val qIdx = findTopLevelToken(expr, "?")
            if (qIdx > 0) {
                val cIdx = findTopLevelToken(expr.substring(qIdx + 1), ":")
                if (cIdx > 0) {
                    val condStr = expr.substring(0, qIdx).trim()
                    val trueStr = expr.substring(qIdx + 1, qIdx + 1 + cIdx).trim()
                    val falseStr = expr.substring(qIdx + 1 + cIdx + 1).trim()
                    return if (toBoolean(evalExpr(condStr, scope, currentClass, lineNum, thisInstance))) {
                        evalExpr(trueStr, scope, currentClass, lineNum, thisInstance)
                    } else {
                        evalExpr(falseStr, scope, currentClass, lineNum, thisInstance)
                    }
                }
            }

            // Logical OR || and AND &&
            val orIdx = findTopLevelToken(expr, "||")
            if (orIdx > 0) {
                val left = toBoolean(evalExpr(expr.substring(0, orIdx), scope, currentClass, lineNum, thisInstance))
                if (left) return true
                return toBoolean(evalExpr(expr.substring(orIdx + 2), scope, currentClass, lineNum, thisInstance))
            }
            val andIdx = findTopLevelToken(expr, "&&")
            if (andIdx > 0) {
                val left = toBoolean(evalExpr(expr.substring(0, andIdx), scope, currentClass, lineNum, thisInstance))
                if (!left) return false
                return toBoolean(evalExpr(expr.substring(andIdx + 2), scope, currentClass, lineNum, thisInstance))
            }

            // instanceof (including JDK 16 pattern matching: obj instanceof String s)
            val instIdx = findTopLevelToken(expr, " instanceof ")
            if (instIdx > 0) {
                val lhsVal = evalExpr(expr.substring(0, instIdx), scope, currentClass, lineNum, thisInstance)
                val rhsTokens = expr.substring(instIdx + 12).trim().split(Regex("""\s+"""))
                val targetType = rhsTokens[0]
                val patternVar = rhsTokens.getOrNull(1)
                val matches = when (targetType) {
                    "String" -> lhsVal is String
                    "Integer", "Long", "Number" -> lhsVal is Number
                    "Double", "Float" -> lhsVal is Double || lhsVal is Float
                    "List", "ArrayList", "Vector", "Collection" -> lhsVal is List<*>
                    else -> (lhsVal as? JavaObjectInstance)?.let {
                        it.classDef.name == targetType || it.classDef.superClass == targetType
                    } ?: false
                }
                if (matches && patternVar != null) {
                    scope[patternVar] = lhsVal
                }
                return matches
            }

            // Relational & equality operators
            for (op in listOf("==", "!=", "<=", ">=", "<", ">")) {
                val opIdx = findTopLevelRelational(expr, op)
                if (opIdx > 0) {
                    val leftVal = evalExpr(expr.substring(0, opIdx), scope, currentClass, lineNum, thisInstance)
                    val rightVal = evalExpr(expr.substring(opIdx + op.length), scope, currentClass, lineNum, thisInstance)
                    return when (op) {
                        "==" -> valuesEqual(leftVal, rightVal)
                        "!=" -> !valuesEqual(leftVal, rightVal)
                        "<=" -> compareValues(leftVal, rightVal) <= 0
                        ">=" -> compareValues(leftVal, rightVal) >= 0
                        "<" -> compareValues(leftVal, rightVal) < 0
                        ">" -> compareValues(leftVal, rightVal) > 0
                        else -> false
                    }
                }
            }

            // Additive (+, -) from right to left at top level
            val addSubIdx = findLastTopLevelAddSub(expr)
            if (addSubIdx > 0) {
                val op = expr[addSubIdx].toString()
                val leftVal = evalExpr(expr.substring(0, addSubIdx), scope, currentClass, lineNum, thisInstance)
                val rightVal = evalExpr(expr.substring(addSubIdx + 1), scope, currentClass, lineNum, thisInstance)
                if (op == "+" && (leftVal is String || rightVal is String)) {
                    return "${formatJavaValue(leftVal)}${formatJavaValue(rightVal)}"
                }
                return evalMathOp(leftVal, rightVal, op, lineNum)
            }

            // Multiplicative (*, /, %) from right to left at top level
            val mulDivIdx = findLastTopLevelMulDiv(expr)
            if (mulDivIdx > 0) {
                val op = expr[mulDivIdx].toString()
                val leftVal = evalExpr(expr.substring(0, mulDivIdx), scope, currentClass, lineNum, thisInstance)
                val rightVal = evalExpr(expr.substring(mulDivIdx + 1), scope, currentClass, lineNum, thisInstance)
                return evalMathOp(leftVal, rightVal, op, lineNum)
            }

            // Bitwise (&, |, ^, <<, >>)
            for (bitOp in listOf("<<", ">>", "&", "|", "^")) {
                val bIdx = findTopLevelToken(expr, bitOp)
                if (bIdx > 0) {
                    val l = toNumber(evalExpr(expr.substring(0, bIdx), scope, currentClass, lineNum, thisInstance)).toLong()
                    val r = toNumber(evalExpr(expr.substring(bIdx + bitOp.length), scope, currentClass, lineNum, thisInstance)).toLong()
                    return when (bitOp) {
                        "<<" -> l shl r.toInt()
                        ">>" -> l shr r.toInt()
                        "&" -> l and r
                        "|" -> l or r
                        "^" -> l xor r
                        else -> l
                    }
                }
            }

            // Unary !
            if (expr.startsWith("!")) {
                return !toBoolean(evalExpr(expr.substring(1), scope, currentClass, lineNum, thisInstance))
            }

            // Object / Collection instantiation: new ClassName(...)
            if (expr.startsWith("new ")) {
                return evalInstantiation(expr, scope, currentClass, lineNum, thisInstance)
            }

            // Stream pipeline evaluation: collection.stream()...
            if (expr.contains(".stream()")) {
                return evalStreamPipeline(expr, scope, currentClass, lineNum, thisInstance)
            }

            // Array indexing: arr[index]
            val idxAccessMatch = Regex("""^([a-zA-Z_][a-zA-Z0-9_]*)\[([^\]]+)\]$""").find(expr)
            if (idxAccessMatch != null) {
                val arrName = idxAccessMatch.groupValues[1]
                val idxVal = toNumber(evalExpr(idxAccessMatch.groupValues[2], scope, currentClass, lineNum, thisInstance)).toInt()
                val arrObj = scope[arrName] ?: thisInstance?.fields?.get(arrName)
                if (arrObj == null) {
                    throw RuntimeThrownException("java.lang.NullPointerException", "Cannot load from array '$arrName' because it is null", lineNum)
                }
                val list = when (arrObj) {
                    is List<*> -> arrObj
                    is Array<*> -> arrObj.toList()
                    else -> emptyList()
                }
                if (idxVal < 0 || idxVal >= list.size) {
                    throw RuntimeThrownException(
                        "java.lang.ArrayIndexOutOfBoundsException",
                        "Index $idxVal out of bounds for length ${list.size}",
                        lineNum
                    )
                }
                return list[idxVal]
            }

            // Built-in static / instance method calls & property accesses
            if (expr.contains(".") || expr.endsWith(")")) {
                return evalCallOrMemberAccess(expr, scope, currentClass, lineNum, thisInstance)
            }

            // Variable lookup
            if (scope.containsKey(expr)) return scope[expr]
            if (thisInstance != null && thisInstance.fields.containsKey(expr)) return thisInstance.fields[expr]
            if (currentClass.fields.containsKey(expr)) {
                val init = currentClass.fields[expr] ?: ""
                return if (init.isNotEmpty()) evalExpr(init, scope, currentClass, lineNum, thisInstance) else 0
            }

            return expr
        }

        private fun evalInstantiation(
            expr: String,
            scope: MutableMap<String, Any?>,
            currentClass: ParsedClassDef,
            lineNum: Int,
            thisInstance: JavaObjectInstance?
        ): Any? {
            val clean = expr.removePrefix("new ").trim()
            val className = clean.substringBefore("(").replace(Regex("""<[^>]*>"""), "").trim()
            val argsRaw = extractParenContent(clean)
            val argValues = if (argsRaw.isBlank()) emptyList() else splitTopLevelArgs(argsRaw).map {
                evalExpr(it, scope, currentClass, lineNum, thisInstance)
            }

            return when (className) {
                "ArrayList", "LinkedList", "Vector", "Stack" -> {
                    val first = argValues.firstOrNull()
                    if (first is Collection<*>) first.toMutableList() else mutableListOf<Any?>()
                }
                "HashSet", "TreeSet", "LinkedHashSet" -> mutableSetOf<Any?>()
                "HashMap", "TreeMap", "Hashtable", "LinkedHashMap" -> mutableMapOf<Any?, Any?>()
                "StringBuilder", "StringBuffer" -> StringBuilder(argValues.firstOrNull()?.toString() ?: "")
                "Scanner" -> "SCANNER_STDIN"
                "File" -> argValues.firstOrNull()?.toString() ?: "data.txt"
                "Frame", "JFrame" -> {
                    val title = argValues.firstOrNull()?.toString() ?: "Java GUI Window"
                    onGuiTitle(title)
                    "GUI_FRAME:$title"
                }
                "Button", "JButton" -> "Button[${argValues.firstOrNull() ?: "Action"}]"
                "Label", "JLabel" -> "Label[${argValues.firstOrNull() ?: "Text"}]"
                "TextField", "JTextField" -> "TextField[${argValues.firstOrNull() ?: ""}]"
                else -> {
                    val targetClass = classes[className]
                    if (targetClass != null) {
                        val instance = JavaObjectInstance(targetClass)
                        // Initialize fields from superClass and targetClass
                        val superDef = targetClass.superClass?.let { classes[it] }
                        superDef?.fields?.forEach { (k, initExpr) ->
                            instance.fields[k] = if (initExpr.isNotBlank()) evalExpr(initExpr, scope, superDef, lineNum, instance) else 0
                        }
                        targetClass.fields.forEach { (k, initExpr) ->
                            instance.fields[k] = if (initExpr.isNotBlank()) evalExpr(initExpr, scope, targetClass, lineNum, instance) else 0
                        }
                        if (targetClass.isRecord) {
                            targetClass.recordComponents.forEachIndexed { idx, (_, compName) ->
                                instance.fields[compName] = argValues.getOrNull(idx)
                            }
                        } else {
                            val ctor = targetClass.constructors.firstOrNull { it.params.size == argValues.size }
                                ?: targetClass.constructors.firstOrNull()
                            if (ctor != null) {
                                val ctorScope = mutableMapOf<String, Any?>()
                                ctor.params.forEachIndexed { idx, (_, pName) ->
                                    ctorScope[pName] = argValues.getOrNull(idx)
                                }
                                executeBlock(ctor.bodyLines, ctorScope, targetClass, instance)
                            }
                        }
                        instance
                    } else {
                        "$className(${argValues.joinToString(", ")})"
                    }
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun evalCallOrMemberAccess(
            expr: String,
            scope: MutableMap<String, Any?>,
            currentClass: ParsedClassDef,
            lineNum: Int,
            thisInstance: JavaObjectInstance?
        ): Any? {
            // Static helpers
            if (expr == "Math.PI") return Math.PI
            if (expr == "Math.E") return Math.E
            if (expr == "Integer.MAX_VALUE") return Int.MAX_VALUE
            if (expr == "Integer.MIN_VALUE") return Int.MIN_VALUE

            // Array .length
            if (expr.endsWith(".length") && !expr.endsWith(".length()")) {
                val targetExpr = expr.removeSuffix(".length").trim()
                val targetVal = evalExpr(targetExpr, scope, currentClass, lineNum, thisInstance)
                return when (targetVal) {
                    is List<*> -> targetVal.size
                    is Array<*> -> targetVal.size
                    is String -> targetVal.length
                    else -> 0
                }
            }

            // Standard static calls: Math.*, Integer.parseInt, Double.parseDouble, Arrays.*, Collections.*, List.of, String.valueOf
            if (expr.startsWith("Math.")) {
                val method = expr.removePrefix("Math.").substringBefore("(")
                val args = splitTopLevelArgs(extractParenContent(expr)).map {
                    toNumber(evalExpr(it, scope, currentClass, lineNum, thisInstance)).toDouble()
                }
                return when (method) {
                    "sqrt" -> sqrt(args.getOrElse(0) { 0.0 })
                    "pow" -> args.getOrElse(0) { 0.0 }.pow(args.getOrElse(1) { 1.0 })
                    "abs" -> abs(args.getOrElse(0) { 0.0 })
                    "max" -> max(args.getOrElse(0) { 0.0 }, args.getOrElse(1) { 0.0 })
                    "min" -> min(args.getOrElse(0) { 0.0 }, args.getOrElse(1) { 0.0 })
                    "round" -> args.getOrElse(0) { 0.0 }.roundToLong()
                    "sin" -> sin(args.getOrElse(0) { 0.0 })
                    "cos" -> cos(args.getOrElse(0) { 0.0 })
                    else -> 0.0
                }
            }

            if (expr.startsWith("Integer.parseInt(")) {
                val str = evalExpr(extractParenContent(expr), scope, currentClass, lineNum, thisInstance).toString().trim()
                return str.toIntOrNull()
                    ?: throw RuntimeThrownException("java.lang.NumberFormatException", "For input string: \"$str\"", lineNum)
            }
            if (expr.startsWith("Double.parseDouble(")) {
                val str = evalExpr(extractParenContent(expr), scope, currentClass, lineNum, thisInstance).toString().trim()
                return str.toDoubleOrNull()
                    ?: throw RuntimeThrownException("java.lang.NumberFormatException", "For input string: \"$str\"", lineNum)
            }
            if (expr.startsWith("String.valueOf(")) {
                return formatJavaValue(evalExpr(extractParenContent(expr), scope, currentClass, lineNum, thisInstance))
            }
            if (expr.startsWith("Arrays.toString(")) {
                val list = evalExpr(extractParenContent(expr), scope, currentClass, lineNum, thisInstance) as? List<*>
                return list?.joinToString(", ", "[", "]") { formatJavaValue(it) } ?: "null"
            }
            if (expr.startsWith("Arrays.asList(") || expr.startsWith("List.of(") || expr.startsWith("Set.of(")) {
                val args = splitTopLevelArgs(extractParenContent(expr)).map {
                    evalExpr(it, scope, currentClass, lineNum, thisInstance)
                }
                return args.toMutableList()
            }
            if (expr.startsWith("Arrays.sort(") || expr.startsWith("Collections.sort(")) {
                val target = evalExpr(extractParenContent(expr), scope, currentClass, lineNum, thisInstance) as? MutableList<Any?>
                target?.sortWith { a, b -> compareValues(a, b) }
                return null
            }

            // Method call on same class or super: methodName(arg1, arg2)
            if (!expr.contains(".") && expr.endsWith(")")) {
                val mName = expr.substringBefore("(").trim()
                val argExprs = splitTopLevelArgs(extractParenContent(expr))
                val argVals = argExprs.map { evalExpr(it, scope, currentClass, lineNum, thisInstance) }
                return invokeMethodOnClass(currentClass, mName, argVals, thisInstance, lineNum)
            }

            // Target.member or Target.method(...)
            val lastDot = findLastTopLevelDot(expr)
            if (lastDot > 0) {
                val targetExpr = expr.substring(0, lastDot).trim()
                val memberPart = expr.substring(lastDot + 1).trim()

                // Check if targetExpr is a class name for static method call
                val staticClass = classes[targetExpr]
                if (staticClass != null && memberPart.endsWith(")")) {
                    val mName = memberPart.substringBefore("(").trim()
                    val argVals = splitTopLevelArgs(extractParenContent(memberPart)).map {
                        evalExpr(it, scope, currentClass, lineNum, thisInstance)
                    }
                    return invokeMethodOnClass(staticClass, mName, argVals, null, lineNum)
                }

                val targetObj = if (targetExpr == "this") thisInstance else evalExpr(targetExpr, scope, currentClass, lineNum, thisInstance)
                if (targetObj == null) {
                    throw RuntimeThrownException(
                        "java.lang.NullPointerException",
                        "Cannot invoke \"$memberPart\" because \"$targetExpr\" is null",
                        lineNum
                    )
                }

                // Property access without parentheses
                if (!memberPart.endsWith(")")) {
                    if (targetObj is JavaObjectInstance) {
                        return targetObj.fields[memberPart]
                    }
                    return null
                }

                val mName = memberPart.substringBefore("(").trim()
                val argVals = splitTopLevelArgs(extractParenContent(memberPart)).map {
                    evalExpr(it, scope, currentClass, lineNum, thisInstance)
                }

                // Scanner input methods
                if (targetObj == "SCANNER_STDIN") {
                    return when (mName) {
                        "nextInt" -> (if (stdinTokens.isNotEmpty()) stdinTokens.removeAt(0) else "42").toIntOrNull() ?: 42
                        "nextLong" -> (if (stdinTokens.isNotEmpty()) stdinTokens.removeAt(0) else "42").toLongOrNull() ?: 42L
                        "nextDouble" -> (if (stdinTokens.isNotEmpty()) stdinTokens.removeAt(0) else "3.14").toDoubleOrNull() ?: 3.14
                        "next" -> if (stdinTokens.isNotEmpty()) stdinTokens.removeAt(0) else "JavaInput"
                        "nextLine" -> if (stdinLines.isNotEmpty()) stdinLines.removeAt(0) else "Universal Java Studio"
                        "hasNext", "hasNextInt", "hasNextLine" -> stdinTokens.isNotEmpty()
                        else -> ""
                    }
                }

                // String methods
                if (targetObj is String) {
                    return when (mName) {
                        "length" -> targetObj.length
                        "toUpperCase" -> targetObj.uppercase()
                        "toLowerCase" -> targetObj.lowercase()
                        "trim", "strip" -> targetObj.trim()
                        "isEmpty" -> targetObj.isEmpty()
                        "isBlank" -> targetObj.isBlank()
                        "charAt" -> {
                            val idx = toNumber(argVals.firstOrNull()).toInt()
                            if (idx < 0 || idx >= targetObj.length) {
                                throw RuntimeThrownException("java.lang.StringIndexOutOfBoundsException", "String index out of range: $idx", lineNum)
                            }
                            targetObj[idx]
                        }
                        "substring" -> {
                            val start = toNumber(argVals.getOrNull(0)).toInt()
                            val end = if (argVals.size >= 2) toNumber(argVals[1]).toInt() else targetObj.length
                            targetObj.substring(start.coerceIn(0, targetObj.length), end.coerceIn(0, targetObj.length))
                        }
                        "indexOf" -> targetObj.indexOf(argVals.firstOrNull()?.toString() ?: "")
                        "contains" -> targetObj.contains(argVals.firstOrNull()?.toString() ?: "")
                        "startsWith" -> targetObj.startsWith(argVals.firstOrNull()?.toString() ?: "")
                        "endsWith" -> targetObj.endsWith(argVals.firstOrNull()?.toString() ?: "")
                        "equals" -> targetObj == argVals.firstOrNull()?.toString()
                        "equalsIgnoreCase" -> targetObj.equals(argVals.firstOrNull()?.toString(), ignoreCase = true)
                        "replace" -> targetObj.replace(
                            argVals.getOrNull(0)?.toString() ?: "",
                            argVals.getOrNull(1)?.toString() ?: ""
                        )
                        "repeat" -> targetObj.repeat(toNumber(argVals.firstOrNull()).toInt().coerceIn(0, 100))
                        "split" -> targetObj.split(Regex(argVals.firstOrNull()?.toString() ?: " ")).toMutableList()
                        else -> targetObj
                    }
                }

                // StringBuilder / StringBuffer
                if (targetObj is StringBuilder) {
                    return when (mName) {
                        "append" -> {
                            targetObj.append(formatJavaValue(argVals.firstOrNull()))
                            targetObj
                        }
                        "reverse" -> {
                            targetObj.reverse()
                            targetObj
                        }
                        "length" -> targetObj.length
                        "toString" -> targetObj.toString()
                        else -> targetObj
                    }
                }

                // List / Vector / Stack methods
                if (targetObj is MutableList<*>) {
                    val list = targetObj as MutableList<Any?>
                    return when (mName) {
                        "add", "addElement", "push", "addLast" -> {
                            if (argVals.size >= 2 && argVals[0] is Number) {
                                list.add((argVals[0] as Number).toInt(), argVals[1])
                            } else {
                                list.add(argVals.firstOrNull())
                            }
                            true
                        }
                        "addFirst" -> {
                            list.add(0, argVals.firstOrNull())
                            true
                        }
                        "get", "elementAt" -> {
                            val idx = toNumber(argVals.firstOrNull()).toInt()
                            list[idx]
                        }
                        "getFirst", "firstElement", "peek" -> list.firstOrNull()
                        "getLast", "lastElement" -> list.lastOrNull()
                        "set" -> {
                            val idx = toNumber(argVals[0]).toInt()
                            list[idx] = argVals.getOrNull(1)
                        }
                        "remove", "removeElement" -> {
                            val first = argVals.firstOrNull()
                            if (first is Number && first.toInt() in list.indices) list.removeAt(first.toInt())
                            else list.remove(first)
                        }
                        "pop", "removeLast" -> if (list.isNotEmpty()) list.removeAt(list.lastIndex) else null
                        "size" -> list.size
                        "isEmpty" -> list.isEmpty()
                        "contains" -> list.contains(argVals.firstOrNull())
                        "clear" -> list.clear()
                        "reversed" -> list.reversed().toMutableList()
                        "toString" -> list.joinToString(", ", "[", "]") { formatJavaValue(it) }
                        else -> list
                    }
                }

                // Map / Hashtable methods
                if (targetObj is MutableMap<*, *>) {
                    val map = targetObj as MutableMap<Any?, Any?>
                    return when (mName) {
                        "put" -> map.put(argVals.getOrNull(0), argVals.getOrNull(1))
                        "get" -> map[argVals.firstOrNull()]
                        "getOrDefault" -> map.getOrDefault(argVals.getOrNull(0), argVals.getOrNull(1))
                        "containsKey" -> map.containsKey(argVals.firstOrNull())
                        "remove" -> map.remove(argVals.firstOrNull())
                        "size" -> map.size
                        "keySet", "keys" -> map.keys.toMutableList()
                        "values" -> map.values.toMutableList()
                        "toString" -> map.entries.joinToString(", ", "{", "}") { "${it.key}=${it.value}" }
                        else -> map
                    }
                }

                // Custom JavaObjectInstance method or record accessor
                if (targetObj is JavaObjectInstance) {
                    if (targetObj.classDef.isRecord && argVals.isEmpty() && targetObj.fields.containsKey(mName)) {
                        return targetObj.fields[mName]
                    }
                    if (mName == "toString" && !targetObj.classDef.methods.containsKey("toString")) {
                        return targetObj.toString()
                    }
                    return invokeMethodOnClass(targetObj.classDef, mName, argVals, targetObj, lineNum)
                }
            }

            return expr
        }

        private fun invokeMethodOnClass(
            classDef: ParsedClassDef,
            methodName: String,
            argVals: List<Any?>,
            instance: JavaObjectInstance?,
            lineNum: Int
        ): Any? {
            val overloads = classDef.methods[methodName]
            val method = overloads?.firstOrNull { it.params.size == argVals.size }
                ?: overloads?.firstOrNull()

            if (method == null) {
                val superDef = classDef.superClass?.let { classes[it] }
                if (superDef != null) {
                    return invokeMethodOnClass(superDef, methodName, argVals, instance, lineNum)
                }
                return null
            }

            val methodScope = mutableMapOf<String, Any?>()
            method.params.forEachIndexed { idx, (_, pName) ->
                methodScope[pName] = argVals.getOrNull(idx)
            }
            try {
                executeBlock(method.bodyLines, methodScope, classDef, instance)
            } catch (ret: ReturnSignal) {
                return ret.value
            }
            return null
        }

        private fun evalStreamPipeline(
            expr: String,
            scope: MutableMap<String, Any?>,
            currentClass: ParsedClassDef,
            lineNum: Int,
            thisInstance: JavaObjectInstance?
        ): Any? {
            val baseExpr = expr.substringBefore(".stream()").trim()
            val baseVal = evalExpr(baseExpr, scope, currentClass, lineNum, thisInstance)
            var currentItems: List<Any?> = when (baseVal) {
                is List<*> -> baseVal.toList()
                is Set<*> -> baseVal.toList()
                else -> emptyList()
            }

            val chain = expr.substringAfter(".stream()")
            val ops = Regex("""\.(filter|map|sorted|distinct|limit|forEach|collect|toList|count|reduce)\s*\(([^)]*)\)""").findAll(chain)

            for (opMatch in ops) {
                val opName = opMatch.groupValues[1]
                val arg = opMatch.groupValues[2].trim()
                when (opName) {
                    "filter" -> {
                        val param = arg.substringBefore("->").trim().removePrefix("(").removeSuffix(")").trim()
                        val body = arg.substringAfter("->").trim()
                        currentItems = currentItems.filter { item ->
                            val tempScope = scope.toMutableMap()
                            tempScope[param] = item
                            toBoolean(evalExpr(body, tempScope, currentClass, lineNum, thisInstance))
                        }
                    }
                    "map" -> {
                        val param = arg.substringBefore("->").trim().removePrefix("(").removeSuffix(")").trim()
                        val body = arg.substringAfter("->").trim()
                        currentItems = currentItems.map { item ->
                            val tempScope = scope.toMutableMap()
                            tempScope[param] = item
                            evalExpr(body, tempScope, currentClass, lineNum, thisInstance)
                        }
                    }
                    "sorted" -> {
                        currentItems = currentItems.sortedWith { a, b -> compareValues(a, b) }
                    }
                    "distinct" -> {
                        currentItems = currentItems.distinct()
                    }
                    "limit" -> {
                        val n = toNumber(evalExpr(arg, scope, currentClass, lineNum, thisInstance)).toInt()
                        currentItems = currentItems.take(n)
                    }
                    "count" -> return currentItems.size.toLong()
                    "collect", "toList" -> return currentItems.toMutableList()
                    "forEach" -> {
                        if (arg.contains("System.out::println")) {
                            currentItems.forEach { stdout.appendLine(formatJavaValue(it)) }
                        } else if (arg.contains("->")) {
                            val param = arg.substringBefore("->").trim().removePrefix("(").removeSuffix(")").trim()
                            val body = arg.substringAfter("->").trim()
                            for (item in currentItems) {
                                val tempScope = scope.toMutableMap()
                                tempScope[param] = item
                                executeSingleStatement(body, tempScope, currentClass, lineNum, thisInstance)
                            }
                        }
                        return null
                    }
                }
            }
            return currentItems.toMutableList()
        }

        private fun evalMathOp(left: Any?, right: Any?, op: String, lineNum: Int): Number {
            val lNum = toNumber(left)
            val rNum = toNumber(right)
            val isFloating = left is Double || left is Float || right is Double || right is Float
            return if (isFloating) {
                val ld = lNum.toDouble()
                val rd = rNum.toDouble()
                when (op) {
                    "+" -> ld + rd
                    "-" -> ld - rd
                    "*" -> ld * rd
                    "/" -> ld / rd
                    "%" -> ld % rd
                    else -> 0.0
                }
            } else {
                val ll = lNum.toLong()
                val rl = rNum.toLong()
                if ((op == "/" || op == "%") && rl == 0L) {
                    throw RuntimeThrownException("java.lang.ArithmeticException", "/ by zero", lineNum)
                }
                when (op) {
                    "+" -> ll + rl
                    "-" -> ll - rl
                    "*" -> ll * rl
                    "/" -> ll / rl
                    "%" -> ll % rl
                    else -> 0L
                }
            }
        }
    }

    // Expression helper utilities
    private fun extractParenContent(s: String): String {
        val open = s.indexOf('(')
        if (open < 0) return ""
        var depth = 0
        var inStr = false
        for (i in open until s.length) {
            val c = s[i]
            if (c == '"' && (i == 0 || s[i - 1] != '\\')) inStr = !inStr
            if (!inStr) {
                if (c == '(') depth++
                else if (c == ')') {
                    depth--
                    if (depth == 0) return s.substring(open + 1, i)
                }
            }
        }
        return s.substring(open + 1).removeSuffix(")")
    }

    private fun isWrappedInSingleParens(s: String): Boolean {
        var depth = 0
        var inStr = false
        for (i in s.indices) {
            val c = s[i]
            if (c == '"' && (i == 0 || s[i - 1] != '\\')) inStr = !inStr
            if (!inStr) {
                if (c == '(') depth++
                else if (c == ')') {
                    depth--
                    if (depth == 0 && i < s.length - 1) return false
                }
            }
        }
        return depth == 0
    }

    private fun isSingleStringLiteral(s: String): Boolean {
        var inStr = false
        for (i in s.indices) {
            val c = s[i]
            if (c == '"' && (i == 0 || s[i - 1] != '\\')) {
                inStr = !inStr
                if (!inStr && i < s.length - 1) return false
            }
        }
        return true
    }

    private fun unescapeString(s: String): String {
        return s.replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    private fun findTopLevelEquals(s: String): Int {
        var depth = 0
        var inStr = false
        for (i in s.indices) {
            val c = s[i]
            if (c == '"' && (i == 0 || s[i - 1] != '\\')) inStr = !inStr
            if (!inStr) {
                if (c == '(' || c == '[' || c == '{') depth++
                else if (c == ')' || c == ']' || c == '}') depth--
                else if (depth == 0 && c == '=') {
                    val prev = if (i > 0) s[i - 1] else ' '
                    val next = if (i + 1 < s.length) s[i + 1] else ' '
                    if (prev !in listOf('=', '!', '<', '>', '+', '-', '*', '/') && next != '=') {
                        return i
                    }
                }
            }
        }
        return -1
    }

    private fun findTopLevelToken(s: String, token: String): Int {
        var depth = 0
        var inStr = false
        var i = 0
        while (i <= s.length - token.length) {
            val c = s[i]
            if (c == '"' && (i == 0 || s[i - 1] != '\\')) inStr = !inStr
            if (!inStr) {
                if (c == '(' || c == '[' || c == '{') depth++
                else if (c == ')' || c == ']' || c == '}') depth--
                else if (depth == 0 && s.substring(i, i + token.length) == token) {
                    return i
                }
            }
            i++
        }
        return -1
    }

    private fun findTopLevelRelational(s: String, op: String): Int {
        var depth = 0
        var inStr = false
        var i = 0
        while (i <= s.length - op.length) {
            val c = s[i]
            if (c == '"' && (i == 0 || s[i - 1] != '\\')) inStr = !inStr
            if (!inStr) {
                if (c == '(' || c == '[' || c == '{') depth++
                else if (c == ')' || c == ']' || c == '}') depth--
                else if (depth == 0 && s.substring(i, i + op.length) == op) {
                    if (op == "<" || op == ">") {
                        val next = if (i + 1 < s.length) s[i + 1] else ' '
                        val prev = if (i > 0) s[i - 1] else ' '
                        if (next == '=' || next == op[0] || prev == op[0] || prev == '-') {
                            i++
                            continue
                        }
                    }
                    return i
                }
            }
            i++
        }
        return -1
    }

    private fun findLastTopLevelAddSub(s: String): Int {
        var depth = 0
        var inStr = false
        var lastIdx = -1
        for (i in s.indices) {
            val c = s[i]
            if (c == '"' && (i == 0 || s[i - 1] != '\\')) inStr = !inStr
            if (!inStr) {
                if (c == '(' || c == '[' || c == '{') depth++
                else if (c == ')' || c == ']' || c == '}') depth--
                else if (depth == 0 && (c == '+' || c == '-') && i > 0) {
                    val prev = s.substring(0, i).trimEnd().lastOrNull() ?: '+'
                    val next = if (i + 1 < s.length) s[i + 1] else ' '
                    if (prev !in listOf('+', '-', '*', '/', '%', '(', '=', '<', '>', ',', '?', ':') && next != c && next != '>') {
                        lastIdx = i
                    }
                }
            }
        }
        return lastIdx
    }

    private fun findLastTopLevelMulDiv(s: String): Int {
        var depth = 0
        var inStr = false
        var lastIdx = -1
        for (i in s.indices) {
            val c = s[i]
            if (c == '"' && (i == 0 || s[i - 1] != '\\')) inStr = !inStr
            if (!inStr) {
                if (c == '(' || c == '[' || c == '{') depth++
                else if (c == ')' || c == ']' || c == '}') depth--
                else if (depth == 0 && (c == '*' || c == '/' || c == '%') && i > 0) {
                    lastIdx = i
                }
            }
        }
        return lastIdx
    }

    private fun findLastTopLevelDot(s: String): Int {
        var depth = 0
        var inStr = false
        var lastDot = -1
        for (i in s.indices) {
            val c = s[i]
            if (c == '"' && (i == 0 || s[i - 1] != '\\')) inStr = !inStr
            if (!inStr) {
                if (c == '(' || c == '[' || c == '{') depth++
                else if (c == ')' || c == ']' || c == '}') depth--
                else if (depth == 0 && c == '.') {
                    val prevChar = if (i > 0) s[i - 1] else ' '
                    val nextChar = if (i + 1 < s.length) s[i + 1] else ' '
                    if (!(prevChar.isDigit() && nextChar.isDigit())) {
                        lastDot = i
                    }
                }
            }
        }
        return lastDot
    }

    private fun splitTopLevelArgs(s: String): List<String> {
        if (s.isBlank()) return emptyList()
        val result = mutableListOf<String>()
        var depth = 0
        var inStr = false
        val current = StringBuilder()
        for (i in s.indices) {
            val c = s[i]
            if (c == '"' && (i == 0 || s[i - 1] != '\\')) inStr = !inStr
            if (!inStr) {
                if (c == '(' || c == '[' || c == '{') depth++
                else if (c == ')' || c == ']' || c == '}') depth--
                else if (depth == 0 && c == ',') {
                    result.add(current.toString().trim())
                    current.clear()
                    continue
                }
            }
            current.append(c)
        }
        if (current.isNotEmpty()) result.add(current.toString().trim())
        return result
    }

    private fun toNumber(v: Any?): Number {
        return when (v) {
            is Number -> v
            is Char -> v.code
            is String -> v.toLongOrNull() ?: v.toDoubleOrNull() ?: 0L
            is Boolean -> if (v) 1L else 0L
            else -> 0L
        }
    }

    private fun toBoolean(v: Any?): Boolean {
        return when (v) {
            is Boolean -> v
            is Number -> v.toDouble() != 0.0
            is String -> v.equals("true", ignoreCase = true)
            null -> false
            else -> true
        }
    }

    private fun valuesEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null || b == null) return false
        if (a is Number && b is Number) return a.toDouble() == b.toDouble()
        return a.toString() == b.toString()
    }

    private fun compareValues(a: Any?, b: Any?): Int {
        if (a is Number && b is Number) return a.toDouble().compareTo(b.toDouble())
        if (a is Char && b is Char) return a.compareTo(b)
        return a.toString().compareTo(b.toString())
    }

    private fun formatJavaValue(v: Any?): String {
        return when (v) {
            null -> "null"
            is List<*> -> v.joinToString(", ", "[", "]") { formatJavaValue(it) }
            is Double -> if (v % 1.0 == 0.0 && !v.isInfinite()) "${v.toLong()}.0" else v.toString()
            else -> v.toString()
        }
    }
}
