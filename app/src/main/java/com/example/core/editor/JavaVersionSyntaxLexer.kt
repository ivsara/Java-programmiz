package com.example.core.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.example.ui.theme.IdeAmberSecondary
import com.example.ui.theme.IdeCyanPrimary
import com.example.ui.theme.IdeErrorRed
import com.example.ui.theme.IdeSuccessGreen
import com.example.ui.theme.SyntaxAnnotationDark
import com.example.ui.theme.SyntaxAnnotationLight
import com.example.ui.theme.SyntaxCommentDark
import com.example.ui.theme.SyntaxCommentLight
import com.example.ui.theme.SyntaxKeywordDark
import com.example.ui.theme.SyntaxKeywordLight
import com.example.ui.theme.SyntaxNumberDark
import com.example.ui.theme.SyntaxNumberLight
import com.example.ui.theme.SyntaxStringDark
import com.example.ui.theme.SyntaxStringLight
import com.example.ui.theme.SyntaxTypeDark
import com.example.ui.theme.SyntaxTypeLight

/**
 * QScintilla-equivalent Version-Aware Java Lexer & Syntax Highlighter.
 * Dynamically adapts syntax rules, keyword sets, modern constructs, and
 * deprecated/removed API or syntax markers to the selected JDK major version.
 */
data class VersionLexerSummary(
    val jdkVersionLabel: String,
    val activeKeywordCount: Int,
    val modernFeatureTokensDetected: List<String>,
    val deprecatedTokensDetected: List<String>,
    val unsupportedTokensDetected: List<String>
)

object JavaVersionSyntaxLexer {

    // Baseline keywords valid across all Java versions (JDK 1.0+)
    private val BASELINE_KEYWORDS_1_0 = setOf(
        "abstract", "boolean", "break", "byte", "case", "catch", "char", "class",
        "const", "continue", "default", "do", "double", "else", "extends", "final",
        "finally", "float", "for", "goto", "if", "implements", "import", "instanceof",
        "int", "interface", "long", "native", "new", "package", "private", "protected",
        "public", "return", "short", "static", "super", "switch", "synchronized",
        "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
        "true", "false", "null"
    )

    // Keywords introduced in specific Java versions
    private val VERSIONED_KEYWORDS = mapOf(
        "strictfp" to 2,      // JDK 1.2 (Java 2); made obsolete no-op in JDK 17 (JEP 306)
        "assert" to 4,        // JDK 1.4
        "enum" to 5,          // JDK 5
        "var" to 10,          // JDK 10
        "yield" to 14,        // JDK 14
        "record" to 16,       // JDK 16
        "sealed" to 17,       // JDK 17
        "non-sealed" to 17,   // JDK 17
        "permits" to 17       // JDK 17
    )

    // Standard types with minimum JDK version
    private val VERSIONED_TYPES = mapOf(
        "Object" to 0,
        "String" to 0,
        "System" to 0,
        "Math" to 0,
        "Thread" to 0,
        "Runnable" to 0,
        "Throwable" to 0,
        "Exception" to 0,
        "RuntimeException" to 0,
        "ArithmeticException" to 0,
        "StringBuffer" to 0,
        "Vector" to 0,
        "Hashtable" to 0,
        "Stack" to 0,
        "Enumeration" to 0,
        "Frame" to 0,
        "Button" to 0,
        "Label" to 0,
        "TextField" to 0,
        "Graphics" to 0,
        "BufferedReader" to 1,
        "InputStreamReader" to 1,
        "Connection" to 1,
        "DriverManager" to 1,
        "PreparedStatement" to 1,
        "ResultSet" to 1,
        // Java 2 (JDK 1.2) Additions
        "Collection" to 2,
        "Collections" to 2,
        "List" to 2,
        "ArrayList" to 2,
        "LinkedList" to 2,
        "Set" to 2,
        "HashSet" to 2,
        "TreeSet" to 2,
        "Map" to 2,
        "HashMap" to 2,
        "TreeMap" to 2,
        "Iterator" to 2,
        "Arrays" to 2,
        "JFrame" to 2,
        "JButton" to 2,
        "JLabel" to 2,
        "JTextField" to 2,
        "JPanel" to 2,
        // Java 1.4 Additions
        "ByteBuffer" to 4,
        "Pattern" to 4,
        "Matcher" to 4,
        // Java 5 Additions
        "StringBuilder" to 5,
        "Scanner" to 5,
        "ExecutorService" to 5,
        "Executors" to 5,
        "ConcurrentHashMap" to 5,
        // Java 7 Additions
        "Files" to 7,
        "Paths" to 7,
        "Path" to 7,
        "Objects" to 7,
        // Java 8 Additions
        "Stream" to 8,
        "Optional" to 8,
        "LocalDate" to 8,
        "LocalDateTime" to 8,
        "CompletableFuture" to 8,
        // Java 11 Additions
        "HttpClient" to 11,
        "HttpRequest" to 11,
        "HttpResponse" to 11,
        // Java 16–25 Additions
        "Record" to 16,
        "HexFormat" to 17,
        "SequencedCollection" to 21
    )

    // Deprecated / Obsolete APIs or Keywords mapped to the JDK version where deprecation began
    private val DEPRECATED_SYMBOLS = mapOf(
        "Applet" to 9,                // Deprecated in JDK 9, for removal in 17
        "JApplet" to 9,               // Deprecated in JDK 9, for removal in 17
        "AudioClip" to 9,             // Deprecated in JDK 9
        "SecurityManager" to 17,      // Deprecated for removal in JDK 17 (JEP 411)
        "AccessController" to 17,     // Deprecated for removal in JDK 17
        "Observable" to 9,            // Deprecated in JDK 9
        "Observer" to 9,              // Deprecated in JDK 9
        "strictfp" to 17              // Obsolete / redundant as of JDK 17 (JEP 306)
    )

    private val DEPRECATED_METHODS = mapOf(
        ".stop(" to 2,                // Thread.stop() deprecated in JDK 1.2
        ".suspend(" to 2,             // Thread.suspend() deprecated in JDK 1.2
        ".resume(" to 2,              // Thread.resume() deprecated in JDK 1.2
        "runFinalizersOnExit(" to 2,  // Deprecated in JDK 1.2
        "finalize()" to 9             // Object.finalize() deprecated in JDK 9
    )

    fun analyzeAndSummarize(source: String, jdkMajor: Int, jdkDisplayName: String): VersionLexerSummary {
        val modernDetected = mutableSetOf<String>()
        val deprecatedDetected = mutableSetOf<String>()
        val unsupportedDetected = mutableSetOf<String>()

        val wordRegex = Regex("""\b[A-Za-z_][A-Za-z0-9_]*\b""")
        for (match in wordRegex.findAll(source)) {
            val token = match.value
            val minKwMajor = VERSIONED_KEYWORDS[token]
            if (minKwMajor != null) {
                if (jdkMajor < minKwMajor) {
                    unsupportedDetected.add("$token (requires JDK ${formatMajor(minKwMajor)}+)")
                } else if (minKwMajor >= 5) {
                    modernDetected.add("$token (JDK ${formatMajor(minKwMajor)})")
                }
            }

            val minTypeMajor = VERSIONED_TYPES[token]
            if (minTypeMajor != null) {
                if (jdkMajor < minTypeMajor) {
                    unsupportedDetected.add("$token (API added in JDK ${formatMajor(minTypeMajor)})")
                } else if (minTypeMajor >= 5) {
                    modernDetected.add("$token (JDK ${formatMajor(minTypeMajor)}+)")
                }
            }

            val depSince = DEPRECATED_SYMBOLS[token]
            if (depSince != null && jdkMajor >= depSince) {
                deprecatedDetected.add("$token (deprecated/obsolete in JDK $depSince+)")
            }
        }

        // Check annotations, lambdas, method references, diamond, text blocks, virtual threads
        if (Regex("""@[A-Za-z_][A-Za-z0-9_]*""").containsMatchIn(source)) {
            if (jdkMajor < 5) unsupportedDetected.add("@Annotation (requires JDK 5+)")
            else modernDetected.add("@Annotation (JDK 5+)")
        }
        if (source.contains("->")) {
            if (jdkMajor < 8) unsupportedDetected.add("Lambda/Switch '->' (requires JDK 8 / 14+)")
            else modernDetected.add("Arrow '->' (Lambda/Switch)")
        }
        if (source.contains("::")) {
            if (jdkMajor < 8) unsupportedDetected.add("Method Ref '::' (requires JDK 8+)")
            else modernDetected.add("Method Ref '::' (JDK 8+)")
        }
        if (source.contains("\"\"\"")) {
            if (jdkMajor < 15) unsupportedDetected.add("Text Block '\"\"\"' (requires JDK 15+)")
            else modernDetected.add("Text Block '\"\"\"' (JDK 15+)")
        }
        if (source.contains("ofVirtual")) {
            if (jdkMajor < 21) unsupportedDetected.add("Virtual Threads (requires JDK 21+)")
            else modernDetected.add("Virtual Threads (JDK 21+)")
        }
        for ((methodPat, depMajor) in DEPRECATED_METHODS) {
            if (source.contains(methodPat) && jdkMajor >= depMajor) {
                deprecatedDetected.add("${methodPat.removeSuffix("(")}() (deprecated since JDK ${formatMajor(depMajor)})")
            }
        }

        val activeKeywords = BASELINE_KEYWORDS_1_0.size + VERSIONED_KEYWORDS.count { it.value <= jdkMajor }

        return VersionLexerSummary(
            jdkVersionLabel = jdkDisplayName,
            activeKeywordCount = activeKeywords,
            modernFeatureTokensDetected = modernDetected.toList(),
            deprecatedTokensDetected = deprecatedDetected.toList(),
            unsupportedTokensDetected = unsupportedDetected.toList()
        )
    }

    fun highlightForJdkVersion(
        source: String,
        jdkMajor: Int,
        isDark: Boolean
    ): AnnotatedString {
        val kwColor = if (isDark) SyntaxKeywordDark else SyntaxKeywordLight
        val modernKwColor = if (isDark) IdeCyanPrimary else Color(0xFF005B9F)
        val typeColor = if (isDark) SyntaxTypeDark else SyntaxTypeLight
        val modernTypeColor = if (isDark) IdeSuccessGreen else Color(0xFF1B7F37)
        val strColor = if (isDark) SyntaxStringDark else SyntaxStringLight
        val numColor = if (isDark) SyntaxNumberDark else SyntaxNumberLight
        val cmtColor = if (isDark) SyntaxCommentDark else SyntaxCommentLight
        val annColor = if (isDark) SyntaxAnnotationDark else SyntaxAnnotationLight
        val deprecatedColor = IdeAmberSecondary
        val unsupportedErrorColor = IdeErrorRed

        return buildAnnotatedString {
            append(source)

            // 1. Identifier & Keyword Tokens (Version-Specific Rules)
            val wordRegex = Regex("""\b[A-Za-z_][A-Za-z0-9_]*\b""")
            for (m in wordRegex.findAll(source)) {
                val w = m.value
                val start = m.range.first
                val end = m.range.last + 1

                val minKwMajor = VERSIONED_KEYWORDS[w]
                val minTypeMajor = VERSIONED_TYPES[w]
                val depMajor = DEPRECATED_SYMBOLS[w]

                when {
                    // Deprecated symbol in the selected JDK (e.g. Applet in JDK 9+, strictfp in JDK 17+)
                    depMajor != null && jdkMajor >= depMajor -> {
                        addStyle(
                            SpanStyle(
                                color = deprecatedColor,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.LineThrough
                            ),
                            start,
                            end
                        )
                    }
                    // Version-specific keyword (strictfp, assert, enum, var, yield, record, sealed, permits)
                    minKwMajor != null -> {
                        if (jdkMajor < minKwMajor) {
                            // Highlighted as Unsupported Future Syntax for selected JDK!
                            addStyle(
                                SpanStyle(
                                    color = unsupportedErrorColor,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline,
                                    background = unsupportedErrorColor.copy(alpha = 0.16f)
                                ),
                                start,
                                end
                            )
                        } else if (minKwMajor >= 5) {
                            // Newly introduced / modern Java keyword
                            addStyle(
                                SpanStyle(
                                    color = modernKwColor,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                start,
                                end
                            )
                        } else {
                            addStyle(
                                SpanStyle(color = kwColor, fontWeight = FontWeight.Bold),
                                start,
                                end
                            )
                        }
                    }
                    // Baseline Java 1.0 keyword
                    w in BASELINE_KEYWORDS_1_0 -> {
                        addStyle(
                            SpanStyle(color = kwColor, fontWeight = FontWeight.Bold),
                            start,
                            end
                        )
                    }
                    // Versioned Standard Library Type
                    minTypeMajor != null -> {
                        if (jdkMajor < minTypeMajor) {
                            // Type does not exist in selected historical JDK!
                            addStyle(
                                SpanStyle(
                                    color = unsupportedErrorColor,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline,
                                    background = unsupportedErrorColor.copy(alpha = 0.16f)
                                ),
                                start,
                                end
                            )
                        } else if (minTypeMajor >= 5) {
                            // Modern API Type (Scanner, StringBuilder, Stream, Optional, HttpClient, SequencedCollection)
                            addStyle(
                                SpanStyle(color = modernTypeColor, fontWeight = FontWeight.SemiBold),
                                start,
                                end
                            )
                        } else {
                            addStyle(
                                SpanStyle(color = typeColor, fontWeight = FontWeight.Medium),
                                start,
                                end
                            )
                        }
                    }
                }
            }

            // 2. Deprecated method invocations (Thread.stop, suspend, resume, finalize)
            for ((pat, depMajor) in DEPRECATED_METHODS) {
                if (jdkMajor >= depMajor) {
                    var idx = source.indexOf(pat)
                    while (idx >= 0) {
                        addStyle(
                            SpanStyle(
                                color = deprecatedColor,
                                textDecoration = TextDecoration.LineThrough,
                                fontWeight = FontWeight.Bold
                            ),
                            idx,
                            (idx + pat.length - 1).coerceAtMost(source.length)
                        )
                        idx = source.indexOf(pat, idx + pat.length)
                    }
                }
            }

            // 3. Lambda '->' and Method Reference '::' highlighting
            val opRegex = Regex("""->|::""")
            for (m in opRegex.findAll(source)) {
                if (jdkMajor < 8) {
                    addStyle(
                        SpanStyle(
                            color = unsupportedErrorColor,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            background = unsupportedErrorColor.copy(alpha = 0.2f)
                        ),
                        m.range.first,
                        m.range.last + 1
                    )
                } else {
                    addStyle(
                        SpanStyle(color = modernKwColor, fontWeight = FontWeight.Bold),
                        m.range.first,
                        m.range.last + 1
                    )
                }
            }

            // 4. Numeric literals (including binary 0b and underscore 1_000 in JDK 7+)
            val numRegex = Regex("""\b(?:0[bB][01_]+|\d[\d_]*(?:\.[\d_]+)?[LlDdFf]?)\b""")
            for (m in numRegex.findAll(source)) {
                val numText = m.value
                val usesJdk7NumSyntax = numText.contains("_") || numText.startsWith("0b", ignoreCase = true)
                if (usesJdk7NumSyntax && jdkMajor < 7) {
                    addStyle(
                        SpanStyle(
                            color = unsupportedErrorColor,
                            textDecoration = TextDecoration.Underline
                        ),
                        m.range.first,
                        m.range.last + 1
                    )
                } else {
                    addStyle(SpanStyle(color = numColor), m.range.first, m.range.last + 1)
                }
            }

            // 5. Annotations (@Override, @Deprecated, etc. — JDK 5+)
            val annRegex = Regex("""@[A-Za-z_][A-Za-z0-9_]*""")
            for (m in annRegex.findAll(source)) {
                if (jdkMajor < 5) {
                    addStyle(
                        SpanStyle(
                            color = unsupportedErrorColor,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            background = unsupportedErrorColor.copy(alpha = 0.18f)
                        ),
                        m.range.first,
                        m.range.last + 1
                    )
                } else {
                    addStyle(
                        SpanStyle(color = annColor, fontWeight = FontWeight.SemiBold),
                        m.range.first,
                        m.range.last + 1
                    )
                }
            }

            // 6. Standard Strings & JDK 15+ Text Blocks
            val strRegex = Regex(""""(?:\\.|[^"\\])*"""")
            for (m in strRegex.findAll(source)) {
                addStyle(SpanStyle(color = strColor), m.range.first, m.range.last + 1)
            }
            val tbRegex = Regex("""\"\"\"[\s\S]*?\"\"\"""")
            for (m in tbRegex.findAll(source)) {
                if (jdkMajor < 15) {
                    addStyle(
                        SpanStyle(
                            color = unsupportedErrorColor,
                            textDecoration = TextDecoration.Underline,
                            background = unsupportedErrorColor.copy(alpha = 0.15f)
                        ),
                        m.range.first,
                        m.range.last + 1
                    )
                } else {
                    addStyle(
                        SpanStyle(color = modernTypeColor, fontStyle = FontStyle.Italic),
                        m.range.first,
                        m.range.last + 1
                    )
                }
            }

            // 7. Single-line & Block Comments (including JDK 23+ Markdown /// comments)
            val lineCmtRegex = Regex("""//.*""")
            for (m in lineCmtRegex.findAll(source)) {
                val isMarkdownDoc = m.value.startsWith("///")
                if (isMarkdownDoc && jdkMajor >= 23) {
                    addStyle(
                        SpanStyle(color = modernKwColor, fontStyle = FontStyle.Italic),
                        m.range.first,
                        m.range.last + 1
                    )
                } else {
                    addStyle(
                        SpanStyle(color = cmtColor, fontStyle = FontStyle.Italic),
                        m.range.first,
                        m.range.last + 1
                    )
                }
            }
        }
    }

    private fun formatMajor(major: Int): String = if (major <= 4) "1.$major" else "$major"
}
