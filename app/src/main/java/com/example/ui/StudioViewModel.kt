package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.compiler.CompilationResult
import com.example.core.compiler.JavaCompilerEngine
import com.example.core.execution.ExecutionResult
import com.example.core.execution.JavaExecutionEngine
import com.example.core.jdk.HostEnvironmentReport
import com.example.core.jdk.JdkCatalogAndDetector
import com.example.core.testing.StudioVerificationSuite
import com.example.core.testing.VerificationTestCaseResult
import com.example.data.content.ApiDocEntry
import com.example.data.content.ApiDocsCatalog
import com.example.data.content.PracticeExercise
import com.example.data.content.PracticeLabCatalog
import com.example.data.content.QuizCatalog
import com.example.data.content.QuizSet
import com.example.data.content.TutorialChapter
import com.example.data.content.TutorialLesson
import com.example.data.content.TutorialsCatalog
import com.example.data.content.VersionExplorerCatalog
import com.example.data.db.ExecutionHistoryEntity
import com.example.data.db.JdkInstallationEntity
import com.example.data.db.LessonProgressEntity
import com.example.data.db.OfflineResourceEntity
import com.example.data.db.ProjectEntity
import com.example.data.db.QuizResultEntity
import com.example.data.db.SourceFileEntity
import com.example.data.db.StudioDatabase
import com.example.data.repository.StudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class StudioModule(val title: String) {
    DASHBOARD("Dashboard"),
    EDITOR("Code Editor"),
    JDK_MANAGER("JDK Manager"),
    TUTORIALS("Tutorials"),
    API_DOCS("API Docs"),
    VERSION_EXPLORER("Versions"),
    PRACTICE_LAB("Practice Lab"),
    QUIZ("Quiz & Assess"),
    PROJECTS("Projects"),
    RESOURCES_AND_TESTS("Offline & Tests")
}

enum class BottomConsoleTab(val label: String) {
    COMPILER_OUTPUT("Compiler Output"),
    PROGRAM_OUTPUT("Program Output"),
    ERROR_CONSOLE("Diagnostics & Errors"),
    BYTECODE_INSPECTOR("Bytecode (javap)"),
    STDIN_AND_ARGS("Stdin & JVM Args")
}

data class EditorTabState(
    val fileId: Long,
    val projectId: Long,
    val fileName: String,
    val packageName: String,
    val content: String,
    val isDirty: Boolean = false,
    val undoStack: List<String> = emptyList(),
    val redoStack: List<String> = emptyList()
)

data class StudioUiState(
    val currentModule: StudioModule = StudioModule.DASHBOARD,
    val navigationHistory: List<StudioModule> = emptyList(),
    val isDarkTheme: Boolean = true,
    val activeProjectId: Long = 1L,
    val activeJdkVersionId: String = "JDK_1_2",
    val openTabs: List<EditorTabState> = emptyList(),
    val activeTabIndex: Int = 0,
    val findQuery: String = "",
    val replaceQuery: String = "",
    val showFindReplaceBar: Boolean = false,
    val stdinInput: String = "42\nUniversal Java Studio",
    val programArgsInput: String = "",
    val activeBottomTab: BottomConsoleTab = BottomConsoleTab.PROGRAM_OUTPUT,
    val isBottomPanelExpanded: Boolean = true,
    val isCompilingOrRunning: Boolean = false,
    val lastCompilationResult: CompilationResult? = null,
    val lastExecutionResult: ExecutionResult? = null,
    val statusBannerMessage: String = "Ready — Offline Java IDE & Compiler Initialized",
    // Tutorials State
    val selectedChapterId: String = "CH_1",
    val selectedLessonId: String = "L_1_1",
    val preferJava2InTutorial: Boolean = true,
    // API Docs State
    val selectedApiMaxMajor: Int = 2, // Defaults to JDK 1.2 (Java 2) to demonstrate strict historical API boundary!
    val apiSearchQuery: String = "",
    val selectedApiEntryId: String = "API_ARRAYLIST",
    val showUnavailableModernApisWithWarning: Boolean = false,
    // Version Explorer State
    val compareVersionLeftId: String = "JDK_8",
    val compareVersionRightId: String = "JDK_17",
    // Practice Lab State
    val selectedExerciseId: String = "EX_01",
    val practiceEditorBuffer: String = PracticeLabCatalog.exercises.first().sourceCode,
    val practiceLabOutput: String = "Click 'Compile & Run Exercise' to execute directly in the Practice Lab.",
    // Quiz State
    val selectedQuizSetId: String = "QUIZ_CH1_3",
    val currentQuizAnswers: Map<String, Int> = emptyMap(),
    val quizSubmitted: Boolean = false,
    val lastQuizScore: Int? = null,
    // Host & Verification Report
    val hostReport: HostEnvironmentReport = JdkCatalogAndDetector.probeHostEnvironment(),
    val verificationResults: List<VerificationTestCaseResult> = emptyList()
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudioRepository
    private var executionJob: Job? = null

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val jdks: StateFlow<List<JdkInstallationEntity>>
    val projects: StateFlow<List<ProjectEntity>>
    val sourceFiles: StateFlow<List<SourceFileEntity>>
    val lessonProgress: StateFlow<List<LessonProgressEntity>>
    val quizResults: StateFlow<List<QuizResultEntity>>
    val recentExecutions: StateFlow<List<ExecutionHistoryEntity>>
    val offlineResources: StateFlow<List<OfflineResourceEntity>>

    init {
        val db = StudioDatabase.getInstance(application)
        repository = StudioRepository(db.studioDao())

        jdks = repository.allJdks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            JdkCatalogAndDetector.getDefaultJdkCatalog()
        )
        projects = repository.allProjects.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        sourceFiles = repository.allSourceFiles.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        lessonProgress = repository.allLessonProgress.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        quizResults = repository.allQuizResults.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        recentExecutions = repository.recentExecutions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        offlineResources = repository.allResources.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.ensureSeeded()
            // Run verification suite once on startup so the 35-test report is immediately populated
            val testResults = withContext(Dispatchers.Default) {
                StudioVerificationSuite.runAll35VerificationTests()
            }
            _uiState.update { it.copy(verificationResults = testResults) }
        }

        // Automatically open first project's files into editor tabs when seeded
        viewModelScope.launch {
            combine(projects, sourceFiles) { projList, fileList ->
                projList to fileList
            }.collect { (projList, fileList) ->
                val state = _uiState.value
                if (state.openTabs.isEmpty() && projList.isNotEmpty() && fileList.isNotEmpty()) {
                    val initialProject = projList.find { it.name == "Java Basics" } ?: projList.first()
                    val projFiles = fileList.filter { it.projectId == initialProject.id }
                    if (projFiles.isNotEmpty()) {
                        val tabs = projFiles.map { f ->
                            EditorTabState(
                                fileId = f.id,
                                projectId = f.projectId,
                                fileName = f.fileName,
                                packageName = f.packageName,
                                content = f.content
                            )
                        }
                        _uiState.update {
                            it.copy(
                                activeProjectId = initialProject.id,
                                activeJdkVersionId = initialProject.jdkVersionId,
                                openTabs = tabs,
                                activeTabIndex = 0,
                                statusBannerMessage = "Loaded Project '${initialProject.name}' [${initialProject.jdkVersionId}]"
                            )
                        }
                    }
                }
            }
        }
    }

    // Navigation with Backstack
    fun navigateTo(module: StudioModule) {
        _uiState.update { current ->
            if (current.currentModule == module) current
            else current.copy(
                currentModule = module,
                navigationHistory = current.navigationHistory + current.currentModule
            )
        }
    }

    fun navigateBack(): Boolean {
        val current = _uiState.value
        if (current.navigationHistory.isNotEmpty()) {
            val previous = current.navigationHistory.last()
            _uiState.update {
                it.copy(
                    currentModule = previous,
                    navigationHistory = current.navigationHistory.dropLast(1)
                )
            }
            return true
        }
        if (current.currentModule != StudioModule.DASHBOARD) {
            _uiState.update { it.copy(currentModule = StudioModule.DASHBOARD) }
            return true
        }
        return false
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun selectBottomTab(tab: BottomConsoleTab) {
        _uiState.update { it.copy(activeBottomTab = tab, isBottomPanelExpanded = true) }
    }

    fun toggleBottomPanelExpanded() {
        _uiState.update { it.copy(isBottomPanelExpanded = !it.isBottomPanelExpanded) }
    }

    // JDK Selection & Project-Specific JDK Synchronization
    fun selectActiveJdk(versionId: String) {
        val activeProjId = _uiState.value.activeProjectId
        _uiState.update {
            it.copy(
                activeJdkVersionId = versionId,
                statusBannerMessage = "Switched Active & Project JDK to $versionId"
            )
        }
        viewModelScope.launch {
            if (activeProjId > 0) {
                repository.switchProjectJdk(activeProjId, versionId)
            }
        }
    }

    fun setDefaultJdk(versionId: String) {
        viewModelScope.launch {
            repository.setDefaultJdk(versionId)
            _uiState.update { it.copy(statusBannerMessage = "Set $versionId as Default Studio JDK") }
        }
    }

    fun addCustomOrFutureJdk(majorNumber: Int, versionString: String, vendor: String, javaHome: String) {
        viewModelScope.launch {
            val customJdk = JdkCatalogAndDetector.createCustomJdkInstallation(
                majorNumber = majorNumber,
                versionString = versionString,
                vendor = vendor,
                javaHome = javaHome
            )
            repository.addCustomJdk(customJdk)
            _uiState.update {
                it.copy(
                    activeJdkVersionId = customJdk.versionId,
                    statusBannerMessage = "Registered ${customJdk.displayName} [Bytecode ${customJdk.bytecodeVersion}]"
                )
            }
        }
    }

    fun removeJdkConfiguration(versionId: String) {
        viewModelScope.launch {
            repository.removeJdk(versionId)
            _uiState.update { it.copy(statusBannerMessage = "Removed JDK configuration $versionId") }
        }
    }

    // Project Operations (Module 12)
    fun openProject(project: ProjectEntity) {
        val projFiles = sourceFiles.value.filter { it.projectId == project.id }
        val tabs = projFiles.map { f ->
            EditorTabState(
                fileId = f.id,
                projectId = f.projectId,
                fileName = f.fileName,
                packageName = f.packageName,
                content = f.content
            )
        }
        _uiState.update {
            it.copy(
                activeProjectId = project.id,
                activeJdkVersionId = project.jdkVersionId, // Restores Project-Specific JDK automatically!
                openTabs = tabs,
                activeTabIndex = 0,
                currentModule = StudioModule.EDITOR,
                navigationHistory = it.navigationHistory + it.currentModule,
                statusBannerMessage = "Opened Project '${project.name}' with dedicated JDK ${project.jdkVersionId}"
            )
        }
    }

    fun createNewProject(
        name: String,
        description: String,
        jdkVersionId: String,
        packageName: String,
        mainClass: String
    ) {
        val cleanClass = mainClass.removeSuffix(".java").ifBlank { "Main" }
        val pkgHeader = if (packageName.isNotBlank() && packageName != "default") "package $packageName;\n\n" else ""
        val isLegacy = jdkVersionId in listOf("JDK_1_0", "JDK_1_1", "JDK_1_2", "JDK_1_3", "JDK_1_4")
        val mainSig = if (isLegacy) "public static void main(String args[])" else "public static void main(String[] args)"
        val starterCode = """${pkgHeader}public class $cleanClass {
    $mainSig {
        System.out.println("Project: $name running on $jdkVersionId");
    }
}"""
        viewModelScope.launch {
            val newProjId = repository.createNewProject(
                name = name.ifBlank { "UntitledJavaProject" },
                description = description.ifBlank { "Java project targeting $jdkVersionId" },
                jdkVersionId = jdkVersionId,
                packageName = packageName.ifBlank { "default" },
                mainClass = cleanClass,
                initialCode = starterCode
            )
            val newTab = EditorTabState(
                fileId = 0L,
                projectId = newProjId,
                fileName = "$cleanClass.java",
                packageName = packageName.ifBlank { "default" },
                content = starterCode
            )
            _uiState.update {
                it.copy(
                    activeProjectId = newProjId,
                    activeJdkVersionId = jdkVersionId,
                    openTabs = listOf(newTab),
                    activeTabIndex = 0,
                    currentModule = StudioModule.EDITOR,
                    statusBannerMessage = "Created Project '$name' bound to $jdkVersionId"
                )
            }
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            _uiState.update { it.copy(statusBannerMessage = "Deleted project #$projectId") }
        }
    }

    // Editor Operations (Module 3)
    fun selectEditorTab(index: Int) {
        _uiState.update { state ->
            if (index in state.openTabs.indices) state.copy(activeTabIndex = index) else state
        }
    }

    fun closeEditorTab(index: Int) {
        _uiState.update { state ->
            if (state.openTabs.size <= 1 || index !in state.openTabs.indices) return@update state
            val updated = state.openTabs.toMutableList().apply { removeAt(index) }
            val newActive = state.activeTabIndex.coerceAtMost(updated.lastIndex)
            state.copy(openTabs = updated, activeTabIndex = newActive)
        }
    }

    fun updateActiveEditorContent(newContent: String) {
        _uiState.update { state ->
            val idx = state.activeTabIndex
            if (idx !in state.openTabs.indices) return@update state
            val curTab = state.openTabs[idx]
            if (curTab.content == newContent) return@update state
            val newUndo = (curTab.undoStack + curTab.content).takeLast(30)
            val updatedTab = curTab.copy(
                content = newContent,
                isDirty = true,
                undoStack = newUndo,
                redoStack = emptyList()
            )
            val newTabs = state.openTabs.toMutableList().apply { set(idx, updatedTab) }
            state.copy(openTabs = newTabs)
        }
    }

    fun undoEditor() {
        _uiState.update { state ->
            val idx = state.activeTabIndex
            val curTab = state.openTabs.getOrNull(idx) ?: return@update state
            if (curTab.undoStack.isEmpty()) return@update state
            val prevText = curTab.undoStack.last()
            val updatedTab = curTab.copy(
                content = prevText,
                undoStack = curTab.undoStack.dropLast(1),
                redoStack = curTab.redoStack + curTab.content,
                isDirty = true
            )
            val newTabs = state.openTabs.toMutableList().apply { set(idx, updatedTab) }
            state.copy(openTabs = newTabs, statusBannerMessage = "Undo applied")
        }
    }

    fun redoEditor() {
        _uiState.update { state ->
            val idx = state.activeTabIndex
            val curTab = state.openTabs.getOrNull(idx) ?: return@update state
            if (curTab.redoStack.isEmpty()) return@update state
            val nextText = curTab.redoStack.last()
            val updatedTab = curTab.copy(
                content = nextText,
                undoStack = curTab.undoStack + curTab.content,
                redoStack = curTab.redoStack.dropLast(1),
                isDirty = true
            )
            val newTabs = state.openTabs.toMutableList().apply { set(idx, updatedTab) }
            state.copy(openTabs = newTabs, statusBannerMessage = "Redo applied")
        }
    }

    fun insertCodeSnippet(snippet: String) {
        val state = _uiState.value
        val curTab = state.openTabs.getOrNull(state.activeTabIndex) ?: return
        updateActiveEditorContent(curTab.content + "\n" + snippet)
    }

    fun toggleFindReplaceBar() {
        _uiState.update { it.copy(showFindReplaceBar = !it.showFindReplaceBar) }
    }

    fun updateFindReplaceQueries(find: String, replace: String) {
        _uiState.update { it.copy(findQuery = find, replaceQuery = replace) }
    }

    fun executeReplaceAllInActiveTab() {
        val state = _uiState.value
        val find = state.findQuery
        if (find.isEmpty()) return
        val curTab = state.openTabs.getOrNull(state.activeTabIndex) ?: return
        val replaced = curTab.content.replace(find, state.replaceQuery)
        updateActiveEditorContent(replaced)
        _uiState.update { it.copy(statusBannerMessage = "Replaced occurrences of '$find' with '${state.replaceQuery}'") }
    }

    fun saveActiveFile() {
        val state = _uiState.value
        val idx = state.activeTabIndex
        val curTab = state.openTabs.getOrNull(idx) ?: return
        viewModelScope.launch {
            val savedId = repository.saveSourceFile(
                SourceFileEntity(
                    id = curTab.fileId,
                    projectId = curTab.projectId,
                    fileName = curTab.fileName,
                    packageName = curTab.packageName,
                    content = curTab.content
                )
            )
            _uiState.update { s ->
                val updatedTab = curTab.copy(fileId = savedId, isDirty = false)
                val newTabs = s.openTabs.toMutableList().apply { set(idx, updatedTab) }
                s.copy(
                    openTabs = newTabs,
                    statusBannerMessage = "Saved ${curTab.fileName} to SQLite Project Storage"
                )
            }
        }
    }

    fun createNewJavaFileInProject(rawFileName: String) {
        val cleanName = rawFileName.trim().removeSuffix(".java").ifBlank { "NewClass" }
        val fileName = "$cleanName.java"
        val state = _uiState.value
        val isLegacy = state.activeJdkVersionId in listOf("JDK_1_0", "JDK_1_1", "JDK_1_2", "JDK_1_3", "JDK_1_4")
        val mainSig = if (isLegacy) "public static void main(String args[])" else "public static void main(String[] args)"
        val content = """public class $cleanName {
    $mainSig {
        System.out.println("Executing $cleanName on ${state.activeJdkVersionId}");
    }
}"""
        viewModelScope.launch {
            val id = repository.saveSourceFile(
                SourceFileEntity(
                    projectId = state.activeProjectId,
                    fileName = fileName,
                    packageName = "default",
                    content = content
                )
            )
            val newTab = EditorTabState(
                fileId = id,
                projectId = state.activeProjectId,
                fileName = fileName,
                packageName = "default",
                content = content
            )
            _uiState.update { s ->
                val updatedTabs = s.openTabs + newTab
                s.copy(
                    openTabs = updatedTabs,
                    activeTabIndex = updatedTabs.lastIndex,
                    currentModule = StudioModule.EDITOR,
                    statusBannerMessage = "Created and opened $fileName"
                )
            }
        }
    }

    fun loadSnippetIntoEditor(fileName: String, code: String, targetJdkId: String) {
        val state = _uiState.value
        val newTab = EditorTabState(
            fileId = 0L,
            projectId = state.activeProjectId,
            fileName = fileName,
            packageName = "default",
            content = code,
            isDirty = true
        )
        _uiState.update { s ->
            val existingIdx = s.openTabs.indexOfFirst { it.fileName == fileName }
            val updatedTabs = if (existingIdx >= 0) {
                s.openTabs.toMutableList().apply { set(existingIdx, newTab) }
            } else {
                s.openTabs + newTab
            }
            val newActiveIdx = if (existingIdx >= 0) existingIdx else updatedTabs.lastIndex
            s.copy(
                activeJdkVersionId = targetJdkId,
                openTabs = updatedTabs,
                activeTabIndex = newActiveIdx,
                currentModule = StudioModule.EDITOR,
                navigationHistory = s.navigationHistory + s.currentModule,
                statusBannerMessage = "Loaded $fileName into Editor with target $targetJdkId"
            )
        }
    }

    // Compiler & Execution Engine Actions (Modules 4 & 5)
    fun updateStdinAndArgs(stdin: String, args: String) {
        _uiState.update { it.copy(stdinInput = stdin, programArgsInput = args) }
    }

    private fun resolveActiveJdk(): JdkInstallationEntity {
        val id = _uiState.value.activeJdkVersionId
        return jdks.value.find { it.versionId == id }
            ?: JdkCatalogAndDetector.getDefaultJdkCatalog().find { it.versionId == id }
            ?: JdkCatalogAndDetector.getDefaultJdkCatalog().first()
    }

    fun compileCurrentTab() {
        val state = _uiState.value
        val curTab = state.openTabs.getOrNull(state.activeTabIndex) ?: return
        val jdk = resolveActiveJdk()
        val companionFiles = state.openTabs
            .filterIndexed { i, _ -> i != state.activeTabIndex }
            .associate { it.fileName to it.content }

        executionJob?.cancel()
        executionJob = viewModelScope.launch {
            _uiState.update { it.copy(isCompilingOrRunning = true, statusBannerMessage = "Compiling ${curTab.fileName} with ${jdk.versionId}...") }
            val result = withContext(Dispatchers.Default) {
                JavaCompilerEngine.compileSource(
                    fileName = curTab.fileName,
                    sourceCode = curTab.content,
                    jdk = jdk,
                    additionalProjectSources = companionFiles
                )
            }
            val projName = projects.value.find { it.id == state.activeProjectId }?.name ?: "Workspace"
            repository.recordExecution(
                ExecutionHistoryEntity(
                    projectName = projName,
                    fileName = curTab.fileName,
                    jdkVersionId = jdk.versionId,
                    status = if (result.success) "COMPILED" else "COMPILATION FAILED",
                    compilerCommand = result.compilerCommand,
                    compilerOutput = result.formattedConsoleOutput,
                    runtimeOutput = "",
                    bytecodeVersion = jdk.bytecodeVersion,
                    executionTimeMs = result.compilationTimeMs
                )
            )
            _uiState.update {
                it.copy(
                    isCompilingOrRunning = false,
                    lastCompilationResult = result,
                    activeBottomTab = if (result.success) BottomConsoleTab.COMPILER_OUTPUT else BottomConsoleTab.ERROR_CONSOLE,
                    isBottomPanelExpanded = true,
                    statusBannerMessage = if (result.success)
                        "Compilation Successful (${jdk.versionId} -> Bytecode ${jdk.bytecodeVersion})"
                    else
                        "Compilation Failed (${result.diagnostics.size} diagnostic(s))"
                )
            }
        }
    }

    fun runCurrentProgram() {
        val state = _uiState.value
        val curTab = state.openTabs.getOrNull(state.activeTabIndex) ?: return
        val jdk = resolveActiveJdk()
        val companionFiles = state.openTabs
            .filterIndexed { i, _ -> i != state.activeTabIndex }
            .associate { it.fileName to it.content }
        val argsList = state.programArgsInput.split(Regex("""\s+""")).filter { it.isNotBlank() }

        executionJob?.cancel()
        executionJob = viewModelScope.launch {
            _uiState.update { it.copy(isCompilingOrRunning = true, statusBannerMessage = "Building & Executing ${curTab.fileName} on ${jdk.versionId}...") }
            val (compRes, execRes) = withContext(Dispatchers.Default) {
                JavaExecutionEngine.compileAndExecute(
                    fileName = curTab.fileName,
                    sourceCode = curTab.content,
                    jdk = jdk,
                    stdinInput = state.stdinInput,
                    programArgs = argsList,
                    additionalProjectSources = companionFiles
                )
            }

            val projName = projects.value.find { it.id == state.activeProjectId }?.name ?: "Workspace"
            val statusStr = when {
                !compRes.success -> "COMPILATION FAILED"
                execRes?.exitCode == 0 -> "COMPILED & EXECUTED"
                else -> "RUNTIME EXCEPTION"
            }
            repository.recordExecution(
                ExecutionHistoryEntity(
                    projectName = projName,
                    fileName = curTab.fileName,
                    jdkVersionId = jdk.versionId,
                    status = statusStr,
                    compilerCommand = compRes.compilerCommand,
                    compilerOutput = compRes.formattedConsoleOutput,
                    runtimeOutput = execRes?.stdout?.ifBlank { execRes.stderr } ?: "",
                    bytecodeVersion = jdk.bytecodeVersion,
                    executionTimeMs = (compRes.compilationTimeMs + (execRes?.executionTimeMs ?: 0L))
                )
            )

            _uiState.update {
                it.copy(
                    isCompilingOrRunning = false,
                    lastCompilationResult = compRes,
                    lastExecutionResult = execRes,
                    activeBottomTab = when {
                        !compRes.success -> BottomConsoleTab.ERROR_CONSOLE
                        execRes != null && execRes.exitCode != 0 -> BottomConsoleTab.ERROR_CONSOLE
                        else -> BottomConsoleTab.PROGRAM_OUTPUT
                    },
                    isBottomPanelExpanded = true,
                    statusBannerMessage = when {
                        !compRes.success -> "Build Failed — Check Compiler Diagnostics"
                        execRes?.exitCode == 0 -> "Process finished with exit code 0 in ${execRes.executionTimeMs}ms [${jdk.versionId}]"
                        else -> "Process terminated with Runtime Exception (Exit code 1)"
                    }
                )
            }
        }
    }

    fun stopRunningProgram() {
        executionJob?.cancel()
        _uiState.update {
            it.copy(
                isCompilingOrRunning = false,
                statusBannerMessage = "Process terminated by user."
            )
        }
    }

    fun cleanConsoleOutput() {
        _uiState.update {
            it.copy(
                lastCompilationResult = null,
                lastExecutionResult = null,
                statusBannerMessage = "Cleaned build/classes artifacts and console buffers."
            )
        }
    }

    // Tutorials Module Actions (Module 6)
    fun selectChapterAndLesson(chapterId: String, lessonId: String) {
        _uiState.update {
            it.copy(selectedChapterId = chapterId, selectedLessonId = lessonId)
        }
    }

    fun setTutorialSyntaxMode(preferJava2: Boolean) {
        _uiState.update { it.copy(preferJava2InTutorial = preferJava2) }
    }

    fun toggleLessonComplete(lesson: TutorialLesson) {
        val current = lessonProgress.value.find { it.lessonId == lesson.id }
        viewModelScope.launch {
            repository.toggleLessonCompleted(
                lessonId = lesson.id,
                chapterId = lesson.chapterId,
                currentCompleted = current?.isCompleted ?: false,
                currentBookmarked = current?.isBookmarked ?: false
            )
        }
    }

    fun toggleLessonBookmark(lesson: TutorialLesson) {
        val current = lessonProgress.value.find { it.lessonId == lesson.id }
        viewModelScope.launch {
            repository.toggleLessonBookmark(
                lessonId = lesson.id,
                chapterId = lesson.chapterId,
                currentCompleted = current?.isCompleted ?: false,
                currentBookmarked = current?.isBookmarked ?: false
            )
        }
    }

    // API Documentation Browser Actions (Module 7)
    fun setApiDocumentationJdkVersion(maxMajor: Int) {
        _uiState.update { it.copy(selectedApiMaxMajor = maxMajor) }
    }

    fun updateApiSearchQuery(query: String) {
        _uiState.update { it.copy(apiSearchQuery = query) }
    }

    fun selectApiDocEntry(entryId: String) {
        _uiState.update { it.copy(selectedApiEntryId = entryId) }
    }

    fun toggleShowFutureApisWithWarning() {
        _uiState.update { it.copy(showUnavailableModernApisWithWarning = !it.showUnavailableModernApisWithWarning) }
    }

    // Version Explorer Actions (Module 8)
    fun setComparisonVersions(leftId: String, rightId: String) {
        _uiState.update {
            it.copy(compareVersionLeftId = leftId, compareVersionRightId = rightId)
        }
    }

    // Practice Laboratory Actions (Module 9)
    fun selectPracticeExercise(exercise: PracticeExercise) {
        _uiState.update {
            it.copy(
                selectedExerciseId = exercise.id,
                practiceEditorBuffer = exercise.sourceCode,
                practiceLabOutput = "Ready to compile & run '${exercise.title}' on ${exercise.targetJdkId}."
            )
        }
    }

    fun updatePracticeEditorBuffer(newCode: String) {
        _uiState.update { it.copy(practiceEditorBuffer = newCode) }
    }

    fun compileAndRunPracticeExercise(exercise: PracticeExercise) {
        val code = _uiState.value.practiceEditorBuffer
        val jdk = jdks.value.find { it.versionId == exercise.targetJdkId } ?: resolveActiveJdk()
        val fileName = "${exercise.sourceCode.let { Regex("""class\s+(\w+)""").find(it)?.groupValues?.get(1) ?: "PracticeLab" }}.java"

        viewModelScope.launch {
            val (comp, exec) = withContext(Dispatchers.Default) {
                JavaExecutionEngine.compileAndExecute(
                    fileName = fileName,
                    sourceCode = code,
                    jdk = jdk,
                    stdinInput = _uiState.value.stdinInput
                )
            }
            val outputText = buildString {
                appendLine(comp.formattedConsoleOutput)
                if (comp.success && exec != null) {
                    appendLine("\n=== PROGRAM EXECUTION OUTPUT (${jdk.versionId}) ===")
                    appendLine("> ${exec.runtimeCommand}")
                    if (exec.stdout.isNotBlank()) appendLine(exec.stdout)
                    if (exec.stderr.isNotBlank()) appendLine(exec.stderr)
                    appendLine("[Exit Code: ${exec.exitCode} | Time: ${exec.executionTimeMs}ms]")
                }
            }.trimEnd()
            _uiState.update {
                it.copy(
                    practiceLabOutput = outputText,
                    lastCompilationResult = comp,
                    lastExecutionResult = exec,
                    statusBannerMessage = "Executed Practice Lab '${exercise.title}' on ${jdk.versionId}"
                )
            }
        }
    }

    // Quiz & Assessment Actions (Module 10)
    fun selectQuizSet(quizSetId: String) {
        _uiState.update {
            it.copy(
                selectedQuizSetId = quizSetId,
                currentQuizAnswers = emptyMap(),
                quizSubmitted = false,
                lastQuizScore = null
            )
        }
    }

    fun selectQuizAnswer(questionId: String, optionIndex: Int) {
        if (_uiState.value.quizSubmitted) return
        _uiState.update {
            val updated = it.currentQuizAnswers.toMutableMap().apply { put(questionId, optionIndex) }
            it.copy(currentQuizAnswers = updated)
        }
    }

    fun submitCurrentQuiz() {
        val state = _uiState.value
        val quizSet = QuizCatalog.quizSets.find { it.id == state.selectedQuizSetId } ?: return
        var correctCount = 0
        for (q in quizSet.questions) {
            if (state.currentQuizAnswers[q.id] == q.correctOptionIndex) {
                correctCount++
            }
        }
        viewModelScope.launch {
            repository.recordQuizSubmission(
                quizId = quizSet.id,
                quizTitle = quizSet.title,
                category = quizSet.category,
                score = correctCount,
                total = quizSet.questions.size
            )
        }
        _uiState.update {
            it.copy(
                quizSubmitted = true,
                lastQuizScore = correctCount,
                statusBannerMessage = "Quiz Graded: $correctCount / ${quizSet.questions.size} saved to SQLite!"
            )
        }
    }

    fun resetCurrentQuiz() {
        _uiState.update {
            it.copy(
                currentQuizAnswers = emptyMap(),
                quizSubmitted = false,
                lastQuizScore = null
            )
        }
    }

    // Offline Resource & Verification Test Actions (Modules 11 & 20)
    fun importNewOfflineResource(title: String, category: String, jdkVersion: String, path: String, attribution: String) {
        viewModelScope.launch {
            repository.importOfflineResource(title, category, jdkVersion, path, attribution)
            _uiState.update { it.copy(statusBannerMessage = "Imported offline resource '$title'") }
        }
    }

    fun removeOfflineResource(id: Long) {
        viewModelScope.launch {
            repository.removeOfflineResource(id)
        }
    }

    fun rerunAll35VerificationTests() {
        viewModelScope.launch {
            val report = withContext(Dispatchers.Default) {
                StudioVerificationSuite.runAll35VerificationTests()
            }
            val passedCount = report.count { it.status == "PASSED" }
            _uiState.update {
                it.copy(
                    verificationResults = report,
                    statusBannerMessage = "Executed 35 Verification Tests ($passedCount/35 PASSED)"
                )
            }
        }
    }
}
