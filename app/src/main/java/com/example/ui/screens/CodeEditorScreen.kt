package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.compiler.CompilerDiagnostic
import com.example.core.compiler.DiagnosticSeverity
import com.example.core.compiler.JavaCompilerEngine
import com.example.core.editor.JavaVersionSyntaxLexer
import com.example.data.db.JdkInstallationEntity
import com.example.ui.StudioUiState
import com.example.ui.theme.IdeAmberSecondary
import com.example.ui.theme.IdeCyanPrimary
import com.example.ui.theme.IdeErrorRed
import com.example.ui.theme.IdeSuccessGreen
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SyntaxKeywordDark
import com.example.ui.theme.SyntaxKeywordLight

class JavaSyntaxVisualTransformation(
    private val jdkMajor: Int,
    private val isDark: Boolean
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotated = JavaVersionSyntaxLexer.highlightForJdkVersion(
            source = text.text,
            jdkMajor = jdkMajor,
            isDark = isDark
        )
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

fun highlightJavaSource(
    source: String,
    isDark: Boolean,
    jdkMajor: Int = 21
): AnnotatedString {
    return JavaVersionSyntaxLexer.highlightForJdkVersion(
        source = source,
        jdkMajor = jdkMajor,
        isDark = isDark
    )
}

@Composable
fun CodeEditorScreen(
    uiState: StudioUiState,
    activeJdk: JdkInstallationEntity,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onUpdateContent: (String) -> Unit,
    onSaveFile: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleFindReplace: () -> Unit,
    onUpdateFindReplace: (String, String) -> Unit,
    onExecuteReplaceAll: () -> Unit,
    onCreateNewFile: (String) -> Unit,
    onInsertSnippet: (String) -> Unit
) {
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("CustomProgram.java") }

    val activeTab = uiState.openTabs.getOrNull(uiState.activeTabIndex)
    val sourceText = activeTab?.content ?: ""

    // Real-time live JDK version & syntax diagnostics
    val liveDiagnostics = remember(sourceText, activeJdk.versionId) {
        val list = mutableListOf<CompilerDiagnostic>()
        if (sourceText.isNotBlank()) {
            JavaCompilerEngine.analyzeVersionCompatibility(
                fileName = activeTab?.fileName ?: "Main.java",
                sourceCode = sourceText,
                jdk = activeJdk,
                diagnostics = list
            )
        }
        list
    }

    // QScintilla-equivalent version-aware lexer summary
    val lexerSummary = remember(sourceText, activeJdk.versionId) {
        JavaVersionSyntaxLexer.analyzeAndSummarize(
            source = sourceText,
            jdkMajor = activeJdk.majorNumber,
            jdkDisplayName = activeJdk.displayName
        )
    }

    val syntaxVisualTransformation = remember(activeJdk.majorNumber, uiState.isDarkTheme) {
        JavaSyntaxVisualTransformation(
            jdkMajor = activeJdk.majorNumber,
            isDark = uiState.isDarkTheme
        )
    }

    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New Java Source File") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Target Project JDK: ${activeJdk.displayName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it },
                        label = { Text("Class / File Name (.java)") },
                        singleLine = true,
                        modifier = Modifier.testTag("new_file_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateNewFile(newFileNameInput)
                        showNewFileDialog = false
                    },
                    modifier = Modifier.testTag("confirm_create_file_button")
                ) {
                    Text("Create File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Open File Tabs Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            uiState.openTabs.forEachIndexed { idx, tab ->
                val isSelected = idx == uiState.activeTabIndex
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.background
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                        .clickable { onSelectTab(idx) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("editor_tab_${tab.fileName}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (tab.isDirty) "${tab.fileName} *" else tab.fileName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (uiState.openTabs.size > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tab",
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onCloseTab(idx) }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { showNewFileDialog = true },
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("editor_new_file_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New File", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New .java", style = MaterialTheme.typography.labelMedium)
            }
        }

        // 2. Editor Operations & Quick Symbol / Template Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onSaveFile,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("editor_save_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save File", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(
                onClick = onUndo,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("editor_undo_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
            }
            IconButton(
                onClick = onRedo,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("editor_redo_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
            }
            IconButton(
                onClick = onToggleFindReplace,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("editor_find_replace_button")
            ) {
                Icon(Icons.Default.FindReplace, contentDescription = "Find and Replace")
            }

            // Quick Bracket / Snippet Insertion Pills
            val snippets = listOf(
                "println" to "        System.out.println(\"Output\");",
                "for-loop" to "        for (int i = 0; i < 5; i++) {\n            System.out.println(i);\n        }",
                "try-catch" to "        try {\n            int r = 10 / 2;\n        } catch (Exception e) {\n            System.out.println(e);\n        }"
            )
            snippets.forEach { (label, code) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onInsertSnippet(code) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text("+ $label", style = MaterialTheme.typography.labelSmall)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable {
                        onUpdateContent(
                            """public class HelloWorld {
    public static void main(String args[]) {
        System.out.println("Hello World");
    }
}"""
                        )
                    }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("load_java2_template_chip")
            ) {
                Text(
                    text = "Java 2 HelloWorld",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        onUpdateContent(
                            """import java.applet.Applet;
import java.util.*;

record ModernPoint(int x, int y) {}

public class VersionSyntaxShowcase {
    @Override
    public String toString() {
        return "Showcase";
    }

    public static void main(String[] args) {
        var point = new ModernPoint(10, 20);
        List<String> items = Arrays.asList("Java 2", "Java 8", "Java 25");
        items.stream().forEach(System.out::println);
        System.out.println("Record: " + point);
    }
}"""
                        )
                    }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("load_version_showcase_chip")
            ) {
                Text(
                    text = "Syntax Showcase (Switch JDK to Compare)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. Version-Specific Syntax Lexer Legend & Token Telemetry Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .testTag("version_syntax_lexer_bar"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Lexer Ruleset: ${activeJdk.versionId.replace("_", " ")} (${lexerSummary.activeKeywordCount} Keywords)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Legend Badges
            Text(
                text = "■ Baseline Keyword",
                style = MaterialTheme.typography.labelSmall,
                color = if (uiState.isDarkTheme) SyntaxKeywordDark else SyntaxKeywordLight
            )
            Text(
                text = "■ Modern Feature (${lexerSummary.modernFeatureTokensDetected.size})",
                style = MaterialTheme.typography.labelSmall,
                color = IdeCyanPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "■ Deprecated/Obsolete (${lexerSummary.deprecatedTokensDetected.size})",
                style = MaterialTheme.typography.labelSmall,
                color = IdeAmberSecondary,
                textDecoration = TextDecoration.LineThrough
            )
            Text(
                text = "■ Unsupported in ${activeJdk.versionId.replace("_", " ")} (${lexerSummary.unsupportedTokensDetected.size})",
                style = MaterialTheme.typography.labelSmall,
                color = IdeErrorRed,
                textDecoration = TextDecoration.Underline
            )
        }

        // 4. Optional Find & Replace Bar
        AnimatedVisibility(visible = uiState.showFindReplaceBar) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.findQuery,
                    onValueChange = { onUpdateFindReplace(it, uiState.replaceQuery) },
                    label = { Text("Find") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.replaceQuery,
                    onValueChange = { onUpdateFindReplace(uiState.findQuery, it) },
                    label = { Text("Replace With") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = onExecuteReplaceAll) {
                    Text("Replace All")
                }
            }
        }

        // 5. Live JDK Compatibility & Deprecated/Modern Token Banner
        Surface(
            color = if (liveDiagnostics.any { it.severity == DiagnosticSeverity.ERROR })
                IdeErrorRed.copy(alpha = 0.16f)
            else if (liveDiagnostics.isNotEmpty() || lexerSummary.deprecatedTokensDetected.isNotEmpty())
                IdeAmberSecondary.copy(alpha = 0.16f)
            else
                IdeSuccessGreen.copy(alpha = 0.10f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val firstIssue = liveDiagnostics.firstOrNull()
                    if (firstIssue == null) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = IdeSuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Syntax & API compatible with ${activeJdk.displayName} [Bytecode ${activeJdk.bytecodeVersion}]",
                            style = MaterialTheme.typography.labelMedium,
                            color = IdeSuccessGreen
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (firstIssue.severity == DiagnosticSeverity.ERROR) IdeErrorRed else IdeAmberSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Line ${firstIssue.line}: ${firstIssue.message}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (firstIssue.severity == DiagnosticSeverity.ERROR) IdeErrorRed else IdeAmberSecondary
                        )
                    }
                }

                if (lexerSummary.modernFeatureTokensDetected.isNotEmpty() || lexerSummary.deprecatedTokensDetected.isNotEmpty()) {
                    val notes = buildList {
                        if (lexerSummary.modernFeatureTokensDetected.isNotEmpty()) {
                            add("Modern tokens: ${lexerSummary.modernFeatureTokensDetected.take(4).joinToString(", ")}")
                        }
                        if (lexerSummary.deprecatedTokensDetected.isNotEmpty()) {
                            add("Deprecated in ${activeJdk.versionId}: ${lexerSummary.deprecatedTokensDetected.joinToString(", ")}")
                        }
                    }.joinToString("  •  ")
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

        // 6. Main Code Editor Area with Synchronized Line Numbers & Version-Specific Syntax Highlighting
        val editorScrollState = rememberScrollState()
        val lineCount = sourceText.lines().size.coerceAtLeast(1)

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(editorScrollState)
                .padding(vertical = 8.dp)
        ) {
            // Line Number Gutter
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .padding(end = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (lineNum in 1..lineCount) {
                    val hasLineError = liveDiagnostics.any { it.line == lineNum && it.severity == DiagnosticSeverity.ERROR }
                    val hasLineWarn = liveDiagnostics.any { it.line == lineNum && it.severity == DiagnosticSeverity.WARNING }
                    Text(
                        text = lineNum.toString(),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = when {
                            hasLineError -> IdeErrorRed
                            hasLineWarn -> IdeAmberSecondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        fontWeight = if (hasLineError || hasLineWarn) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Vertical Gutter Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height((lineCount * 20 + 40).dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            )

            // Editable Monospace Java Buffer with Dynamic Version-Aware Highlighting
            BasicTextField(
                value = sourceText,
                onValueChange = onUpdateContent,
                textStyle = TextStyle(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = syntaxVisualTransformation,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .testTag("java_code_editor_input")
            )
        }
    }
}
