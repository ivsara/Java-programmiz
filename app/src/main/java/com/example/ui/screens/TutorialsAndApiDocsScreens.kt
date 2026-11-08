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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.content.ApiDocsCatalog
import com.example.data.content.TutorialLesson
import com.example.data.content.TutorialsCatalog
import com.example.data.db.LessonProgressEntity
import com.example.ui.StudioUiState
import com.example.ui.theme.IdeAmberSecondary
import com.example.ui.theme.IdeCyanPrimary
import com.example.ui.theme.IdeErrorRed
import com.example.ui.theme.IdeSuccessGreen
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun TutorialsScreen(
    uiState: StudioUiState,
    lessonProgress: List<LessonProgressEntity>,
    onSelectChapterAndLesson: (String, String) -> Unit,
    onSetJava2Mode: (Boolean) -> Unit,
    onToggleComplete: (TutorialLesson) -> Unit,
    onToggleBookmark: (TutorialLesson) -> Unit,
    onLoadCodeIntoEditor: (String, String, String) -> Unit
) {
    val chapters = TutorialsCatalog.chapters
    val activeChapter = chapters.find { it.id == uiState.selectedChapterId } ?: chapters.first()
    val activeLesson = activeChapter.lessons.find { it.id == uiState.selectedLessonId } ?: activeChapter.lessons.first()
    val progress = lessonProgress.find { it.lessonId == activeLesson.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Offline Java Tutorial Platform (Chapters 1–11, Core APIs & Modern Java)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Structured on IIT Kharagpur Programming in Java & Oracle Java SE Tutorials. Every lesson preserves both Historical Java 2 (JDK 1.2) and Modern Java examples.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Horizontal Chapter Selector Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chapters.forEach { chapter ->
                val isSelected = chapter.id == activeChapter.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable {
                            onSelectChapterAndLesson(chapter.id, chapter.lessons.first().id)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("tutorial_chapter_${chapter.id.lowercase()}")
                ) {
                    Column {
                        Text(
                            text = "${chapter.numberLabel} • ${chapter.categoryTag}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Lesson Selector inside Chapter (if chapter has multiple lessons)
        if (activeChapter.lessons.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeChapter.lessons.forEach { lesson ->
                    val selected = lesson.id == activeLesson.id
                    OutlinedButton(
                        onClick = { onSelectChapterAndLesson(activeChapter.id, lesson.id) }
                    ) {
                        Text(
                            text = lesson.title,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Lesson Header Card with Progress & Bookmark Actions
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "12. Compatible JDKs: ${activeLesson.compatibleJdkRange} (Min: ${activeLesson.minimumJdkVersion})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Text(
                    text = activeLesson.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Status / Deprecation Note: ${activeLesson.deprecationOrStatusNote} • Ref: ${activeChapter.referenceSource}",
                    style = MaterialTheme.typography.labelMedium,
                    color = IdeCyanPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onToggleComplete(activeLesson) },
                        modifier = Modifier.testTag("tutorial_mark_complete_button")
                    ) {
                        Icon(
                            imageVector = if (progress?.isCompleted == true) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (progress?.isCompleted == true) "Completed" else "Mark Lesson Completed")
                    }

                    OutlinedButton(
                        onClick = { onToggleBookmark(activeLesson) },
                        modifier = Modifier.testTag("tutorial_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (progress?.isBookmarked == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (progress?.isBookmarked == true) "Bookmarked" else "Bookmark")
                    }
                }
            }
        }

        // All 12 Required Tutorial Sections
        TutorialSectionCard("1. Topic Introduction", activeLesson.topicIntroduction)
        TutorialSectionCard("2. Definition", activeLesson.definition)
        TutorialSectionCard("3. Detailed Explanation", activeLesson.detailedExplanation)

        // 4. Syntax Block
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("4. Syntax", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = IdeCyanPrimary)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(10.dp)
                ) {
                    Text(
                        text = highlightJavaSource(activeLesson.syntaxBlock, uiState.isDarkTheme),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 5. Important Concepts
        TutorialBulletListCard("5. Important Concepts", activeLesson.importantConcepts)

        // 6. Side-by-Side / Toggleable Java 2 (JDK 1.2) vs Modern Java Example Program
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "6. Java Example Program (Historical Java 2 vs Modern Java)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = IdeAmberSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onSetJava2Mode(true) },
                        modifier = Modifier.testTag("tutorial_java2_example_tab")
                    ) {
                        Text("Java 2 (JDK 1.2) Syntax")
                    }
                    OutlinedButton(
                        onClick = { onSetJava2Mode(false) },
                        modifier = Modifier.testTag("tutorial_modern_example_tab")
                    ) {
                        Text("Modern Java (JDK 8–25) Syntax")
                    }
                }

                val activeCode = if (uiState.preferJava2InTutorial) activeLesson.java2ExampleCode else activeLesson.modernJavaExampleCode
                val targetJdkForCode = if (uiState.preferJava2InTutorial) "JDK_1_2" else "JDK_21"
                val className = Regex("""public\s+class\s+(\w+)""").find(activeCode)?.groupValues?.get(1) ?: "TutorialExample"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(12.dp)
                ) {
                    Text(
                        text = highlightJavaSource(activeCode, uiState.isDarkTheme),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }

                Button(
                    onClick = { onLoadCodeIntoEditor("$className.java", activeCode, targetJdkForCode) },
                    modifier = Modifier.testTag("tutorial_load_into_editor_button")
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Load $className.java into Editor ($targetJdkForCode)")
                }
            }
        }

        TutorialSectionCard("7. Program Explanation", activeLesson.programExplanation)
        TutorialSectionCard("8. Sample Output", activeLesson.sampleOutput, isMonospace = true)
        TutorialBulletListCard("9. Common Errors & Version Pitfalls", activeLesson.commonErrors)
        TutorialBulletListCard("10. Practice Questions", activeLesson.practiceQuestions)
        TutorialBulletListCard("11. Related Offline API Documentation", activeLesson.relatedApiDocs)
    }
}

@Composable
private fun TutorialSectionCard(title: String, body: String, isMonospace: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = IdeCyanPrimary)
            Text(
                text = body,
                style = if (isMonospace) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
                color = if (isMonospace) IdeSuccessGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TutorialBulletListCard(title: String, items: List<String>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = IdeCyanPrimary)
            items.forEach { item ->
                Text("• $item", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// =========================================================================
// MODULE 7 – OFFLINE JAVA API DOCUMENTATION BROWSER
// =========================================================================

@Composable
fun ApiDocsScreen(
    uiState: StudioUiState,
    onSelectMaxMajor: (Int) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onSelectEntry: (String) -> Unit,
    onToggleShowFutureWithWarning: () -> Unit
) {
    var showVersionMenu by remember { mutableStateOf(false) }
    val selectedVersionLabel = ApiDocsCatalog.versionFilterOptions
        .find { it.second == uiState.selectedApiMaxMajor }?.first ?: "JDK 1.2 – Java 2 (1998)"

    val query = uiState.apiSearchQuery.trim()
    val matchingEntries = remember(uiState.selectedApiMaxMajor, query, uiState.showUnavailableModernApisWithWarning) {
        ApiDocsCatalog.entries.filter { entry ->
            val versionAllowed = uiState.showUnavailableModernApisWithWarning || entry.introducedMajor <= uiState.selectedApiMaxMajor
            val matchesQuery = query.isEmpty() ||
                entry.typeName.contains(query, ignoreCase = true) ||
                entry.packageName.contains(query, ignoreCase = true) ||
                entry.summary.contains(query, ignoreCase = true) ||
                entry.methods.any { it.first.contains(query, ignoreCase = true) }
            versionAllowed && matchesQuery
        }
    }

    val activeEntry = matchingEntries.find { it.id == uiState.selectedApiEntryId }
        ?: matchingEntries.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Offline Java API Documentation Browser",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Search packages, classes, interfaces, methods, constructors, and fields. Strict version filtering guarantees Java 7 or modern APIs are NEVER shown as available in Java 2.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // API Documentation Version Selector & Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Button(
                    onClick = { showVersionMenu = true },
                    modifier = Modifier.testTag("api_version_selector_button")
                ) {
                    Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("API Target: $selectedVersionLabel")
                }

                DropdownMenu(
                    expanded = showVersionMenu,
                    onDismissRequest = { showVersionMenu = false },
                    modifier = Modifier.heightIn(max = 340.dp)
                ) {
                    ApiDocsCatalog.versionFilterOptions.forEach { (label, major) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onSelectMaxMajor(major)
                                showVersionMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onToggleShowFutureWithWarning,
                modifier = Modifier.testTag("api_compare_future_toggle")
            ) {
                Text(
                    if (uiState.showUnavailableModernApisWithWarning) "Strict Filter: OFF (Compare Mode)"
                    else "Strict Filter: ON (Hide Post-${uiState.selectedApiMaxMajor} APIs)"
                )
            }
        }

        OutlinedTextField(
            value = uiState.apiSearchQuery,
            onValueChange = onUpdateSearchQuery,
            label = { Text("Search packages, classes, methods (e.g. ArrayList, Vector, String, Applet, Stream)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("api_search_input")
        )

        // Class / Interface Pills Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            matchingEntries.forEach { entry ->
                val isSelected = entry.id == activeEntry?.id
                val isNewerThanSelected = entry.introducedMajor > uiState.selectedApiMaxMajor
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isNewerThanSelected -> IdeErrorRed.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .clickable { onSelectEntry(entry.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("api_entry_chip_${entry.typeName.lowercase()}")
                ) {
                    Column {
                        Text(
                            text = entry.packageName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${entry.typeName} (${entry.introducedInLabel})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Selected Class / Interface Javadoc View
        if (activeEntry != null) {
            val isUnavailableInTarget = activeEntry.introducedMajor > uiState.selectedApiMaxMajor
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isUnavailableInTarget) IdeErrorRed else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isUnavailableInTarget) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(IdeErrorRed.copy(alpha = 0.2f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "UNAVAILABLE IN $selectedVersionLabel: ${activeEntry.packageName}.${activeEntry.typeName} was introduced in ${activeEntry.introducedInLabel}.",
                                style = MaterialTheme.typography.labelLarge,
                                color = IdeErrorRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "${activeEntry.kind} ${activeEntry.packageName}.${activeEntry.typeName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = IdeCyanPrimary
                    )
                    Text(
                        text = "Since: ${activeEntry.introducedInLabel} • Hierarchy: ${activeEntry.inheritanceHierarchy}",
                        style = MaterialTheme.typography.labelMedium
                    )

                    if (activeEntry.deprecatedInLabel != null || activeEntry.removedInLabel != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(IdeAmberSecondary.copy(alpha = 0.2f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Lifecycle Notice: ${listOfNotNull(activeEntry.deprecatedInLabel, activeEntry.removedInLabel).joinToString(" | ")}",
                                style = MaterialTheme.typography.labelMedium,
                                color = IdeAmberSecondary
                            )
                        }
                    }

                    Text(
                        text = activeEntry.summary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    if (activeEntry.constructors.isNotEmpty()) {
                        Text("Constructors", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        activeEntry.constructors.forEach { ctor ->
                            Text("• $ctor", fontFamily = JetBrainsMonoFamily, fontSize = 12.sp)
                        }
                    }

                    if (activeEntry.fields.isNotEmpty()) {
                        Text("Fields", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        activeEntry.fields.forEach { f ->
                            Text("• $f", fontFamily = JetBrainsMonoFamily, fontSize = 12.sp)
                        }
                    }

                    // Filter methods by selected API documentation version!
                    val availableMethods = activeEntry.methods.filter { it.second <= uiState.selectedApiMaxMajor }
                    val futureMethods = activeEntry.methods.filter { it.second > uiState.selectedApiMaxMajor }

                    Text(
                        text = "Methods Available in $selectedVersionLabel (${availableMethods.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = IdeSuccessGreen
                    )
                    availableMethods.forEach { (sig, minVer) ->
                        val verTag = if (minVer <= 4) "1.$minVer" else "$minVer"
                        Text(
                            text = "• $sig   [Since JDK $verTag]",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp
                        )
                    }

                    if (futureMethods.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Methods Added in Newer JDK Releases (Not in $selectedVersionLabel — ${futureMethods.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = IdeAmberSecondary
                        )
                        futureMethods.forEach { (sig, minVer) ->
                            val verTag = if (minVer <= 4) "1.$minVer" else "$minVer"
                            Text(
                                text = "✕ $sig   [Requires JDK $verTag+]",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Related Classes: ${activeEntry.relatedClasses.joinToString(", ")}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
