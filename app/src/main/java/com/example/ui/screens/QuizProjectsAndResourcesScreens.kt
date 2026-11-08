package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.content.QuizCatalog
import com.example.data.db.JdkInstallationEntity
import com.example.data.db.OfflineResourceEntity
import com.example.data.db.ProjectEntity
import com.example.data.db.QuizResultEntity
import com.example.data.db.SourceFileEntity
import com.example.ui.StudioUiState
import com.example.ui.theme.IdeAmberSecondary
import com.example.ui.theme.IdeCyanPrimary
import com.example.ui.theme.IdeErrorRed
import com.example.ui.theme.IdeSuccessGreen
import com.example.ui.theme.JetBrainsMonoFamily

// =========================================================================
// MODULE 10 – QUIZ AND ASSESSMENT SCREEN
// =========================================================================

@Composable
fun QuizScreen(
    uiState: StudioUiState,
    quizResults: List<QuizResultEntity>,
    onSelectQuizSet: (String) -> Unit,
    onSelectAnswer: (String, Int) -> Unit,
    onSubmitQuiz: () -> Unit,
    onResetQuiz: () -> Unit
) {
    val quizSets = QuizCatalog.quizSets
    val activeSet = quizSets.find { it.id == uiState.selectedQuizSetId } ?: quizSets.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Java Assessment & Version Quiz Platform",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Includes Multiple-Choice, Fill-in-the-Blank, Output Prediction, Find-the-Error, Debugging, and Version Compatibility assessments saved to SQLite.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Quiz Set Selector Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quizSets.forEach { set ->
                val isSelected = set.id == activeSet.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onSelectQuizSet(set.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("quiz_set_tab_${set.id.lowercase()}")
                ) {
                    Column {
                        Text(
                            text = set.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = set.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (uiState.quizSubmitted && uiState.lastQuizScore != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Assessment Score: ${uiState.lastQuizScore} / ${activeSet.questions.size}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = IdeSuccessGreen
                        )
                        Text(
                            text = "Result persisted to SQLite quiz_results table.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    OutlinedButton(onClick = onResetQuiz) {
                        Text("Retake Quiz")
                    }
                }
            }
        }

        // Questions List
        activeSet.questions.forEachIndexed { idx, q ->
            val selectedOpt = uiState.currentQuizAnswers[q.id]
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Q${idx + 1} • ${q.questionType}",
                            style = MaterialTheme.typography.labelLarge,
                            color = IdeCyanPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = q.targetVersionOrChapter,
                            style = MaterialTheme.typography.labelSmall,
                            color = IdeAmberSecondary
                        )
                    }

                    Text(
                        text = q.prompt,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (q.codeSnippet != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = q.codeSnippet,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 12.sp
                            )
                        }
                    }

                    q.options.forEachIndexed { optIdx, optText ->
                        val isChosen = selectedOpt == optIdx
                        val isCorrect = optIdx == q.correctOptionIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        uiState.quizSubmitted && isCorrect -> IdeSuccessGreen.copy(alpha = 0.2f)
                                        uiState.quizSubmitted && isChosen && !isCorrect -> IdeErrorRed.copy(alpha = 0.2f)
                                        isChosen -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .clickable { onSelectAnswer(q.id, optIdx) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("quiz_option_${q.id}_$optIdx"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isChosen,
                                onClick = { onSelectAnswer(q.id, optIdx) }
                            )
                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (uiState.quizSubmitted) {
                        Text(
                            text = "Explanation: ${q.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = IdeCyanPrimary
                        )
                    }
                }
            }
        }

        if (!uiState.quizSubmitted) {
            Button(
                onClick = onSubmitQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_quiz_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit & Grade Assessment", fontWeight = FontWeight.Bold)
            }
        }

        // Previous SQLite Quiz Results History
        if (quizResults.isNotEmpty()) {
            Text(
                text = "Completed Assessment History (SQLite)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            quizResults.take(5).forEach { res ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(res.quizTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(
                            text = "Score: ${res.score} / ${res.totalQuestions}",
                            style = MaterialTheme.typography.labelLarge,
                            color = IdeSuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// MODULE 12 – PROJECT MANAGEMENT SCREEN (PER-PROJECT JDK PERSISTENCE)
// =========================================================================

@Composable
fun ProjectsScreen(
    uiState: StudioUiState,
    projects: List<ProjectEntity>,
    sourceFiles: List<SourceFileEntity>,
    jdks: List<JdkInstallationEntity>,
    onOpenProject: (ProjectEntity) -> Unit,
    onCreateProject: (String, String, String, String, String) -> Unit,
    onSwitchProjectJdk: (Long, String) -> Unit,
    onDeleteProject: (Long) -> Unit
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var projName by remember { mutableStateOf("LegacyJava2Lab") }
    var projDesc by remember { mutableStateOf("Custom multi-class Java project") }
    var projJdk by remember { mutableStateOf("JDK_1_2") }
    var projPkg by remember { mutableStateOf("com.studio.app") }
    var projMain by remember { mutableStateOf("MainApp") }
    var showJdkDropdownInDialog by remember { mutableStateOf(false) }

    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Create New Java Project (With Dedicated JDK)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = projName,
                        onValueChange = { projName = it },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier.testTag("new_project_name_input")
                    )
                    OutlinedTextField(
                        value = projDesc,
                        onValueChange = { projDesc = it },
                        label = { Text("Description") },
                        singleLine = true
                    )
                    Box {
                        OutlinedButton(
                            onClick = { showJdkDropdownInDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Project JDK: $projJdk")
                        }
                        DropdownMenu(
                            expanded = showJdkDropdownInDialog,
                            onDismissRequest = { showJdkDropdownInDialog = false },
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            jdks.forEach { jdk ->
                                DropdownMenuItem(
                                    text = { Text(jdk.displayName) },
                                    onClick = {
                                        projJdk = jdk.versionId
                                        showJdkDropdownInDialog = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = projPkg,
                        onValueChange = { projPkg = it },
                        label = { Text("Package Name (or default)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = projMain,
                        onValueChange = { projMain = it },
                        label = { Text("Main Class Name") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateProject(projName, projDesc, projJdk, projPkg, projMain)
                        showNewProjectDialog = false
                    },
                    modifier = Modifier.testTag("confirm_create_project_button")
                ) {
                    Text("Create Project")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Multi-Version Project Manager",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Every project remembers its own assigned JDK version in SQLite so you can maintain Java 2, Java 8, Java 17, Java 21, and Java 25 projects simultaneously.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { showNewProjectDialog = true },
                modifier = Modifier.testTag("new_project_button")
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Project")
            }
        }

        projects.forEach { project ->
            val isActive = project.id == uiState.activeProjectId
            val files = sourceFiles.filter { it.projectId == project.id }
            var showProjectJdkPicker by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = project.name + (if (isActive) " (Active Workspace)" else ""),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = project.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box {
                            OutlinedButton(
                                onClick = { showProjectJdkPicker = true }
                            ) {
                                Text("JDK: ${project.jdkVersionId}", style = MaterialTheme.typography.labelMedium)
                            }
                            DropdownMenu(
                                expanded = showProjectJdkPicker,
                                onDismissRequest = { showProjectJdkPicker = false },
                                modifier = Modifier.heightIn(max = 300.dp)
                            ) {
                                jdks.forEach { jdk ->
                                    DropdownMenuItem(
                                        text = { Text(jdk.displayName) },
                                        onClick = {
                                            onSwitchProjectJdk(project.id, jdk.versionId)
                                            showProjectJdkPicker = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Package: ${project.packageName} • Main Class: ${project.mainClass} • Source Files (${files.size}): ${files.joinToString { it.fileName }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = IdeCyanPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onOpenProject(project) },
                            modifier = Modifier.testTag("open_project_btn_${project.name.lowercase().replace(" ", "_")}")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in Editor")
                        }
                        if (projects.size > 1) {
                            OutlinedButton(onClick = { onDeleteProject(project.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// MODULE 11 & SECTION 20 – OFFLINE RESOURCE MANAGER & 35-TEST VERIFICATION REPORT
// =========================================================================

@Composable
fun ResourcesAndTestsScreen(
    uiState: StudioUiState,
    resources: List<OfflineResourceEntity>,
    onImportResource: (String, String, String, String, String) -> Unit,
    onRemoveResource: (Long) -> Unit,
    onRerunTests: () -> Unit
) {
    var showImportDialog by remember { mutableStateOf(false) }
    var resTitle by remember { mutableStateOf("JDK 1.2 Swing & Collections Archive") }
    var resCategory by remember { mutableStateOf("API Docs") }
    var resJdk by remember { mutableStateOf("JDK 1.2") }
    var resPath by remember { mutableStateOf("resources/java_1_2/swing_spec.html") }
    var resAttr by remember { mutableStateOf("Local Offline Educational Bundle") }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Offline Resource Bundle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = resTitle, onValueChange = { resTitle = it }, label = { Text("Resource Title") }, singleLine = true)
                    OutlinedTextField(value = resCategory, onValueChange = { resCategory = it }, label = { Text("Category (HTML Tutorial, API Docs, PDF, Examples)") }, singleLine = true)
                    OutlinedTextField(value = resJdk, onValueChange = { resJdk = it }, label = { Text("Target JDK Version") }, singleLine = true)
                    OutlinedTextField(value = resPath, onValueChange = { resPath = it }, label = { Text("Local Relative Path (resources/...)") }, singleLine = true)
                    OutlinedTextField(value = resAttr, onValueChange = { resAttr = it }, label = { Text("Copyright Attribution / License") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onImportResource(resTitle, resCategory, resJdk, resPath, resAttr)
                        showImportDialog = false
                    }
                ) {
                    Text("Import Offline")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val passedCount = uiState.verificationResults.count { it.status == "PASSED" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Offline Resource Manager Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Offline Resource Manager & Executed Test Suite",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage offline tutorials, API doc archives, and inspect the 35 executed application, JDK, compiler, and runtime verification tests.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { showImportDialog = true },
                modifier = Modifier.testTag("import_offline_resource_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Import Resource")
            }
        }

        // Offline Resources List
        resources.forEach { res ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(res.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${res.category} • Version: ${res.targetJdkVersion} • Path: ${res.resourcePath} • Status: ${res.status}",
                            style = MaterialTheme.typography.labelSmall,
                            color = IdeCyanPrimary
                        )
                        Text(
                            text = "Attribution: ${res.attribution}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onRemoveResource(res.id) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove Resource")
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        // Section 20: 35-Case Executed Testing Report
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Executed Verification Testing Report ($passedCount / ${uiState.verificationResults.size} PASSED)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = IdeSuccessGreen
                )
                Text(
                    text = "Tests are executed directly against JavaCompilerEngine, JavaExecutionEngine, JdkCatalogAndDetector, and Offline Catalogs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = onRerunTests,
                modifier = Modifier.testTag("rerun_35_tests_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Re-Run All 35 Tests")
            }
        }

        uiState.verificationResults.forEach { test ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${test.testCase} [${test.category}]",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = test.status,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (test.status == "PASSED") IdeSuccessGreen else IdeAmberSecondary
                        )
                    }
                    Text("Expected: ${test.expectedResult}", style = MaterialTheme.typography.bodySmall)
                    Text("Actual  : ${test.actualResult}", style = MaterialTheme.typography.labelMedium, color = IdeCyanPrimary)
                    Text("Remarks : ${test.remarks}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
