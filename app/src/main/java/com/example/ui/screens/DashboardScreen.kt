package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.content.TutorialsCatalog
import com.example.data.db.ExecutionHistoryEntity
import com.example.data.db.JdkInstallationEntity
import com.example.data.db.LessonProgressEntity
import com.example.data.db.ProjectEntity
import com.example.data.db.QuizResultEntity
import com.example.data.db.SourceFileEntity
import com.example.ui.StudioModule
import com.example.ui.StudioUiState
import com.example.ui.theme.IdeAmberSecondary
import com.example.ui.theme.IdeCyanPrimary
import com.example.ui.theme.IdeErrorRed
import com.example.ui.theme.IdeSuccessGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    uiState: StudioUiState,
    jdks: List<JdkInstallationEntity>,
    projects: List<ProjectEntity>,
    sourceFiles: List<SourceFileEntity>,
    lessonProgress: List<LessonProgressEntity>,
    quizResults: List<QuizResultEntity>,
    recentExecutions: List<ExecutionHistoryEntity>,
    onNavigateTo: (StudioModule) -> Unit,
    onOpenProject: (ProjectEntity) -> Unit,
    onQuickNewJavaFile: () -> Unit
) {
    val activeJdk = jdks.find { it.versionId == uiState.activeJdkVersionId }
    val completedLessonsCount = lessonProgress.count { it.isCompleted }
    val totalLessonsCount = TutorialsCatalog.allLessons.size
    val avgQuizPct = if (quizResults.isEmpty()) 100 else {
        (quizResults.sumOf { (it.score * 100) / it.totalQuestions.coerceAtLeast(1) } / quizResults.size)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Card
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_hero_card"),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.img_ide_hero_banner_1794108085755),
                    contentDescription = "Universal Java Studio IDE Hero Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x990D1117),
                                    Color(0xEB0D1117)
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(IdeAmberSecondary)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "JDK 1.0 – JDK 25+ OFFLINE IDE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F1400)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(IdeCyanPrimary.copy(alpha = 0.2f))
                                    .border(1.dp, IdeCyanPrimary, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ACTIVE: ${activeJdk?.displayName ?: uiState.activeJdkVersionId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IdeCyanPrimary
                                )
                            }
                        }

                        Text(
                            text = "Universal Java Studio",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Write, compile, and execute historical Java 2 (JDK 1.2) through modern Java 25 programs with offline IIT Kharagpur & Oracle tutorials, version-filtered API docs, and 25 practice labs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD0D7DE)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onNavigateTo(StudioModule.EDITOR) },
                                modifier = Modifier.testTag("hero_open_editor_button")
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Code Editor")
                            }
                            OutlinedButton(
                                onClick = { onNavigateTo(StudioModule.JDK_MANAGER) },
                                modifier = Modifier.testTag("hero_jdk_manager_button")
                            ) {
                                Icon(Icons.Default.DeveloperBoard, contentDescription = null, modifier = Modifier.size(16.dp), tint = IdeCyanPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("JDK Manager (${jdks.size})", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Telemetry Summary Grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 3
        ) {
            DashboardMetricCard(
                label = "Active JDK",
                value = uiState.activeJdkVersionId.replace("_", " "),
                subtext = "Bytecode ${activeJdk?.bytecodeVersion ?: "46.0"}",
                accent = IdeCyanPrimary,
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                label = "Installed JDKs",
                value = "${jdks.size} Releases",
                subtext = "JDK 1.0 to JDK 25+",
                accent = IdeAmberSecondary,
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                label = "Learning Progress",
                value = "$completedLessonsCount / $totalLessonsCount",
                subtext = "Quiz Avg: $avgQuizPct%",
                accent = IdeSuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Quick-Access IDE Modules Grid
        Text(
            text = "IDE & Learning Modules Quick Access",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2
        ) {
            QuickAccessModuleCard(
                title = "New Java Program",
                subtitle = "Create a .java source file in the editor",
                icon = Icons.Default.AddCircleOutline,
                tag = "quick_new_program",
                onClick = onQuickNewJavaFile,
                modifier = Modifier.weight(1f)
            )
            QuickAccessModuleCard(
                title = "New / Manage Projects",
                subtitle = "${projects.size} multi-version Java projects",
                icon = Icons.Default.CreateNewFolder,
                tag = "quick_projects",
                onClick = { onNavigateTo(StudioModule.PROJECTS) },
                modifier = Modifier.weight(1f)
            )
            QuickAccessModuleCard(
                title = "Universal JDK Manager",
                subtitle = "Configure JDK 1.0–25+ & 5-layer compatibility",
                icon = Icons.Default.DeveloperBoard,
                tag = "quick_jdk_manager",
                onClick = { onNavigateTo(StudioModule.JDK_MANAGER) },
                modifier = Modifier.weight(1f)
            )
            QuickAccessModuleCard(
                title = "Offline Java Tutorials",
                subtitle = "11 Chapters + Core APIs + Modern Java",
                icon = Icons.Default.MenuBook,
                tag = "quick_tutorials",
                onClick = { onNavigateTo(StudioModule.TUTORIALS) },
                modifier = Modifier.weight(1f)
            )
            QuickAccessModuleCard(
                title = "Offline API Documentation",
                subtitle = "Version-filtered Javadoc (Java 2 to 25)",
                icon = Icons.Default.LibraryBooks,
                tag = "quick_api_docs",
                onClick = { onNavigateTo(StudioModule.API_DOCS) },
                modifier = Modifier.weight(1f)
            )
            QuickAccessModuleCard(
                title = "Java Practice Laboratory",
                subtitle = "25 categorized runnable exercise labs",
                icon = Icons.Default.Science,
                tag = "quick_practice_lab",
                onClick = { onNavigateTo(StudioModule.PRACTICE_LAB) },
                modifier = Modifier.weight(1f)
            )
            QuickAccessModuleCard(
                title = "Java Version Explorer",
                subtitle = "Side-by-side comparison (1.2 vs 25, etc.)",
                icon = Icons.Default.Timeline,
                tag = "quick_version_explorer",
                onClick = { onNavigateTo(StudioModule.VERSION_EXPLORER) },
                modifier = Modifier.weight(1f)
            )
            QuickAccessModuleCard(
                title = "Quizzes & 35-Test Report",
                subtitle = "Assessments & executed verification suite",
                icon = Icons.Default.Verified,
                tag = "quick_quiz_tests",
                onClick = { onNavigateTo(StudioModule.RESOURCES_AND_TESTS) },
                modifier = Modifier.weight(1f)
            )
        }

        // Recent Multi-JDK Projects Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Multi-Version Java Projects (Per-Project JDK)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View All (${projects.size})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateTo(StudioModule.PROJECTS) }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            projects.take(6).forEach { project ->
                val isCurrent = project.id == uiState.activeProjectId
                val fileCount = sourceFiles.count { it.projectId == project.id }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenProject(project) }
                        .testTag("dashboard_project_${project.name.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = project.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${project.description} ($fileCount Java file(s))",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = project.jdkVersionId.replace("_", " "),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Recent Compilation & Execution Results
        Text(
            text = "Recent Compilation & Execution Log",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (recentExecutions.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "No builds logged in this session yet. Click 'Compile & Run' in the top bar to compile and execute the active Java program.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                recentExecutions.take(5).forEach { entry ->
                    val statusColor = when {
                        entry.status.contains("FAILED") || entry.status.contains("EXCEPTION") -> IdeErrorRed
                        else -> IdeSuccessGreen
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${entry.projectName} / ${entry.fileName} [${entry.jdkVersionId}]",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${entry.status} (${entry.executionTimeMs}ms)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColor
                                )
                            }
                            if (entry.runtimeOutput.isNotBlank()) {
                                Text(
                                    text = "Output: ${entry.runtimeOutput.lines().firstOrNull() ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    label: String,
    value: String,
    subtext: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickAccessModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
