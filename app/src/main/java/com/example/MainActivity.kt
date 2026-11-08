package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.jdk.JdkCatalogAndDetector
import com.example.ui.StudioModule
import com.example.ui.StudioViewModel
import com.example.ui.components.IdeBottomConsolePanel
import com.example.ui.components.IdeModuleNavigationStrip
import com.example.ui.components.IdeTopToolbar
import com.example.ui.components.moduleIcon
import com.example.ui.screens.ApiDocsScreen
import com.example.ui.screens.CodeEditorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JdkManagerScreen
import com.example.ui.screens.PracticeLabScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.ResourcesAndTestsScreen
import com.example.ui.screens.TutorialsScreen
import com.example.ui.screens.VersionExplorerScreen
import com.example.ui.theme.UniversalJavaStudioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val studioViewModel: StudioViewModel = viewModel()
            val uiState by studioViewModel.uiState.collectAsStateWithLifecycle()

            UniversalJavaStudioTheme(darkTheme = uiState.isDarkTheme) {
                UniversalJavaStudioApp(viewModel = studioViewModel)
            }
        }
    }
}

@Composable
fun UniversalJavaStudioApp(viewModel: StudioViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val jdks by viewModel.jdks.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val sourceFiles by viewModel.sourceFiles.collectAsStateWithLifecycle()
    val lessonProgress by viewModel.lessonProgress.collectAsStateWithLifecycle()
    val quizResults by viewModel.quizResults.collectAsStateWithLifecycle()
    val recentExecutions by viewModel.recentExecutions.collectAsStateWithLifecycle()
    val offlineResources by viewModel.offlineResources.collectAsStateWithLifecycle()

    val activeProject = projects.find { it.id == uiState.activeProjectId }
    val activeJdk = jdks.find { it.versionId == uiState.activeJdkVersionId }
        ?: JdkCatalogAndDetector.getDefaultJdkCatalog().first()

    // Handle system Back navigation across all secondary screens
    BackHandler(enabled = uiState.currentModule != StudioModule.DASHBOARD || uiState.navigationHistory.isNotEmpty()) {
        viewModel.navigateBack()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            IdeTopToolbar(
                uiState = uiState,
                activeProject = activeProject,
                jdks = jdks,
                onSelectJdk = viewModel::selectActiveJdk,
                onCompile = viewModel::compileCurrentTab,
                onRun = viewModel::runCurrentProgram,
                onStop = viewModel::stopRunningProgram,
                onToggleTheme = viewModel::toggleTheme,
                onNavigateTo = viewModel::navigateTo
            )
        },
        bottomBar = {
            IdeBottomConsolePanel(
                uiState = uiState,
                onSelectTab = viewModel::selectBottomTab,
                onToggleExpand = viewModel::toggleBottomPanelExpanded,
                onClearConsole = viewModel::cleanConsoleOutput,
                onUpdateStdinAndArgs = viewModel::updateStdinAndArgs
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 720.dp

            Row(modifier = Modifier.fillMaxSize()) {
                // Canonical NavigationRail on Wide/Tablet/Foldable screens
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        StudioModule.entries.forEach { module ->
                            NavigationRailItem(
                                selected = uiState.currentModule == module,
                                onClick = { viewModel.navigateTo(module) },
                                icon = {
                                    Icon(
                                        imageVector = moduleIcon(module),
                                        contentDescription = module.title
                                    )
                                },
                                label = { Text(module.title, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    // Horizontal Module Strip on Handheld/Compact screens
                    if (!isWideScreen) {
                        IdeModuleNavigationStrip(
                            currentModule = uiState.currentModule,
                            onSelectModule = viewModel::navigateTo
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (uiState.currentModule) {
                            StudioModule.DASHBOARD -> {
                                DashboardScreen(
                                    uiState = uiState,
                                    jdks = jdks,
                                    projects = projects,
                                    sourceFiles = sourceFiles,
                                    lessonProgress = lessonProgress,
                                    quizResults = quizResults,
                                    recentExecutions = recentExecutions,
                                    onNavigateTo = viewModel::navigateTo,
                                    onOpenProject = viewModel::openProject,
                                    onQuickNewJavaFile = {
                                        viewModel.createNewJavaFileInProject("QuickLab")
                                    }
                                )
                            }

                            StudioModule.EDITOR -> {
                                CodeEditorScreen(
                                    uiState = uiState,
                                    activeJdk = activeJdk,
                                    onSelectTab = viewModel::selectEditorTab,
                                    onCloseTab = viewModel::closeEditorTab,
                                    onUpdateContent = viewModel::updateActiveEditorContent,
                                    onSaveFile = viewModel::saveActiveFile,
                                    onUndo = viewModel::undoEditor,
                                    onRedo = viewModel::redoEditor,
                                    onToggleFindReplace = viewModel::toggleFindReplaceBar,
                                    onUpdateFindReplace = viewModel::updateFindReplaceQueries,
                                    onExecuteReplaceAll = viewModel::executeReplaceAllInActiveTab,
                                    onCreateNewFile = viewModel::createNewJavaFileInProject,
                                    onInsertSnippet = viewModel::insertCodeSnippet
                                )
                            }

                            StudioModule.JDK_MANAGER -> {
                                JdkManagerScreen(
                                    uiState = uiState,
                                    jdks = jdks,
                                    onSelectJdk = viewModel::selectActiveJdk,
                                    onSetDefaultJdk = viewModel::setDefaultJdk,
                                    onAddCustomJdk = viewModel::addCustomOrFutureJdk,
                                    onRemoveJdk = viewModel::removeJdkConfiguration
                                )
                            }

                            StudioModule.TUTORIALS -> {
                                TutorialsScreen(
                                    uiState = uiState,
                                    lessonProgress = lessonProgress,
                                    onSelectChapterAndLesson = viewModel::selectChapterAndLesson,
                                    onSetJava2Mode = viewModel::setTutorialSyntaxMode,
                                    onToggleComplete = viewModel::toggleLessonComplete,
                                    onToggleBookmark = viewModel::toggleLessonBookmark,
                                    onLoadCodeIntoEditor = viewModel::loadSnippetIntoEditor
                                )
                            }

                            StudioModule.API_DOCS -> {
                                ApiDocsScreen(
                                    uiState = uiState,
                                    onSelectMaxMajor = viewModel::setApiDocumentationJdkVersion,
                                    onUpdateSearchQuery = viewModel::updateApiSearchQuery,
                                    onSelectEntry = viewModel::selectApiDocEntry,
                                    onToggleShowFutureWithWarning = viewModel::toggleShowFutureApisWithWarning
                                )
                            }

                            StudioModule.VERSION_EXPLORER -> {
                                VersionExplorerScreen(
                                    uiState = uiState,
                                    onSelectComparisonPair = viewModel::setComparisonVersions
                                )
                            }

                            StudioModule.PRACTICE_LAB -> {
                                PracticeLabScreen(
                                    uiState = uiState,
                                    onSelectExercise = viewModel::selectPracticeExercise,
                                    onUpdatePracticeBuffer = viewModel::updatePracticeEditorBuffer,
                                    onCompileAndRunExercise = viewModel::compileAndRunPracticeExercise,
                                    onOpenExerciseInEditor = viewModel::loadSnippetIntoEditor
                                )
                            }

                            StudioModule.QUIZ -> {
                                QuizScreen(
                                    uiState = uiState,
                                    quizResults = quizResults,
                                    onSelectQuizSet = viewModel::selectQuizSet,
                                    onSelectAnswer = viewModel::selectQuizAnswer,
                                    onSubmitQuiz = viewModel::submitCurrentQuiz,
                                    onResetQuiz = viewModel::resetCurrentQuiz
                                )
                            }

                            StudioModule.PROJECTS -> {
                                ProjectsScreen(
                                    uiState = uiState,
                                    projects = projects,
                                    sourceFiles = sourceFiles,
                                    jdks = jdks,
                                    onOpenProject = viewModel::openProject,
                                    onCreateProject = viewModel::createNewProject,
                                    onSwitchProjectJdk = { projId, jdkId ->
                                        viewModel.selectActiveJdk(jdkId)
                                    },
                                    onDeleteProject = viewModel::deleteProject
                                )
                            }

                            StudioModule.RESOURCES_AND_TESTS -> {
                                ResourcesAndTestsScreen(
                                    uiState = uiState,
                                    resources = offlineResources,
                                    onImportResource = viewModel::importNewOfflineResource,
                                    onRemoveResource = viewModel::removeOfflineResource,
                                    onRerunTests = viewModel::rerunAll35VerificationTests
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
