package com.example.data.content

data class QuizQuestion(
    val id: String,
    val quizSetId: String,
    val questionType: String, // "Multiple-Choice", "Fill-in-the-Blank", "Output Prediction", "Find-the-Error", "Debugging", "Version Compatibility"
    val targetVersionOrChapter: String,
    val prompt: String,
    val codeSnippet: String? = null,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

data class QuizSet(
    val id: String,
    val title: String,
    val category: String, // "Chapter Assessment" or "Version-Specific Quiz"
    val description: String,
    val questions: List<QuizQuestion>
)

object QuizCatalog {

    val quizSets: List<QuizSet> = listOf(
        QuizSet(
            id = "QUIZ_CH1_3",
            title = "Chapters 1–3: Java Fundamentals, Control Flow & OOP",
            category = "Chapter Assessment",
            description = "Covers JVM bytecode headers, primitive types, arrays, constructors, inheritance, and dynamic dispatch.",
            questions = listOf(
                QuizQuestion(
                    id = "Q_1",
                    quizSetId = "QUIZ_CH1_3",
                    questionType = "Multiple-Choice",
                    targetVersionOrChapter = "Chapter 1 – JVM & Bytecode",
                    prompt = "What magic 4-byte hexadecimal header identifies every valid compiled Java .class file from JDK 1.0 through JDK 25?",
                    options = listOf("0xCAFEBABE", "0xFEEDFACE", "0xdeadbeef", "0x7F454C46"),
                    correctOptionIndex = 0,
                    explanation = "Every Java .class file starts with the 4-byte magic number 0xCAFEBABE followed by 2 bytes for minor version and 2 bytes for major version (e.g. 46.0 for JDK 1.2)."
                ),
                QuizQuestion(
                    id = "Q_2",
                    quizSetId = "QUIZ_CH1_3",
                    questionType = "Output Prediction",
                    targetVersionOrChapter = "Chapter 2 – Operators & Arrays",
                    prompt = "Predict the exact output of the following Java 2 program:",
                    codeSnippet = """int a[] = {10, 20, 30};
int x = a[1] + a.length * 2;
System.out.println(x);""",
                    options = listOf("26", "16", "23", "36"),
                    correctOptionIndex = 0,
                    explanation = "a[1] is 20 and a.length is 3. Multiplication has higher precedence than addition: 20 + (3 * 2) = 26."
                ),
                QuizQuestion(
                    id = "Q_3",
                    quizSetId = "QUIZ_CH1_3",
                    questionType = "Find-the-Error",
                    targetVersionOrChapter = "Chapter 3 – OOP & Java 2 Compatibility",
                    prompt = "Why does the following code fail to compile when the project JDK is set to JDK 1.2 (Java 2)?",
                    codeSnippet = """class Sub extends Base {
    @Override
    void show() { System.out.println("Sub"); }
}""",
                    options = listOf(
                        "Annotations (@Override) were introduced in JDK 5 and do not exist in JDK 1.2",
                        "Method overriding is not supported in JDK 1.2",
                        "The 'extends' keyword requires JDK 1.4",
                        "void methods cannot be overridden"
                    ),
                    correctOptionIndex = 0,
                    explanation = "While method overriding has existed since JDK 1.0, metadata annotations like @Override were introduced in J2SE 5.0 (2004)."
                ),
                QuizQuestion(
                    id = "Q_4",
                    quizSetId = "QUIZ_CH1_3",
                    questionType = "Fill-in-the-Blank",
                    targetVersionOrChapter = "Chapter 3 – Inheritance",
                    prompt = "To invoke a superclass constructor from inside a subclass constructor, the _____ keyword must be the first statement.",
                    options = listOf("super(...)", "this(...)", "parent(...)", "base(...)"),
                    correctOptionIndex = 0,
                    explanation = "super(...) explicitly invokes the parent class constructor and must appear as the very first statement in the subclass constructor."
                )
            )
        ),
        QuizSet(
            id = "QUIZ_CH4_8",
            title = "Chapters 4–8: Interfaces, Exceptions, Threads, AWT & I/O",
            category = "Chapter Assessment",
            description = "Tests exception handling rules, thread safety, AWT delegation events, and historical vs modern console I/O.",
            questions = listOf(
                QuizQuestion(
                    id = "Q_5",
                    quizSetId = "QUIZ_CH4_8",
                    questionType = "Debugging",
                    targetVersionOrChapter = "Chapter 5 – Exception Handling",
                    prompt = "A student wants to read console input in a strict JDK 1.2 (Java 2) environment. Which class should they use instead of java.util.Scanner?",
                    options = listOf(
                        "java.io.BufferedReader wrapping new InputStreamReader(System.in)",
                        "java.util.Scanner(System.in)",
                        "java.io.Console.readLine()",
                        "java.nio.file.Files"
                    ),
                    correctOptionIndex = 0,
                    explanation = "BufferedReader and InputStreamReader have been available since JDK 1.1. java.util.Scanner was added in JDK 5, and java.io.Console in JDK 6."
                ),
                QuizQuestion(
                    id = "Q_6",
                    quizSetId = "QUIZ_CH4_8",
                    questionType = "Output Prediction",
                    targetVersionOrChapter = "Chapter 5 – try-catch-finally",
                    prompt = "What is printed when this code executes?",
                    codeSnippet = """try {
    int v = 10 / 0;
    System.out.print("A ");
} catch (ArithmeticException e) {
    System.out.print("B ");
} finally {
    System.out.print("C");
}""",
                    options = listOf("B C", "A B C", "B", "A C"),
                    correctOptionIndex = 0,
                    explanation = "10 / 0 throws ArithmeticException immediately, skipping 'A', executing catch ('B ') and then finally ('C')."
                ),
                QuizQuestion(
                    id = "Q_7",
                    quizSetId = "QUIZ_CH4_8",
                    questionType = "Multiple-Choice",
                    targetVersionOrChapter = "Chapter 6 – Multithreading",
                    prompt = "Why were Thread.stop(), Thread.suspend(), and Thread.resume() deprecated in JDK 1.2 (Java 2)?",
                    options = listOf(
                        "stop() unlocks all monitors leaving shared objects in damaged inconsistent states, and suspend() holds locks causing deadlocks",
                        "Multithreading was removed in JDK 1.2",
                        "They required 64-bit processors",
                        "They were replaced by Virtual Threads in JDK 1.2"
                    ),
                    correctOptionIndex = 0,
                    explanation = "Sun deprecated Thread.stop() and Thread.suspend() in JDK 1.2 because they are inherently unsafe and prone to corrupted invariants and deadlocks."
                )
            )
        ),
        QuizSet(
            id = "QUIZ_VERSIONS",
            title = "Java Version Evolution Quiz (JDK 1.0 to JDK 25)",
            category = "Version-Specific Quiz",
            description = "Assess your mastery of when language features, APIs, bytecode versions, and compiler flags were introduced.",
            questions = listOf(
                QuizQuestion(
                    id = "Q_8",
                    quizSetId = "QUIZ_VERSIONS",
                    questionType = "Version Compatibility",
                    targetVersionOrChapter = "JDK 1.2 vs JDK 1.4 vs JDK 5",
                    prompt = "Match the Java release that first introduced the 'assert' keyword:",
                    options = listOf("JDK 1.4 (2002)", "JDK 1.2 (1998)", "JDK 5 (2004)", "JDK 8 (2014)"),
                    correctOptionIndex = 0,
                    explanation = "The 'assert' keyword was added in J2SE 1.4 (Merlin) and required passing '-source 1.4' to javac."
                ),
                QuizQuestion(
                    id = "Q_9",
                    quizSetId = "QUIZ_VERSIONS",
                    questionType = "Version Compatibility",
                    targetVersionOrChapter = "Compiler Flags (--release vs -source/-target)",
                    prompt = "Why is compiling with 'javac -source 1.2 -target 1.2' WITHOUT '-bootclasspath <jdk1.2/rt.jar>' not equivalent to using an authentic JDK 1.2 compiler?",
                    options = listOf(
                        "Without a JDK 1.2 bootclasspath, the compiler links against the host JDK's modern rt.jar, allowing calls to newer APIs that fail at runtime on a real Java 2 VM",
                        "Because -source 1.2 automatically upgrades bytecode to 65.0",
                        "Because javac ignores -target when -source is specified",
                        "They are 100% identical in all cases"
                    ),
                    correctOptionIndex = 0,
                    explanation = "Language compatibility (-source) and bytecode version (-target) do not restrict the classpath API unless -bootclasspath (or --release in JDK 9+) is enforced."
                ),
                QuizQuestion(
                    id = "Q_10",
                    quizSetId = "QUIZ_VERSIONS",
                    questionType = "Version Compatibility",
                    targetVersionOrChapter = "Modern Java (JDK 16 – JDK 25)",
                    prompt = "Which Java LTS release finalized Project Loom Virtual Threads (Thread.ofVirtual()) and Sequenced Collections?",
                    options = listOf("JDK 21 LTS", "JDK 17 LTS", "JDK 11 LTS", "JDK 8 LTS"),
                    correctOptionIndex = 0,
                    explanation = "Virtual Threads (JEP 444) and Sequenced Collections (JEP 431) were finalized in Java SE 21 LTS (September 2023)."
                )
            )
        )
    )
}
