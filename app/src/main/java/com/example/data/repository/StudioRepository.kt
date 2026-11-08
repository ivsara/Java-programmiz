package com.example.data.repository

import com.example.core.jdk.JdkCatalogAndDetector
import com.example.data.db.ExecutionHistoryEntity
import com.example.data.db.JdkInstallationEntity
import com.example.data.db.LearningProgressEntity
import com.example.data.db.LessonProgressEntity
import com.example.data.db.OfflineResourceEntity
import com.example.data.db.ProjectEntity
import com.example.data.db.QuizResultEntity
import com.example.data.db.SourceFileEntity
import com.example.data.db.StudioDao
import kotlinx.coroutines.flow.Flow

class StudioRepository(private val dao: StudioDao) {

    val allJdks: Flow<List<JdkInstallationEntity>> = dao.getAllJdks()
    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val allSourceFiles: Flow<List<SourceFileEntity>> = dao.getAllSourceFiles()
    val allLessonProgress: Flow<List<LessonProgressEntity>> = dao.getAllLessonProgress()
    val allQuizResults: Flow<List<QuizResultEntity>> = dao.getAllQuizResults()
    val recentExecutions: Flow<List<ExecutionHistoryEntity>> = dao.getRecentExecutions()
    val allResources: Flow<List<OfflineResourceEntity>> = dao.getAllResources()

    suspend fun ensureSeeded() {
        val existingJdks = dao.getAllJdksOnce()
        if (existingJdks.isEmpty()) {
            dao.insertJdks(JdkCatalogAndDetector.getDefaultJdkCatalog())
        }

        val existingProjects = dao.getAllProjectsOnce()
        if (existingProjects.isEmpty()) {
            seedDefaultMultiVersionProjects()
        }

        val existingResources = dao.getAllResourcesOnce()
        if (existingResources.isEmpty()) {
            seedOfflineResources()
        }
    }

    private suspend fun seedDefaultMultiVersionProjects() {
        // 1. Java Basics -> JDK 1.2 (Java 2)
        val p1Id = dao.insertProject(
            ProjectEntity(
                name = "Java Basics",
                description = "Historical Java 2 (JDK 1.2) fundamental console & Vector/ArrayList programs.",
                jdkVersionId = "JDK_1_2",
                packageName = "default",
                mainClass = "HelloWorld",
                templateType = "Java 2 Console Application"
            )
        )
        dao.insertSourceFile(
            SourceFileEntity(
                projectId = p1Id,
                fileName = "HelloWorld.java",
                packageName = "default",
                content = """import java.util.Vector;

public class HelloWorld {
    public static void main(String args[]) {
        System.out.println("Hello World");
        System.out.println("Running on Project JDK: Java 2 (JDK 1.2)");
        Vector items = new Vector();
        items.addElement("CAFEBABE Bytecode 46.0");
        items.addElement("Collections & Swing Era");
        for (int i = 0; i < items.size(); i++) {
            System.out.println("Feature " + (i + 1) + ": " + items.elementAt(i));
        }
    }
}"""
            )
        )
        dao.insertSourceFile(
            SourceFileEntity(
                projectId = p1Id,
                fileName = "MathHelper.java",
                packageName = "default",
                content = """public class MathHelper {
    public static int square(int n) {
        return n * n;
    }
}"""
            )
        )

        // 2. Java OOP -> JDK 1.4
        val p2Id = dao.insertProject(
            ProjectEntity(
                name = "Java OOP",
                description = "Object-Oriented Programming with classes, inheritance, polymorphism, and JDK 1.4 assertions.",
                jdkVersionId = "JDK_1_4",
                packageName = "com.studio.oop",
                mainClass = "OopLaboratory",
                templateType = "JDK 1.4 OOP Project"
            )
        )
        dao.insertSourceFile(
            SourceFileEntity(
                projectId = p2Id,
                fileName = "OopLaboratory.java",
                packageName = "com.studio.oop",
                content = """package com.studio.oop;

class Instrument {
    String name;
    Instrument(String name) {
        this.name = name;
    }
    String playNote() {
        return name + " plays standard concert pitch A4 (440 Hz)";
    }
}

class GrandPiano extends Instrument {
    GrandPiano(String name) {
        super(name);
    }
    String playNote() {
        return name + " resonates 88-key harmonic chord";
    }
}

public class OopLaboratory {
    public static void main(String args[]) {
        Instrument inst = new GrandPiano("Steinway Model D");
        assert inst != null : "Instrument must not be null";
        System.out.println("[JDK 1.4 OOP & Assert Verified]");
        System.out.println(inst.playNote());
    }
}"""
            )
        )

        // 3. Java Advanced -> JDK 8
        val p3Id = dao.insertProject(
            ProjectEntity(
                name = "Java Advanced",
                description = "Functional programming with Lambdas, Stream API, Generics, and Collections on JDK 8 LTS.",
                jdkVersionId = "JDK_8",
                packageName = "com.studio.advanced",
                mainClass = "StreamAnalytics",
                templateType = "JDK 8 Functional & Streams"
            )
        )
        dao.insertSourceFile(
            SourceFileEntity(
                projectId = p3Id,
                fileName = "StreamAnalytics.java",
                packageName = "com.studio.advanced",
                content = """package com.studio.advanced;
import java.util.*;

public class StreamAnalytics {
    public static void main(String[] args) {
        List<Integer> latencies = Arrays.asList(12, 45, 18, 90, 24, 60);
        System.out.println("[JDK 8 Stream Pipeline - Fast Latencies < 30ms Doubled]:");
        latencies.stream()
            .filter(ms -> ms < 30)
            .map(ms -> ms * 2)
            .sorted()
            .forEach(System.out::println);
    }
}"""
            )
        )

        // 4. Java Backend -> JDK 17
        val p4Id = dao.insertProject(
            ProjectEntity(
                name = "Java Backend",
                description = "Enterprise service modeling using JDK 17 LTS Records, Switch Expressions, and Text Blocks.",
                jdkVersionId = "JDK_17",
                packageName = "com.studio.backend",
                mainClass = "BackendService",
                templateType = "JDK 17 LTS Enterprise"
            )
        )
        dao.insertSourceFile(
            SourceFileEntity(
                projectId = p4Id,
                fileName = "BackendService.java",
                packageName = "com.studio.backend",
                content = """package com.studio.backend;

record ApiResponse(int statusCode, String payload) {}

public class BackendService {
    public static void main(String[] args) {
        ApiResponse resp = new ApiResponse(200, "OK: Universal Java Studio SQLite Connected");
        System.out.println("Response Record: " + resp);
        switch (resp.statusCode()) {
            case 200 -> System.out.println("HTTP Status 200 -> " + resp.payload());
            case 404 -> System.out.println("Not Found");
            default -> System.out.println("Unhandled Status");
        }
    }
}"""
            )
        )

        // 5. Modern Java -> JDK 21
        val p5Id = dao.insertProject(
            ProjectEntity(
                name = "Modern Java",
                description = "Project Loom Virtual Threads, Sequenced Collections, and Pattern Matching on JDK 21 LTS.",
                jdkVersionId = "JDK_21",
                packageName = "com.studio.modern",
                mainClass = "VirtualThreadDemo",
                templateType = "JDK 21 LTS Virtual Threads"
            )
        )
        dao.insertSourceFile(
            SourceFileEntity(
                projectId = p5Id,
                fileName = "VirtualThreadDemo.java",
                packageName = "com.studio.modern",
                content = """package com.studio.modern;
import java.util.*;

public class VirtualThreadDemo {
    public static void main(String[] args) {
        ArrayList<String> queue = new ArrayList<String>();
        queue.add("Task-Alpha");
        queue.add("Task-Beta");
        queue.addFirst("Task-Priority-Zero");
        System.out.println("Sequenced First: " + queue.getFirst());
        System.out.println("Sequenced Last : " + queue.getLast());
        Thread.ofVirtual().start(() -> {
            System.out.println("Virtual Thread executed queue of size " + queue.size());
        });
    }
}"""
            )
        )

        // 6. Latest Java -> JDK 25
        val p6Id = dao.insertProject(
            ProjectEntity(
                name = "Latest Java",
                description = "Next-generation Java SE 25 LTS with Compact Source Files, Scoped Values, and Stream Gatherers.",
                jdkVersionId = "JDK_25",
                packageName = "com.studio.jdk25",
                mainClass = "Java25Showcase",
                templateType = "JDK 25 LTS Next-Gen"
            )
        )
        dao.insertSourceFile(
            SourceFileEntity(
                projectId = p6Id,
                fileName = "Java25Showcase.java",
                packageName = "com.studio.jdk25",
                content = """package com.studio.jdk25;

record CompilerTarget(String jdkName, int classFileMajor) {}

public class Java25Showcase {
    public static void main(String[] args) {
        var target = new CompilerTarget("JDK 25 LTS", 69);
        System.out.println("Active Target : " + target);
        System.out.println("Bytecode Major: " + target.classFileMajor() + ".0 (0x45)");
    }
}"""
            )
        )
    }

    private suspend fun seedOfflineResources() {
        val defaultResources = listOf(
            OfflineResourceEntity(
                title = "IIT Kharagpur Programming in Java – Complete 11-Chapter Syllabus",
                category = "HTML Tutorial",
                targetJdkVersion = "JDK 1.2 – JDK 25",
                resourcePath = "resources/tutorials/iitkgp_java_chapters_1_to_11.html",
                status = "Verified Offline",
                attribution = "Adapted & structured from Prof. Debasis Samanta, IIT Kharagpur (https://cse.iitkgp.ac.in/~dsamanta/java/index.htm)"
            ),
            OfflineResourceEntity(
                title = "Oracle Java SE 7 & Historical Java 2 (JDK 1.2) API Reference Index",
                category = "API Docs",
                targetJdkVersion = "JDK 1.0 – JDK 7",
                resourcePath = "resources/api_docs/java_1_2_to_7/index.html",
                status = "Verified Offline",
                attribution = "Oracle Java SE API Specification (https://docs.oracle.com/javase/7/docs/api/)"
            ),
            OfflineResourceEntity(
                title = "Oracle Java Tutorials – Core Trails, Collections, Concurrency & Swing",
                category = "HTML Tutorial",
                targetJdkVersion = "JDK 1.2 – JDK 25",
                resourcePath = "resources/tutorials/oracle_java_trails.html",
                status = "Verified Offline",
                attribution = "Oracle Java Tutorials (https://docs.oracle.com/javase/tutorial/java/index.html)"
            ),
            OfflineResourceEntity(
                title = "Modern Java SE 8, 11, 17, 21 & 25 LTS API & Bytecode Specification",
                category = "API Docs",
                targetJdkVersion = "JDK 8 – JDK 25",
                resourcePath = "resources/api_docs/latest/index.html",
                status = "Verified Offline",
                attribution = "OpenJDK / JCP JSR Specifications (JDK 8 through JDK 25)"
            ),
            OfflineResourceEntity(
                title = "25-Category Java Practice Laboratory Source Bundle",
                category = "Exercise Pack",
                targetJdkVersion = "JDK 1.2 – JDK 25",
                resourcePath = "resources/examples/practice_lab_25_categories.zip",
                status = "Verified Offline",
                attribution = "Universal Java Studio Built-in Offline Curriculum"
            )
        )
        dao.insertResources(defaultResources)
    }

    suspend fun createNewProject(
        name: String,
        description: String,
        jdkVersionId: String,
        packageName: String,
        mainClass: String,
        initialCode: String
    ): Long {
        val projectId = dao.insertProject(
            ProjectEntity(
                name = name,
                description = description,
                jdkVersionId = jdkVersionId,
                packageName = packageName,
                mainClass = mainClass,
                templateType = "Custom Project ($jdkVersionId)"
            )
        )
        dao.insertSourceFile(
            SourceFileEntity(
                projectId = projectId,
                fileName = "$mainClass.java",
                packageName = packageName,
                content = initialCode
            )
        )
        return projectId
    }

    suspend fun switchProjectJdk(projectId: Long, jdkVersionId: String) {
        dao.updateProjectJdk(projectId, jdkVersionId)
    }

    suspend fun deleteProject(projectId: Long) {
        dao.deleteFilesForProject(projectId)
        dao.deleteProject(projectId)
    }

    suspend fun saveSourceFile(file: SourceFileEntity): Long {
        return dao.insertSourceFile(file.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSourceFile(fileId: Long) {
        dao.deleteSourceFile(fileId)
    }

    suspend fun addCustomJdk(jdk: JdkInstallationEntity) {
        dao.insertJdk(jdk)
    }

    suspend fun removeJdk(versionId: String) {
        dao.deleteJdk(versionId)
    }

    suspend fun setDefaultJdk(versionId: String) {
        dao.setDefaultJdk(versionId)
    }

    suspend fun toggleLessonCompleted(lessonId: String, chapterId: String, currentCompleted: Boolean, currentBookmarked: Boolean) {
        val nextCompleted = !currentCompleted
        dao.upsertLessonProgress(
            LessonProgressEntity(
                lessonId = lessonId,
                chapterId = chapterId,
                isCompleted = nextCompleted,
                isBookmarked = currentBookmarked,
                timeSpentMinutes = 15
            )
        )
        dao.upsertLearningProgress(
            LearningProgressEntity(
                itemId = lessonId,
                moduleType = "TUTORIAL",
                chapterOrCategory = chapterId,
                isCompleted = nextCompleted,
                isBookmarked = currentBookmarked,
                timeSpentMinutes = 15
            )
        )
    }

    suspend fun toggleLessonBookmark(lessonId: String, chapterId: String, currentCompleted: Boolean, currentBookmarked: Boolean) {
        val nextBookmarked = !currentBookmarked
        dao.upsertLessonProgress(
            LessonProgressEntity(
                lessonId = lessonId,
                chapterId = chapterId,
                isCompleted = currentCompleted,
                isBookmarked = nextBookmarked,
                timeSpentMinutes = 10
            )
        )
        dao.upsertLearningProgress(
            LearningProgressEntity(
                itemId = lessonId,
                moduleType = "TUTORIAL",
                chapterOrCategory = chapterId,
                isCompleted = currentCompleted,
                isBookmarked = nextBookmarked,
                timeSpentMinutes = 10
            )
        )
    }

    suspend fun recordQuizSubmission(quizId: String, quizTitle: String, category: String, score: Int, total: Int) {
        dao.insertQuizResult(
            QuizResultEntity(
                quizId = quizId,
                quizTitle = quizTitle,
                targetCategory = category,
                score = score,
                totalQuestions = total
            )
        )
    }

    suspend fun recordExecution(entry: ExecutionHistoryEntity) {
        dao.insertExecution(entry)
    }

    suspend fun importOfflineResource(title: String, category: String, jdkVersion: String, path: String, attribution: String) {
        dao.insertResource(
            OfflineResourceEntity(
                title = title,
                category = category,
                targetJdkVersion = jdkVersion,
                resourcePath = path,
                status = "Imported Offline",
                attribution = attribution
            )
        )
    }

    suspend fun removeOfflineResource(id: Long) {
        dao.deleteResource(id)
    }
}
