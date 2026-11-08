package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.content.JavaReleaseProfile
import com.example.data.content.PracticeExercise
import com.example.data.content.PracticeLabCatalog
import com.example.data.content.VersionExplorerCatalog
import com.example.ui.StudioUiState
import com.example.ui.theme.IdeAmberSecondary
import com.example.ui.theme.IdeCyanPrimary
import com.example.ui.theme.IdeSuccessGreen
import com.example.ui.theme.JetBrainsMonoFamily
import kotlin.math.abs

enum class ExplorerPresentationMode(val label: String) {
    COMBINED("Tabular + Graphical"),
    TABULAR("Tabular Matrix"),
    GRAPHICAL("Graphical Charts")
}

@Composable
fun VersionExplorerScreen(
    uiState: StudioUiState,
    onSelectComparisonPair: (String, String) -> Unit
) {
    var showLeftDropdown by remember { mutableStateOf(false) }
    var showRightDropdown by remember { mutableStateOf(false) }
    var presentationMode by remember { mutableStateOf(ExplorerPresentationMode.COMBINED) }

    val leftProfile = VersionExplorerCatalog.findProfile(uiState.compareVersionLeftId)
    val rightProfile = VersionExplorerCatalog.findProfile(uiState.compareVersionRightId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Java Version Explorer & Release Comparison (JDK 1.0 – JDK 25)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select any two Java versions (such as Java 8 and Java 17) to inspect a side-by-side tabular and graphical comparison of major language changes, new APIs, deprecated/removed APIs, JVM/GC advancements, and bytecode evolution.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Milestone Quick-Compare Preset Chips (including Java 8 vs Java 17)
        Text(
            text = "Milestone Comparison Presets",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = IdeCyanPrimary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VersionExplorerCatalog.presetPairs.forEach { (leftId, rightId) ->
                val isSelected = uiState.compareVersionLeftId == leftId && uiState.compareVersionRightId == rightId
                val label = "${leftId.replace("JDK_", "Java ").replace("_", ".")} vs ${rightId.replace("JDK_", "Java ").replace("_", ".")}"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onSelectComparisonPair(leftId, rightId) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("version_preset_${leftId}_vs_${rightId}")
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Custom Version A vs Version B Dropdown Selectors + Presentation Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { showLeftDropdown = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("version_explorer_left_selector")
                ) {
                    Text("Baseline: ${leftProfile.shortName}")
                }
                DropdownMenu(
                    expanded = showLeftDropdown,
                    onDismissRequest = { showLeftDropdown = false },
                    modifier = Modifier.heightIn(max = 320.dp)
                ) {
                    VersionExplorerCatalog.releases.forEach { rel ->
                        DropdownMenuItem(
                            text = { Text(rel.fullName) },
                            onClick = {
                                onSelectComparisonPair(rel.versionId, uiState.compareVersionRightId)
                                showLeftDropdown = false
                            }
                        )
                    }
                }
            }

            Icon(Icons.Default.CompareArrows, contentDescription = null, tint = IdeAmberSecondary)

            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { showRightDropdown = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("version_explorer_right_selector")
                ) {
                    Text("Target: ${rightProfile.shortName}")
                }
                DropdownMenu(
                    expanded = showRightDropdown,
                    onDismissRequest = { showRightDropdown = false },
                    modifier = Modifier.heightIn(max = 320.dp)
                ) {
                    VersionExplorerCatalog.releases.forEach { rel ->
                        DropdownMenuItem(
                            text = { Text(rel.fullName) },
                            onClick = {
                                onSelectComparisonPair(uiState.compareVersionLeftId, rel.versionId)
                                showRightDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Presentation Mode Filter Chips (Tabular + Graphical / Tabular / Graphical)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExplorerPresentationMode.entries.forEach { mode ->
                FilterChip(
                    selected = presentationMode == mode,
                    onClick = { presentationMode = mode },
                    label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (mode == ExplorerPresentationMode.GRAPHICAL) Icons.Default.BarChart else Icons.Default.TableChart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("version_mode_${mode.name.lowercase()}")
                )
            }
        }

        // Graphical Comparison Section
        if (presentationMode == ExplorerPresentationMode.COMBINED || presentationMode == ExplorerPresentationMode.GRAPHICAL) {
            GraphicalVersionComparisonCard(
                leftProfile = leftProfile,
                rightProfile = rightProfile
            )
        }

        // Tabular Side-by-Side Comparison Section
        if (presentationMode == ExplorerPresentationMode.COMBINED || presentationMode == ExplorerPresentationMode.TABULAR) {
            TabularVersionComparisonSection(
                leftProfile = leftProfile,
                rightProfile = rightProfile
            )
        }
    }
}

@Composable
private fun GraphicalVersionComparisonCard(
    leftProfile: JavaReleaseProfile,
    rightProfile: JavaReleaseProfile
) {
    val bytecodeDelta = rightProfile.bytecodeMajor - leftProfile.bytecodeMajor
    val keywordDelta = rightProfile.keywordCount - leftProfile.keywordCount
    val gcDelta = rightProfile.gcEfficiencyScore - leftProfile.gcEfficiencyScore

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("version_graphical_comparison_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Graphical Evolution Comparison: ${leftProfile.shortName} vs ${rightProfile.shortName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IdeCyanPrimary
                )
                Text(
                    text = "Δ Bytecode: ${if (bytecodeDelta >= 0) "+$bytecodeDelta" else "$bytecodeDelta"} versions",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = IdeAmberSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(IdeAmberSecondary))
                    Text("${leftProfile.shortName} (${leftProfile.releaseDate})", style = MaterialTheme.typography.labelSmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(IdeSuccessGreen))
                    Text("${rightProfile.shortName} (${rightProfile.releaseDate})", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Dual Metric Bars
            DualMetricBar(
                metricLabel = "Class File Bytecode Major Version (CAFEBABE)",
                leftName = leftProfile.shortName,
                leftDisplay = "v${leftProfile.bytecodeVersion}",
                leftRatio = (leftProfile.bytecodeMajor - 40f) / 32f,
                rightName = rightProfile.shortName,
                rightDisplay = "v${rightProfile.bytecodeVersion}",
                rightRatio = (rightProfile.bytecodeMajor - 40f) / 32f
            )

            DualMetricBar(
                metricLabel = "Reserved & Contextual Language Keywords",
                leftName = leftProfile.shortName,
                leftDisplay = "${leftProfile.keywordCount} keywords",
                leftRatio = leftProfile.keywordCount / 65f,
                rightName = rightProfile.shortName,
                rightDisplay = "${rightProfile.keywordCount} keywords (${if (keywordDelta >= 0) "+$keywordDelta" else "$keywordDelta"})",
                rightRatio = rightProfile.keywordCount / 65f
            )

            DualMetricBar(
                metricLabel = "Standard Library Core Packages & Modules",
                leftName = leftProfile.shortName,
                leftDisplay = "${leftProfile.coreApiModulesCount} packages",
                leftRatio = leftProfile.coreApiModulesCount / 240f,
                rightName = rightProfile.shortName,
                rightDisplay = "${rightProfile.coreApiModulesCount} packages",
                rightRatio = rightProfile.coreApiModulesCount / 240f
            )

            DualMetricBar(
                metricLabel = "JVM Runtime & GC Low-Pause Efficiency Index",
                leftName = leftProfile.shortName,
                leftDisplay = "${leftProfile.gcEfficiencyScore}/100",
                leftRatio = leftProfile.gcEfficiencyScore / 100f,
                rightName = rightProfile.shortName,
                rightDisplay = "${rightProfile.gcEfficiencyScore}/100 (${if (gcDelta >= 0) "+$gcDelta" else "$gcDelta"} pts)",
                rightRatio = rightProfile.gcEfficiencyScore / 100f
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            Text(
                text = "Bytecode Major Version Evolution Trajectory (JDK 1.0 [45] → JDK 25 [69])",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Custom Canvas Bar Chart across all 26 Java Releases highlighting Left & Right selections
            val releases = VersionExplorerCatalog.releases
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            val neutralBarColor = IdeCyanPrimary.copy(alpha = 0.38f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val count = releases.size
                    val spacing = 4.dp.toPx()
                    val barWidth = ((size.width - spacing * (count - 1)) / count).coerceAtLeast(4f)

                    // Horizontal reference lines
                    for (lineIdx in 1..3) {
                        val y = size.height * (lineIdx / 4f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }

                    releases.forEachIndexed { idx, profile ->
                        val normalized = ((profile.bytecodeMajor - 42f) / (70f - 42f)).coerceIn(0.1f, 1f)
                        val barHeight = size.height * normalized
                        val x = idx * (barWidth + spacing)
                        val y = size.height - barHeight

                        val color = when (profile.versionId) {
                            leftProfile.versionId -> IdeAmberSecondary
                            rightProfile.versionId -> IdeSuccessGreen
                            else -> neutralBarColor
                        }

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("JDK 1.0 (v45.3)", fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Java 8 (v52.0)", fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Java 17 (v61.0)", fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Java 25 (v69.0)", fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DualMetricBar(
    metricLabel: String,
    leftName: String,
    leftDisplay: String,
    leftRatio: Float,
    rightName: String,
    rightDisplay: String,
    rightRatio: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = metricLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        // Left Version Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = leftName,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = IdeAmberSecondary,
                modifier = Modifier.width(88.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(leftRatio.coerceIn(0.06f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(IdeAmberSecondary)
                )
            }
            Text(
                text = leftDisplay,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                modifier = Modifier.width(120.dp)
            )
        }
        // Right Version Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = rightName,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = IdeSuccessGreen,
                modifier = Modifier.width(88.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(rightRatio.coerceIn(0.06f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(IdeSuccessGreen)
                )
            }
            Text(
                text = rightDisplay,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                modifier = Modifier.width(120.dp)
            )
        }
    }
}

@Composable
private fun TabularVersionComparisonSection(
    leftProfile: JavaReleaseProfile,
    rightProfile: JavaReleaseProfile
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("version_tabular_comparison_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Table Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "COMPARISON DIMENSION",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = IdeCyanPrimary,
                modifier = Modifier.weight(0.8f)
            )
            Text(
                text = "BASELINE: ${leftProfile.shortName.uppercase()}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = IdeAmberSecondary,
                modifier = Modifier.weight(1.1f)
            )
            Text(
                text = "TARGET: ${rightProfile.shortName.uppercase()}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = IdeSuccessGreen,
                modifier = Modifier.weight(1.1f)
            )
        }

        VersionComparisonRowCard(
            dimension = "Release, Codename & Support Tier",
            leftTitle = leftProfile.shortName,
            leftValue = "${leftProfile.fullName}\nReleased: ${leftProfile.releaseDate} • Codename: ${leftProfile.codename}\nTier: ${if (leftProfile.isLts) "Long-Term Support (LTS)" else "Feature / Standard Release"}",
            rightTitle = rightProfile.shortName,
            rightValue = "${rightProfile.fullName}\nReleased: ${rightProfile.releaseDate} • Codename: ${rightProfile.codename}\nTier: ${if (rightProfile.isLts) "Long-Term Support (LTS)" else "Feature / Standard Release"}"
        )
        VersionComparisonRowCard(
            dimension = "Bytecode Version (Class File Header)",
            leftTitle = leftProfile.shortName,
            leftValue = "CAFEBABE Class Major.Minor: ${leftProfile.bytecodeVersion}\nMax Supported Class File Version: ${leftProfile.bytecodeMajor}.0",
            rightTitle = rightProfile.shortName,
            rightValue = "CAFEBABE Class Major.Minor: ${rightProfile.bytecodeVersion}\nMax Supported Class File Version: ${rightProfile.bytecodeMajor}.0 (Δ ${abs(rightProfile.bytecodeMajor - leftProfile.bytecodeMajor)} class versions)"
        )
        VersionComparisonRowCard(
            dimension = "Major Language Changes",
            leftTitle = leftProfile.shortName,
            leftValue = leftProfile.languageChanges,
            rightTitle = rightProfile.shortName,
            rightValue = rightProfile.languageChanges
        )
        VersionComparisonRowCard(
            dimension = "New API Additions",
            leftTitle = leftProfile.shortName,
            leftValue = leftProfile.apiAdditions,
            rightTitle = rightProfile.shortName,
            rightValue = rightProfile.apiAdditions
        )
        VersionComparisonRowCard(
            dimension = "Deprecated or Removed APIs",
            leftTitle = leftProfile.shortName,
            leftValue = leftProfile.deprecatedOrRemovedApis,
            rightTitle = rightProfile.shortName,
            rightValue = rightProfile.deprecatedOrRemovedApis
        )
        VersionComparisonRowCard(
            dimension = "JVM Improvements & Garbage Collection",
            leftTitle = leftProfile.shortName,
            leftValue = leftProfile.jvmAndGcChanges,
            rightTitle = rightProfile.shortName,
            rightValue = rightProfile.jvmAndGcChanges
        )
        VersionComparisonRowCard(
            dimension = "Compiler (javac) & Tooling Changes",
            leftTitle = leftProfile.shortName,
            leftValue = leftProfile.compilerAndToolingChanges,
            rightTitle = rightProfile.shortName,
            rightValue = rightProfile.compilerAndToolingChanges
        )
    }
}

@Composable
private fun VersionComparisonRowCard(
    dimension: String,
    leftTitle: String,
    leftValue: String,
    rightTitle: String,
    rightValue: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = dimension,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = IdeCyanPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, IdeAmberSecondary.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = leftTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = IdeAmberSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = leftValue, style = MaterialTheme.typography.bodySmall)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, IdeSuccessGreen.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = rightTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = IdeSuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = rightValue, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// =========================================================================
// MODULE 9 – JAVA PRACTICE LABORATORY (ALL 25 CATEGORIES)
// =========================================================================

@Composable
fun PracticeLabScreen(
    uiState: StudioUiState,
    onSelectExercise: (PracticeExercise) -> Unit,
    onUpdatePracticeBuffer: (String) -> Unit,
    onCompileAndRunExercise: (PracticeExercise) -> Unit,
    onOpenExerciseInEditor: (String, String, String) -> Unit
) {
    val exercises = PracticeLabCatalog.exercises
    val activeExercise = exercises.find { it.id == uiState.selectedExerciseId } ?: exercises.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Java Practice Laboratory (25 Categorized Exercise Labs)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select any of the 25 programming categories, modify the Java source code directly below, and click 'Compile & Run Exercise'.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 25-Category Horizontal Selector Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            exercises.forEach { ex ->
                val isSelected = ex.id == activeExercise.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onSelectExercise(ex) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("practice_category_${ex.categoryNumber}")
                ) {
                    Column {
                        Text(
                            text = ex.categoryName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target: ${ex.targetJdkId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Problem Statement & Algorithm Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
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
                    Text(
                        text = "${activeExercise.categoryName}: ${activeExercise.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IdeCyanPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeExercise.compatibleJdkRange,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Text(
                    text = "Problem Statement: ${activeExercise.problemStatement}",
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                Text(
                    text = "Algorithm:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = IdeAmberSecondary
                )
                activeExercise.algorithmSteps.forEachIndexed { idx, step ->
                    Text(
                        text = "${idx + 1}. $step",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Editable Java Source Code Area + Compile & Run Buttons
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
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
                    Text(
                        text = "Interactive Lab Source Code (${activeExercise.targetJdkId})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { onUpdatePracticeBuffer(activeExercise.sourceCode) }
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Code", style = MaterialTheme.typography.labelSmall)
                    }
                }

                OutlinedTextField(
                    value = uiState.practiceEditorBuffer,
                    onValueChange = onUpdatePracticeBuffer,
                    textStyle = TextStyle(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .testTag("practice_lab_code_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onCompileAndRunExercise(activeExercise) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IdeSuccessGreen,
                            contentColor = Color(0xFF002410)
                        ),
                        modifier = Modifier.testTag("practice_compile_run_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compile & Run Exercise", fontWeight = FontWeight.Bold)
                    }

                    val className = Regex("""class\s+(\w+)""").find(uiState.practiceEditorBuffer)?.groupValues?.get(1) ?: "LabExercise"
                    OutlinedButton(
                        onClick = {
                            onOpenExerciseInEditor(
                                "$className.java",
                                uiState.practiceEditorBuffer,
                                activeExercise.targetJdkId
                            )
                        },
                        modifier = Modifier.testTag("practice_open_in_editor_button")
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open in Full Editor")
                    }
                }
            }
        }

        // Expected Output, Actual Lab Execution Output, and Explanation
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Expected Output:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = IdeAmberSecondary
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(10.dp)
                ) {
                    Text(
                        text = activeExercise.expectedOutput,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        color = IdeSuccessGreen
                    )
                }

                Text(
                    text = "Live Compiler & Execution Output:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = IdeCyanPrimary
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(10.dp)
                ) {
                    Text(
                        text = uiState.practiceLabOutput,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        modifier = Modifier.testTag("practice_lab_output_text")
                    )
                }

                Text(
                    text = "Explanation: ${activeExercise.explanation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
