package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.compiler.DiagnosticSeverity
import com.example.data.db.JdkInstallationEntity
import com.example.data.db.ProjectEntity
import com.example.ui.BottomConsoleTab
import com.example.ui.StudioModule
import com.example.ui.StudioUiState
import com.example.ui.theme.IdeAmberSecondary
import com.example.ui.theme.IdeCyanPrimary
import com.example.ui.theme.IdeErrorRed
import com.example.ui.theme.IdeSuccessGreen
import com.example.ui.theme.JetBrainsMonoFamily

fun moduleIcon(module: StudioModule): ImageVector = when (module) {
    StudioModule.DASHBOARD -> Icons.Default.Dashboard
    StudioModule.EDITOR -> Icons.Default.Code
    StudioModule.JDK_MANAGER -> Icons.Default.DeveloperBoard
    StudioModule.TUTORIALS -> Icons.Default.MenuBook
    StudioModule.API_DOCS -> Icons.Default.LibraryBooks
    StudioModule.VERSION_EXPLORER -> Icons.Default.Timeline
    StudioModule.PRACTICE_LAB -> Icons.Default.Science
    StudioModule.QUIZ -> Icons.Default.Quiz
    StudioModule.PROJECTS -> Icons.Default.Folder
    StudioModule.RESOURCES_AND_TESTS -> Icons.Default.Verified
}

@Composable
fun IdeTopToolbar(
    uiState: StudioUiState,
    activeProject: ProjectEntity?,
    jdks: List<JdkInstallationEntity>,
    onSelectJdk: (String) -> Unit,
    onCompile: () -> Unit,
    onRun: () -> Unit,
    onStop: () -> Unit,
    onToggleTheme: () -> Unit,
    onNavigateTo: (StudioModule) -> Unit
) {
    var showJdkDropdown by remember { mutableStateOf(false) }
    val activeJdk = jdks.find { it.versionId == uiState.activeJdkVersionId }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand & Active Project Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTo(StudioModule.DASHBOARD) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = "Universal Java Studio Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "UNIVERSAL JAVA STUDIO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Project: ${activeProject?.name ?: "Java Basics"} • Bytecode ${activeJdk?.bytecodeVersion ?: "46.0"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // JDK Version Selector Dropdown
                Box {
                    OutlinedButton(
                        onClick = { showJdkDropdown = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("toolbar_jdk_selector")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperBoard,
                            contentDescription = "Select JDK Version",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.activeJdkVersionId.replace("_", " "),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    DropdownMenu(
                        expanded = showJdkDropdown,
                        onDismissRequest = { showJdkDropdown = false },
                        modifier = Modifier.heightIn(max = 360.dp)
                    ) {
                        jdks.forEach { jdk ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = jdk.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (jdk.versionId == uiState.activeJdkVersionId) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = "Bytecode ${jdk.bytecodeVersion} • ${jdk.vendor}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectJdk(jdk.versionId)
                                    showJdkDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Theme Toggle Button
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Dark or Light IDE Theme",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Secondary Action Strip: Compile, Run, Stop & Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCompile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("toolbar_compile_button")
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Compile", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compile (javac)", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = onRun,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IdeSuccessGreen,
                        contentColor = Color(0xFF002410)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("toolbar_run_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compile & Run", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }

                if (uiState.isCompilingOrRunning) {
                    OutlinedButton(
                        onClick = onStop,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("toolbar_stop_button")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = IdeErrorRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Text(
                    text = uiState.statusBannerMessage,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun IdeModuleNavigationStrip(
    currentModule: StudioModule,
    onSelectModule: (StudioModule) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StudioModule.entries.forEach { module ->
            val selected = module == currentModule
            val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            val textColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            val borderMod = if (selected) {
                Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            } else {
                Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .then(borderMod)
                    .clickable { onSelectModule(module) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("nav_tab_${module.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = moduleIcon(module),
                    contentDescription = module.title,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun IdeBottomConsolePanel(
    uiState: StudioUiState,
    onSelectTab: (BottomConsoleTab) -> Unit,
    onToggleExpand: () -> Unit,
    onClearConsole: () -> Unit,
    onUpdateStdinAndArgs: (String, String) -> Unit
) {
    val diagCount = uiState.lastCompilationResult?.diagnostics?.size ?: 0
    val errorCount = uiState.lastCompilationResult?.diagnostics?.count { it.severity == DiagnosticSeverity.ERROR } ?: 0

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            // Console Tab Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("console_expand_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.isBottomPanelExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = "Expand or Collapse Console"
                        )
                    }

                    BottomConsoleTab.entries.forEach { tab ->
                        val isSelected = uiState.activeBottomTab == tab
                        val label = when (tab) {
                            BottomConsoleTab.ERROR_CONSOLE -> "${tab.label} ($diagCount)"
                            else -> tab.label
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable { onSelectTab(tab) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("bottom_tab_${tab.name.lowercase()}")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    tab == BottomConsoleTab.ERROR_CONSOLE && errorCount > 0 -> IdeErrorRed
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onClearConsole() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            AnimatedVisibility(visible = uiState.isBottomPanelExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(10.dp)
                ) {
                    when (uiState.activeBottomTab) {
                        BottomConsoleTab.COMPILER_OUTPUT -> {
                            val text = uiState.lastCompilationResult?.formattedConsoleOutput
                                ?: "Compiler Ready (${uiState.activeJdkVersionId}). Click 'Compile (javac)' or 'Compile & Run' in the top bar."
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(
                                    text = text,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        BottomConsoleTab.PROGRAM_OUTPUT -> {
                            val exec = uiState.lastExecutionResult
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                if (exec == null) {
                                    Text(
                                        text = "Program Output Console (${uiState.activeJdkVersionId}). Click 'Compile & Run' to execute the active class.",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "> ${exec.runtimeCommand}",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 11.sp,
                                        color = IdeCyanPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (exec.stdout.isNotEmpty()) {
                                        Text(
                                            text = exec.stdout,
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp,
                                            color = IdeSuccessGreen,
                                            modifier = Modifier.testTag("console_stdout_text")
                                        )
                                    }
                                    if (exec.stderr.isNotEmpty()) {
                                        Text(
                                            text = exec.stderr,
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 12.sp,
                                            color = IdeErrorRed
                                        )
                                    }
                                    if (exec.guiSnapshotTitle != null || exec.guiSnapshotComponents.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "[GUI Window Preview: ${exec.guiSnapshotTitle ?: "Frame"}] Components: ${exec.guiSnapshotComponents.joinToString(" | ")}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = IdeAmberSecondary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Process finished with exit code ${exec.exitCode} (${exec.executionTimeMs} ms)",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        BottomConsoleTab.ERROR_CONSOLE -> {
                            val diags = uiState.lastCompilationResult?.diagnostics ?: emptyList()
                            val runtimeErr = uiState.lastExecutionResult?.exceptionTrace
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                if (diags.isEmpty() && runtimeErr == null) {
                                    Text(
                                        text = "No compiler errors, version compatibility violations, or runtime exceptions detected.",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 12.sp,
                                        color = IdeSuccessGreen
                                    )
                                } else {
                                    diags.forEach { d ->
                                        val col = when (d.severity) {
                                            DiagnosticSeverity.ERROR -> IdeErrorRed
                                            DiagnosticSeverity.WARNING -> IdeAmberSecondary
                                            DiagnosticSeverity.INFO -> IdeCyanPrimary
                                        }
                                        Text(
                                            text = "[${d.severity}] Line ${d.line}: ${d.message}" +
                                                (d.minimumJdkRequired?.let { " (Requires $it)" } ?: ""),
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 12.sp,
                                            color = col,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                    if (runtimeErr != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = runtimeErr,
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 12.sp,
                                            color = IdeErrorRed
                                        )
                                    }
                                }
                            }
                        }

                        BottomConsoleTab.BYTECODE_INSPECTOR -> {
                            val classes = uiState.lastCompilationResult?.generatedClasses ?: emptyList()
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                if (classes.isEmpty()) {
                                    Text(
                                        text = "Compile a Java file to inspect its generated .class CAFEBABE header, major.minor version, and constant pool.",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    classes.forEach { cls ->
                                        Text(
                                            text = cls.disassemblyPreview,
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }

                        BottomConsoleTab.STDIN_AND_ARGS -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.stdinInput,
                                    onValueChange = { onUpdateStdinAndArgs(it, uiState.programArgsInput) },
                                    label = { Text("Standard Input (System.in / Scanner)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("stdin_input_field"),
                                    maxLines = 4
                                )
                                OutlinedTextField(
                                    value = uiState.programArgsInput,
                                    onValueChange = { onUpdateStdinAndArgs(uiState.stdinInput, it) },
                                    label = { Text("Command-Line Args (String[] args)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("program_args_field"),
                                    maxLines = 4
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
