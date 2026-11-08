package com.example.data.content

data class TutorialLesson(
    val id: String,
    val chapterId: String,
    val title: String,
    val minimumJdkVersion: String,
    val compatibleJdkRange: String,
    val deprecationOrStatusNote: String,
    // 12 Required Structured Sections per Lesson
    val topicIntroduction: String,
    val definition: String,
    val detailedExplanation: String,
    val syntaxBlock: String,
    val importantConcepts: List<String>,
    val java2ExampleCode: String,
    val modernJavaExampleCode: String,
    val programExplanation: String,
    val sampleOutput: String,
    val commonErrors: List<String>,
    val practiceQuestions: List<String>,
    val relatedApiDocs: List<String>
)

data class TutorialChapter(
    val id: String,
    val numberLabel: String,
    val title: String,
    val categoryTag: String, // "CORE CHAPTER", "API MODULE", "MODERN JAVA"
    val referenceSource: String,
    val lessons: List<TutorialLesson>
)

object TutorialsCatalog {

    val chapters: List<TutorialChapter> = listOf(
        // CHAPTER 1 - FUNDAMENTALS OF JAVA
        TutorialChapter(
            id = "CH_1",
            numberLabel = "Chapter 1",
            title = "Fundamentals of Java",
            categoryTag = "CORE CHAPTER",
            referenceSource = "IIT Kharagpur Java Course & Oracle Java Tutorials",
            lessons = listOf(
                TutorialLesson(
                    id = "L_1_1",
                    chapterId = "CH_1",
                    title = "Introduction, History, JVM Architecture & First Program",
                    minimumJdkVersion = "JDK 1.0",
                    compatibleJdkRange = "JDK 1.0 – JDK 25+",
                    deprecationOrStatusNote = "Active Core Standard (JDK 25 adds JEP 512 Compact Source Files)",
                    topicIntroduction = "Java was initiated in 1991 by James Gosling and the Green Team at Sun Microsystems (originally named Oak) and officially released as JDK 1.0 in 1996. In December 1998, JDK 1.2 rebranded the platform as Java 2 (J2SE).",
                    definition = "Java is a class-based, object-oriented, platform-independent programming language designed around the 'Write Once, Run Anywhere' (WORA) principle using intermediate bytecode (.class) executed by the Java Virtual Machine (JVM).",
                    detailedExplanation = "The Java ecosystem consists of three core layers:\n1. JDK (Java Development Kit): Includes javac compiler, archiver (jar), debugger (jdb), javadoc, and the JRE.\n2. JRE (Java Runtime Environment): Provides standard class libraries (rt.jar in JDK 1.2–8; modular jimage in JDK 9+) and the JVM.\n3. JVM (Java Virtual Machine): Loads, verifies (CAFEBABE magic header + major.minor version), interprets, and JIT-compiles bytecode into native machine instructions.",
                    syntaxBlock = "// Standard Java 2 (JDK 1.2) Entry Point\npublic class ClassName {\n    public static void main(String args[]) {\n        System.out.println(\"Message\");\n    }\n}",
                    importantConcepts = listOf(
                        "Bytecode Portability: javac compiles .java source into .class files with versioned headers (46.0 for JDK 1.2, 52.0 for JDK 8, 69.0 for JDK 25).",
                        "Strict Class Naming: A public class must be saved in a file matching its exact case-sensitive name.",
                        "Historical vs Modern Entry Point: JDK 1.0–24 requires a enclosing class with public static void main(String[] args). JDK 25 standardizes compact instance main()."
                    ),
                    java2ExampleCode = """public class HelloWorld {
    public static void main(String args[]) {
        System.out.println("Hello World from Java 2 (JDK 1.2)");
        System.out.println("Bytecode Target: 46.0 (CAFEBABE)");
    }
}""",
                    modernJavaExampleCode = """public class HelloWorldModern {
    public static void main(String[] args) {
        var platform = "Modern Java SE (JDK 21 / 25)";
        System.out.println("Hello World from " + platform);
        System.out.println("Supports Virtual Threads, Records & Pattern Matching");
    }
}""",
                    programExplanation = "The HelloWorld class declares the static main method invoked by the JVM launcher. System.out refers to the standard output PrintStream inside java.lang.System.",
                    sampleOutput = "Hello World from Java 2 (JDK 1.2)\nBytecode Target: 46.0 (CAFEBABE)",
                    commonErrors = listOf(
                        "Omitting 'static' in main() causes a runtime error in JDK 1.2–24: Main method is not static.",
                        "Using lowercase 'string' instead of 'String' causes a compiler 'cannot find symbol' error.",
                        "Using 'var' in JDK 1.2–9 fails because local variable type inference was introduced in JDK 10."
                    ),
                    practiceQuestions = listOf(
                        "Modify the program to print your name, active JDK version, and the sum of 1998 + 27.",
                        "Compare what happens when you compile the Modern Java example using JDK 1.2 vs JDK 21."
                    ),
                    relatedApiDocs = listOf("java.lang.System", "java.io.PrintStream", "java.lang.String")
                )
            )
        ),

        // CHAPTER 2 - PROGRAMMING IN JAVA
        TutorialChapter(
            id = "CH_2",
            numberLabel = "Chapter 2",
            title = "Programming in Java: Types, Control Flow, Arrays & Strings",
            categoryTag = "CORE CHAPTER",
            referenceSource = "IIT Kharagpur Module 2 & Oracle Language Basics",
            lessons = listOf(
                TutorialLesson(
                    id = "L_2_1",
                    chapterId = "CH_2",
                    title = "Data Types, Operators, Control Statements & Arrays",
                    minimumJdkVersion = "JDK 1.0",
                    compatibleJdkRange = "JDK 1.0 – JDK 25+",
                    deprecationOrStatusNote = "Enhanced for-each added in JDK 5; Switch expressions added in JDK 14",
                    topicIntroduction = "Java is a strongly typed language with 8 primitive types (byte, short, int, long, float, double, char, boolean), reference types, structured control statements, and bounds-checked arrays.",
                    definition = "Primitive types store raw values on the stack or inline in objects, whereas arrays and Strings are heap-allocated objects with runtime bounds verification.",
                    detailedExplanation = "In Java 2 (JDK 1.2), array iteration requires a standard indexed for-loop (for (int i = 0; i < arr.length; i++)). Java 5 introduced the enhanced for-each loop (for (int x : arr)), Java 7 enabled Strings in switch statements, and Java 14 finalized arrow switch expressions (case 1 -> ...).",
                    syntaxBlock = "int[] scores = {85, 92, 78, 96};\nfor (int i = 0; i < scores.length; i++) {\n    System.out.println(scores[i]);\n}",
                    importantConcepts = listOf(
                        "8 Primitive Types: byte (8-bit), short (16-bit), int (32-bit), long (64-bit), float (32-bit IEEE 754), double (64-bit), char (16-bit Unicode), boolean.",
                        "Zero-based Bounds Checking: Accessing arr[arr.length] throws ArrayIndexOutOfBoundsException at runtime.",
                        "String Immutability: String objects cannot be mutated in place; use StringBuffer (JDK 1.0+) or StringBuilder (JDK 5+) for mutable concatenation."
                    ),
                    java2ExampleCode = """public class ArrayControlJava2 {
    public static void main(String args[]) {
        int numbers[] = {12, 25, 38, 41, 50};
        int sum = 0;
        for (int i = 0; i < numbers.length; i++) {
            sum += numbers[i];
        }
        StringBuffer sb = new StringBuffer("Java 2 Array Sum = ");
        sb.append(sum);
        System.out.println(sb.toString());
        System.out.println("Average = " + (sum / numbers.length));
    }
}""",
                    modernJavaExampleCode = """public class ArrayControlModern {
    public static void main(String[] args) {
        int[] numbers = {12, 25, 38, 41, 50};
        int sum = 0;
        for (int n : numbers) {
            sum += n;
        }
        StringBuilder sb = new StringBuilder("Modern Java Array Sum = ");
        sb.append(sum);
        System.out.println(sb.toString());
        System.out.println("Average = " + (sum / numbers.length));
    }
}""",
                    programExplanation = "The Java 2 example uses C-style array brackets (int numbers[]), an indexed for-loop, and thread-safe StringBuffer. The Modern Java example uses enhanced for-each and unsynchronized StringBuilder (introduced in JDK 5).",
                    sampleOutput = "Java 2 Array Sum = 166\nAverage = 33",
                    commonErrors = listOf(
                        "Using StringBuilder or for (int n : numbers) when compiling with JDK 1.2 or JDK 1.4 triggers a version compatibility error.",
                        "Using == instead of .equals() to compare String contents."
                    ),
                    practiceQuestions = listOf(
                        "Write a Java 2 compatible loop to find the maximum and minimum element in an integer array.",
                        "Test out-of-bounds protection by reading numbers[5] and inspecting the stack trace."
                    ),
                    relatedApiDocs = listOf("java.lang.String", "java.lang.StringBuffer", "java.lang.StringBuilder", "java.util.Arrays")
                )
            )
        ),

        // CHAPTER 3 - OBJECT-ORIENTED PROGRAMMING
        TutorialChapter(
            id = "CH_3",
            numberLabel = "Chapter 3",
            title = "Object-Oriented Programming (Classes, Inheritance & Polymorphism)",
            categoryTag = "CORE CHAPTER",
            referenceSource = "IIT Kharagpur Module 3 & Oracle OOP Guide",
            lessons = listOf(
                TutorialLesson(
                    id = "L_3_1",
                    chapterId = "CH_3",
                    title = "Classes, Constructors, Inheritance, Method Overriding & Dynamic Dispatch",
                    minimumJdkVersion = "JDK 1.0",
                    compatibleJdkRange = "JDK 1.0 – JDK 25+",
                    deprecationOrStatusNote = "@Override annotation added in JDK 5; Sealed hierarchies added in JDK 17",
                    topicIntroduction = "Object-Oriented Programming in Java models real-world entities using encapsulation, inheritance, abstraction, and runtime polymorphism (dynamic method dispatch).",
                    definition = "Inheritance allows a subclass to extend a superclass using 'extends', inheriting fields and methods while overriding behavior resolved dynamically at runtime based on the actual object type.",
                    detailedExplanation = "In Java 2 (JDK 1.2), method overriding is performed without annotations because '@Override' was introduced in JDK 5. When a subclass overrides an instance method, calling that method on a superclass reference invokes the subclass implementation via virtual method table (invokevirtual) lookup.",
                    syntaxBlock = "class Base {\n    void display() { System.out.println(\"Base\"); }\n}\nclass Derived extends Base {\n    void display() { System.out.println(\"Derived\"); }\n}",
                    importantConcepts = listOf(
                        "Constructor Overloading: Multiple constructors with distinct parameter lists initialize objects in different ways.",
                        "Dynamic Method Dispatch: Runtime resolution of overridden methods based on heap instance type.",
                        "Historical vs Modern: Never use @Override in JDK 1.2/1.4 code; always use @Override in JDK 5+ to catch signature mismatches."
                    ),
                    java2ExampleCode = """class Account {
    String owner;
    int balance;

    Account(String owner, int balance) {
        this.owner = owner;
        this.balance = balance;
    }

    int computeInterest() {
        return balance * 4 / 100;
    }
}

class SavingsAccount extends Account {
    SavingsAccount(String owner, int balance) {
        super(owner, balance);
    }

    // Overridden in Java 2 without @Override annotation
    int computeInterest() {
        return balance * 7 / 100;
    }
}

public class OopJava2Demo {
    public static void main(String args[]) {
        Account acc = new SavingsAccount("Ada Lovelace", 10000);
        System.out.println("Holder: " + acc.owner);
        System.out.println("Interest (Dynamic Dispatch): " + acc.computeInterest());
    }
}""",
                    modernJavaExampleCode = """class Account {
    String owner;
    int balance;

    Account(String owner, int balance) {
        this.owner = owner;
        this.balance = balance;
    }

    int computeInterest() {
        return balance * 4 / 100;
    }
}

class SavingsAccount extends Account {
    SavingsAccount(String owner, int balance) {
        super(owner, balance);
    }

    @Override
    int computeInterest() {
        return balance * 7 / 100;
    }
}

public class OopModernDemo {
    public static void main(String[] args) {
        Account acc = new SavingsAccount("Ada Lovelace", 10000);
        System.out.println("Holder: " + acc.owner);
        System.out.println("Interest (@Override Dispatch): " + acc.computeInterest());
    }
}""",
                    programExplanation = "SavingsAccount extends Account and overrides computeInterest(). Even when referenced as Account, the JVM invokes SavingsAccount.computeInterest() returning 700 instead of 400.",
                    sampleOutput = "Holder: Ada Lovelace\nInterest (Dynamic Dispatch): 700",
                    commonErrors = listOf(
                        "Adding @Override when target JDK is 1.2, 1.3, or 1.4 causes a compiler error.",
                        "Attempting to override a private, static, or final method."
                    ),
                    practiceQuestions = listOf(
                        "Create a FixedDepositAccount subclass that computes 9% interest and test polymorphic dispatch.",
                        "Add constructor overloading with a default balance of 1000."
                    ),
                    relatedApiDocs = listOf("java.lang.Object", "java.lang.Class")
                )
            )
        ),

        // CHAPTER 4 - INTERFACES AND PACKAGES
        TutorialChapter(
            id = "CH_4",
            numberLabel = "Chapter 4",
            title = "Interfaces and Packages",
            categoryTag = "CORE CHAPTER",
            referenceSource = "IIT Kharagpur Module 4 & Oracle Interfaces Guide",
            lessons = listOf(
                TutorialLesson(
                    id = "L_4_1",
                    chapterId = "CH_4",
                    title = "Defining Interfaces, Multiple Interface Implementation & Packages",
                    minimumJdkVersion = "JDK 1.0",
                    compatibleJdkRange = "JDK 1.0 – JDK 25+",
                    deprecationOrStatusNote = "Default & static interface methods added in JDK 8; private methods added in JDK 9",
                    topicIntroduction = "Interfaces specify behavioral contracts that classes implement, enabling multiple-type inheritance in Java while packages organize classes into hierarchical namespaces.",
                    definition = "An interface is an abstract type containing constant declarations (public static final) and method signatures. From JDK 8+, interfaces can also include default and static method implementations.",
                    detailedExplanation = "In JDK 1.0 through JDK 7, every method declared in an interface had to be abstract (no method body). In JDK 8, default methods were introduced so libraries like java.util.Collection could add .stream() and .forEach() without breaking existing implementations.",
                    syntaxBlock = "interface Printable {\n    void printSummary();\n}\npublic class Report implements Printable {\n    public void printSummary() { ... }\n}",
                    importantConcepts = listOf(
                        "Multiple Implementation: A class can extend only one superclass but implement any number of interfaces.",
                        "Package Namespace: package com.studio.model; must appear as the very first non-comment statement in a source file.",
                        "Access Protection: private, package-private (default), protected, and public control visibility across packages."
                    ),
                    java2ExampleCode = """interface Measurable {
    int getArea();
}

class RectangleShape implements Measurable {
    int width;
    int height;

    RectangleShape(int w, int h) {
        this.width = w;
        this.height = h;
    }

    public int getArea() {
        return width * height;
    }
}

public class InterfaceJava2Demo {
    public static void main(String args[]) {
        RectangleShape rect = new RectangleShape(12, 8);
        System.out.println("Rectangle Area = " + rect.getArea());
    }
}""",
                    modernJavaExampleCode = """public class InterfaceModernDemo {
    record CircleMetric(int radius) {
        int diameter() { return radius * 2; }
    }

    public static void main(String[] args) {
        CircleMetric c = new CircleMetric(10);
        System.out.println("Record Component: " + c);
        System.out.println("Diameter = " + c.diameter());
    }
}""",
                    programExplanation = "RectangleShape implements the Measurable contract and provides a concrete public getArea() method.",
                    sampleOutput = "Rectangle Area = 96",
                    commonErrors = listOf(
                        "Omitting the 'public' modifier when implementing an interface method (interface methods are implicitly public).",
                        "Writing 'default void foo()' in an interface when targeting JDK 1.2–7."
                    ),
                    practiceQuestions = listOf(
                        "Add a getPerimeter() method to Measurable and implement it in RectangleShape.",
                        "Test package imports with java.util.Vector (Java 2) vs java.util.List."
                    ),
                    relatedApiDocs = listOf("java.lang.Comparable", "java.io.Serializable", "java.lang.Cloneable")
                )
            )
        ),

        // CHAPTER 5 - EXCEPTION HANDLING
        TutorialChapter(
            id = "CH_5",
            numberLabel = "Chapter 5",
            title = "Exception Handling",
            categoryTag = "CORE CHAPTER",
            referenceSource = "IIT Kharagpur Module 5 & Oracle Exceptions Tutorial",
            lessons = listOf(
                TutorialLesson(
                    id = "L_5_1",
                    chapterId = "CH_5",
                    title = "Exception Hierarchy, try-catch-finally, throw, throws & Custom Exceptions",
                    minimumJdkVersion = "JDK 1.0",
                    compatibleJdkRange = "JDK 1.0 – JDK 25+",
                    deprecationOrStatusNote = "Try-with-resources & multi-catch added in JDK 7; Helpful NPEs added in JDK 14",
                    topicIntroduction = "Java provides structured exception handling to separate error-recovery logic from normal execution and prevent abrupt program crashes.",
                    definition = "An exception is an object extending java.lang.Throwable (divided into Checked Exceptions, RuntimeExceptions, and Errors) thrown when an abnormal condition occurs.",
                    detailedExplanation = "In Java 2 (JDK 1.2), resources like streams must be closed explicitly in a 'finally' block. Java 7 introduced try-with-resources (try (BufferedReader br = ...)) for automatic AutoCloseable management.",
                    syntaxBlock = "try {\n    int res = 100 / divisor;\n} catch (ArithmeticException e) {\n    System.out.println(\"Error: \" + e);\n} finally {\n    System.out.println(\"Cleanup complete\");\n}",
                    importantConcepts = listOf(
                        "Checked vs Unchecked: Subclasses of Exception (excluding RuntimeException) must be caught or declared in throws.",
                        "Finally Guarantee: The finally block always executes whether an exception is thrown or caught.",
                        "JDK 7 Try-with-Resources: Requires AutoCloseable, unavailable in JDK 1.0–6."
                    ),
                    java2ExampleCode = """public class ExceptionJava2Demo {
    public static void main(String args[]) {
        try {
            System.out.println("Dividing 100 by 0...");
            int result = 100 / 0;
            System.out.println("Result: " + result);
        } catch (ArithmeticException e) {
            System.out.println("Caught ArithmeticException: " + e);
        } finally {
            System.out.println("Finally block executed safely in Java 2.");
        }
    }
}""",
                    modernJavaExampleCode = """public class ExceptionModernDemo {
    public static void main(String[] args) {
        try {
            int[] data = {10, 20, 30};
            System.out.println("Accessing index 5...");
            System.out.println(data[5]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Caught Bounds Error: " + e);
        } finally {
            System.out.println("Cleanup complete.");
        }
    }
}""",
                    programExplanation = "Dividing an integer by zero throws java.lang.ArithmeticException: / by zero, which is intercepted by the catch block before the finally block runs.",
                    sampleOutput = "Dividing 100 by 0...\nCaught ArithmeticException: / by zero\nFinally block executed safely in Java 2.",
                    commonErrors = listOf(
                        "Placing catch (Exception e) before a specific catch (ArithmeticException e) causes an unreachable catch block compile error.",
                        "Using try (Resource r = ...) in JDK 1.2–6 fails compilation."
                    ),
                    practiceQuestions = listOf(
                        "Use 'throw new IllegalArgumentException(\"Negative age\")' and catch it in main().",
                        "Test removing the try-catch block to inspect the raw runtime stack trace in the Error Console."
                    ),
                    relatedApiDocs = listOf("java.lang.Throwable", "java.lang.Exception", "java.lang.RuntimeException", "java.lang.ArithmeticException")
                )
            )
        ),

        // CHAPTER 6 - MULTITHREADING
        TutorialChapter(
            id = "CH_6",
            numberLabel = "Chapter 6",
            title = "Multithreading and Concurrency",
            categoryTag = "CORE CHAPTER",
            referenceSource = "IIT Kharagpur Module 6 & Oracle Concurrency Tutorial",
            lessons = listOf(
                TutorialLesson(
                    id = "L_6_1",
                    chapterId = "CH_6",
                    title = "Thread Lifecycle, Runnable, Synchronization & Virtual Threads",
                    minimumJdkVersion = "JDK 1.0",
                    compatibleJdkRange = "JDK 1.0 – JDK 25+",
                    deprecationOrStatusNote = "Thread.stop()/suspend()/resume() deprecated in JDK 1.2; Virtual Threads finalized in JDK 21",
                    topicIntroduction = "Java has built-in language and runtime support for multithreaded concurrent execution from JDK 1.0, evolving from OS platform threads to millions of lightweight Virtual Threads in JDK 21.",
                    definition = "A thread is an independent path of execution within a process, created by extending java.lang.Thread, implementing java.lang.Runnable, or spawning a Virtual Thread (JDK 21+).",
                    detailedExplanation = "Notice a crucial historical milestone: JDK 1.2 deprecated Thread.stop(), Thread.suspend(), and Thread.resume() because they were inherently deadlock-prone. Modern Java 5 added java.util.concurrent executors, Java 8 enabled Runnable lambdas, and Java 21 introduced Project Loom Virtual Threads.",
                    syntaxBlock = "// Java 2 Thread\nclass Worker extends Thread {\n    public void run() { System.out.println(\"Running\"); }\n}\n// JDK 21 Virtual Thread\nThread.ofVirtual().start(() -> System.out.println(\"Virtual\"));",
                    importantConcepts = listOf(
                        "Thread States: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED.",
                        "Monitor Synchronization: The 'synchronized' keyword ensures mutual exclusion on an object's monitor lock.",
                        "Project Loom (JDK 21): Thread.ofVirtual() schedules lightweight user-mode threads on a small pool of OS carrier threads."
                    ),
                    java2ExampleCode = """class TaskRunner {
    String taskName;

    TaskRunner(String name) {
        this.taskName = name;
    }

    void runTask() {
        for (int step = 1; step <= 3; step++) {
            System.out.println("[" + taskName + "] Executing step " + step);
        }
    }
}

public class ThreadJava2Demo {
    public static void main(String args[]) {
        TaskRunner worker = new TaskRunner("Java2-Worker-Thread");
        worker.runTask();
        System.out.println("Worker completed safely without deprecated Thread.stop().");
    }
}""",
                    modernJavaExampleCode = """public class VirtualThreadModernDemo {
    public static void main(String[] args) {
        System.out.println("Launching JDK 21+ Virtual Thread Workload...");
        Thread.ofVirtual().start(() -> {
            System.out.println("[VirtualThread] Executing carrier-mounted task");
        });
        System.out.println("Main carrier completed.");
    }
}""",
                    programExplanation = "Demonstrates structured task execution in Java 2 and lightweight Virtual Thread execution in JDK 21+.",
                    sampleOutput = "[Java2-Worker-Thread] Executing step 1\n[Java2-Worker-Thread] Executing step 2\n[Java2-Worker-Thread] Executing step 3\nWorker completed safely without deprecated Thread.stop().",
                    commonErrors = listOf(
                        "Calling thread.run() directly instead of thread.start() executes on the current caller thread.",
                        "Calling Thread.ofVirtual() on JDK 17 or earlier causes a compilation error."
                    ),
                    practiceQuestions = listOf(
                        "Compare compiling VirtualThreadModernDemo on JDK 17 vs JDK 21.",
                        "Simulate a shared counter updated by two workers."
                    ),
                    relatedApiDocs = listOf("java.lang.Thread", "java.lang.Runnable", "java.util.concurrent.Executors")
                )
            )
        ),

        // CHAPTER 7 - JAVA GUI PROGRAMMING (AWT)
        TutorialChapter(
            id = "CH_7",
            numberLabel = "Chapter 7",
            title = "Java GUI Programming (AWT & Event Handling)",
            categoryTag = "CORE CHAPTER",
            referenceSource = "IIT Kharagpur Module 7 & Oracle AWT Reference",
            lessons = listOf(
                TutorialLesson(
                    id = "L_7_1",
                    chapterId = "CH_7",
                    title = "AWT Components, Frames, Layout Managers & Delegation Event Model",
                    minimumJdkVersion = "JDK 1.1",
                    compatibleJdkRange = "JDK 1.1 – JDK 25+",
                    deprecationOrStatusNote = "JDK 1.0 hierarchical handleEvent() deprecated in 1.1 in favor of ActionListener Delegation Model",
                    topicIntroduction = "The Abstract Window Toolkit (java.awt) is Java's original heavyweight GUI toolkit using native operating system peers for windows, buttons, labels, and graphics canvases.",
                    definition = "AWT containers (Frame, Panel) arrange components (Button, Label, TextField) using LayoutManagers (FlowLayout, BorderLayout, GridLayout) and handle user input via java.awt.event listeners.",
                    detailedExplanation = "JDK 1.1 replaced the JDK 1.0 inheritance-based event model with the Delegation Event Model: event sources (Button) register event listeners (ActionListener, MouseListener, KeyListener) and fire typed event objects (ActionEvent).",
                    syntaxBlock = "Frame f = new Frame(\"AWT Lab\");\nButton b = new Button(\"Click Me\");\nf.add(b);",
                    importantConcepts = listOf(
                        "Heavyweight Peers: Every AWT component binds to a native OS window widget.",
                        "Layout Managers: Decouple component positioning from fixed pixel coordinates.",
                        "Delegation Event Model (JDK 1.1+): Sources dispatch events only to registered Listener interfaces."
                    ),
                    java2ExampleCode = """import java.awt.*;

public class AwtJava2Demo {
    public static void main(String args[]) {
        Frame frame = new Frame("Java 2 AWT Calculator Window");
        Label lbl = new Label("Enter Value:");
        TextField tf = new TextField("128");
        Button btn = new Button("Compute Square");
        frame.add(lbl);
        frame.add(tf);
        frame.add(btn);
        System.out.println("Initialized AWT Frame with 3 Heavyweight Components.");
        System.out.println("Computed 128^2 = " + (128 * 128));
    }
}""",
                    modernJavaExampleCode = """import java.awt.*;

public class AwtModernDemo {
    public static void main(String[] args) {
        Frame frame = new Frame("Modern AWT Inspection Window");
        frame.add(new Label("Status: Ready"));
        frame.add(new Button("Execute Task"));
        System.out.println("AWT Frame initialized with Lambda ActionListener support.");
    }
}""",
                    programExplanation = "Creates a java.awt.Frame and attaches Label, TextField, and Button components, which Universal Java Studio's execution engine also captures in the GUI Component Inspector.",
                    sampleOutput = "Initialized AWT Frame with 3 Heavyweight Components.\nComputed 128^2 = 16384",
                    commonErrors = listOf(
                        "Forgetting to register a WindowListener for windowClosing leaves AWT Frame close buttons unresponsive.",
                        "Mixing heavyweight AWT components and lightweight Swing components without Z-order awareness."
                    ),
                    practiceQuestions = listOf(
                        "Add a second Button('Reset') to the AWT Frame and run the program to inspect the GUI component tree."
                    ),
                    relatedApiDocs = listOf("java.awt.Frame", "java.awt.Button", "java.awt.Label", "java.awt.event.ActionListener")
                )
            )
        ),

        // CHAPTER 8 - INPUT/OUTPUT AND NETWORKING
        TutorialChapter(
            id = "CH_8",
            numberLabel = "Chapter 8",
            title = "Input/Output Streams and Networking",
            categoryTag = "CORE CHAPTER",
            referenceSource = "IIT Kharagpur Module 8 & Oracle I/O and Networking",
            lessons = listOf(
                TutorialLesson(
                    id = "L_8_1",
                    chapterId = "CH_8",
                    title = "Byte/Character Streams, Console Input, TCP/UDP Sockets & HTTP",
                    minimumJdkVersion = "JDK 1.0",
                    compatibleJdkRange = "JDK 1.0 – JDK 25+",
                    deprecationOrStatusNote = "Reader/Writer added in JDK 1.1; NIO added in 1.4; NIO.2 in 7; HttpClient in 11",
                    topicIntroduction = "Java performs input and output through sequential streams of bytes (InputStream/OutputStream) and Unicode characters (Reader/Writer), plus socket networking in java.net.",
                    definition = "Streams abstract files, memory buffers, console pipes, and network sockets behind uniform read/write operations.",
                    detailedExplanation = "In Java 2 (JDK 1.2), console input is read by wrapping System.in inside an InputStreamReader and BufferedReader (because java.util.Scanner did not exist until JDK 5!). Similarly, HTTP networking in JDK 1.2–10 uses java.net.URL and HttpURLConnection, whereas JDK 11+ provides standard java.net.http.HttpClient.",
                    syntaxBlock = "// Java 2 Console Input\nBufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n// Java 5+ Console Input\nScanner sc = new Scanner(System.in);",
                    importantConcepts = listOf(
                        "Byte vs Character Streams: InputStream/OutputStream handle raw 8-bit bytes; Reader/Writer (added in JDK 1.1) handle 16-bit Unicode characters.",
                        "Historical Authenticity: Never use java.util.Scanner when targeting JDK 1.2 or 1.4.",
                        "TCP vs UDP: ServerSocket/Socket provide reliable TCP streams; DatagramSocket/DatagramPacket provide connectionless UDP datagrams."
                    ),
                    java2ExampleCode = """import java.io.*;

public class IoNetJava2Demo {
    public static void main(String args[]) {
        String payload = "GET /index.html HTTP/1.0";
        System.out.println("[Java 2 java.io / java.net] Opening Buffered Stream...");
        System.out.println("Payload Length: " + payload.length() + " bytes");
        System.out.println("Upper Header  : " + payload.toUpperCase());
    }
}""",
                    modernJavaExampleCode = """import java.util.Scanner;

public class IoScannerModernDemo {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        String line = sc.nextLine();
        System.out.println("Read integer from stdin: " + a);
        System.out.println("Remaining stdin text   : " + line);
    }
}""",
                    programExplanation = "Compares Java 2 stream manipulation with Java 5+ Scanner standard input parsing.",
                    sampleOutput = "[Java 2 java.io / java.net] Opening Buffered Stream...\nPayload Length: 24 bytes\nUpper Header  : GET /INDEX.HTML HTTP/1.0",
                    commonErrors = listOf(
                        "Importing java.util.Scanner in a JDK 1.2 project triggers a compiler compatibility error.",
                        "Not handling IOException when working with FileInputStream or Socket."
                    ),
                    practiceQuestions = listOf(
                        "Enter '256 Hello Studio' in the Standard Input box at the bottom of the IDE and run IoScannerModernDemo on JDK 8+."
                    ),
                    relatedApiDocs = listOf("java.io.InputStream", "java.io.BufferedReader", "java.net.Socket", "java.net.URL", "java.net.http.HttpClient")
                )
            )
        ),

        // CHAPTER 9 - JAVA MULTIMEDIA & APPLETS
        TutorialChapter(
            id = "CH_9",
            numberLabel = "Chapter 9",
            title = "Java Multimedia & Historical Applets",
            categoryTag = "CORE CHAPTER",
            referenceSource = "IIT Kharagpur Module 9 (Applets & Multimedia)",
            lessons = listOf(
                TutorialLesson(
                    id = "L_9_1",
                    chapterId = "CH_9",
                    title = "Applet Lifecycle (init, start, paint, stop, destroy), Audio & Graphics",
                    minimumJdkVersion = "JDK 1.0",
                    compatibleJdkRange = "JDK 1.0 – JDK 8 (Deprecated in JDK 9, Marked for Removal in JDK 17+)",
                    deprecationOrStatusNote = "DEPRECATED IN JDK 9 / OBVIOUS HISTORICAL TECHNOLOGY — Switch to JDK 1.2–8 for warning-free Applet compilation",
                    topicIntroduction = "Java Applets (java.applet.Applet) powered interactive browser multimedia in the late 1990s and 2000s and remain a foundational chapter in classical university syllabi (such as IIT Kharagpur).",
                    definition = "An Applet is a Graphical Panel program that does not use main(); instead, the AppletViewer or browser invokes init(), start(), paint(Graphics g), stop(), and destroy().",
                    detailedExplanation = "Universal Java Studio preserves historical Applet compilation and AppletViewer lifecycle execution for JDK 1.0–8 while accurately issuing deprecation/removal warnings when compiled under JDK 9–25.",
                    syntaxBlock = "import java.applet.Applet;\nimport java.awt.Graphics;\npublic class BannerApplet extends Applet {\n    public void paint(Graphics g) {\n        g.drawString(\"Hello Java 2 Applet\", 20, 40);\n    }\n}",
                    importantConcepts = listOf(
                        "No main() Required: Applets are driven by the host container lifecycle (init -> start -> paint -> stop -> destroy).",
                        "Security Sandbox: Untrusted Applets ran under a strict SecurityManager restricting local file and network access.",
                        "Deprecation Timeline: Deprecated in Java 9 (JEP 289) and slated for removal in Java 17 (JEP 398)."
                    ),
                    java2ExampleCode = """import java.applet.Applet;
import java.awt.Graphics;

public class HelloJava2Applet extends Applet {
    public void init() {
        System.out.println("Applet init(): Loading multimedia resources...");
    }

    public void paint(Graphics g) {
        g.drawString("Welcome to Java 2 AppletViewer (JDK 1.2)!", 20, 30);
    }
}""",
                    modernJavaExampleCode = """public class ModernGraphicsAlternative {
    public static void main(String[] args) {
        System.out.println("In Modern Java (JDK 17–25), Applets are replaced by:");
        System.out.println("1. Standalone Swing / JavaFX Desktop Apps (jpackage)");
        System.out.println("2. WebAssembly / Server-Side Backends");
    }
}""",
                    programExplanation = "When HelloJava2Applet is executed in Universal Java Studio, the Execution Engine detects 'extends Applet' and invokes init() and paint(Graphics g) in AppletViewer mode.",
                    sampleOutput = "[AppletViewer JDK_1_2] Initializing Applet: HelloJava2Applet\n[AppletViewer] Calling init() -> start() -> paint(Graphics g)\nApplet init(): Loading multimedia resources...\n[AWT Graphics] drawString(\"Welcome to Java 2 AppletViewer (JDK 1.2)!\")",
                    commonErrors = listOf(
                        "Trying to run an Applet with standard java launcher instead of appletviewer.",
                        "Expecting modern browsers or JDK 21+ to support browser plug-in Applets without deprecation warnings."
                    ),
                    practiceQuestions = listOf(
                        "Compile HelloJava2Applet under JDK 1.2 (0 warnings) and then under JDK 17 (inspect the [removal] warning)."
                    ),
                    relatedApiDocs = listOf("java.applet.Applet", "java.applet.AudioClip", "java.awt.Graphics", "java.awt.Image")
                )
            )
        ),

        // CHAPTER 10 - JAVA SWING
        TutorialChapter(
            id = "CH_10",
            numberLabel = "Chapter 10",
            title = "Java Swing (Lightweight JFC GUI)",
            categoryTag = "CORE CHAPTER",
            referenceSource = "IIT Kharagpur Module 10 & Oracle Swing Tutorial",
            lessons = listOf(
                TutorialLesson(
                    id = "L_10_1",
                    chapterId = "CH_10",
                    title = "AWT vs Swing, JFrame, JButton, JLabel, JTextField & JPanel",
                    minimumJdkVersion = "JDK 1.2 (Java 2)",
                    compatibleJdkRange = "JDK 1.2 – JDK 25+",
                    deprecationOrStatusNote = "Introduced as core javax.swing in Java 2 (JDK 1.2); unavailable in JDK 1.0/1.1 core",
                    topicIntroduction = "Introduced as a core part of Java 2 (JDK 1.2) under the Java Foundation Classes (JFC), Swing provides lightweight, platform-consistent GUI components prefixed with 'J'.",
                    definition = "Unlike heavyweight AWT components, Swing components (JButton, JLabel, JTextField, JPanel, JTable) are painted purely in Java using Java 2D on top of a root top-level window (JFrame).",
                    detailedExplanation = "Swing uses a Model-View-Controller (MVC) architecture, supports Pluggable Look and Feel (Metal, Nimbus, System), and remains actively supported in java.desktop from JDK 1.2 through JDK 25.",
                    syntaxBlock = "import javax.swing.*;\nJFrame frame = new JFrame(\"Swing App\");\nJButton btn = new JButton(\"Submit\");\nframe.add(btn);",
                    importantConcepts = listOf(
                        "Lightweight Rendering: Swing components do not allocate native OS peer handles.",
                        "Java 2 Milestone: javax.swing is a defining addition of JDK 1.2; compiling javax.swing on JDK 1.0/1.1 fails.",
                        "Event Dispatch Thread (EDT): All Swing UI updates should occur on the EDT via SwingUtilities.invokeLater."
                    ),
                    java2ExampleCode = """import javax.swing.*;

public class SwingJava2Demo {
    public static void main(String args[]) {
        JFrame frame = new JFrame("Java 2 Swing Studio Window");
        JLabel label = new JLabel("JDK 1.2 Lightweight JFC Component");
        JTextField field = new JTextField("Universal Java Studio");
        JButton button = new JButton("Compile & Run");
        frame.add(label);
        frame.add(field);
        frame.add(button);
        System.out.println("Swing JFrame configured with JLabel, JTextField, and JButton.");
    }
}""",
                    modernJavaExampleCode = """import javax.swing.*;

public class SwingModernDemo {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Modern Swing Window (JDK 21)");
        frame.add(new JLabel("High-DPI Scaled Swing UI"));
        frame.add(new JButton("Launch Action"));
        System.out.println("Modern Swing JFrame ready on java.desktop module.");
    }
}""",
                    programExplanation = "Constructs a JFrame and adds JLabel, JTextField, and JButton components, rendering both console output and the visual component list in the bottom output panel.",
                    sampleOutput = "Swing JFrame configured with JLabel, JTextField, and JButton.",
                    commonErrors = listOf(
                        "Attempting to compile javax.swing under JDK 1.0 or JDK 1.1 causes aJAVA_2_API_REQUIRED error.",
                        "Blocking the Swing Event Dispatch Thread with long-running I/O."
                    ),
                    practiceQuestions = listOf(
                        "Try compiling SwingJava2Demo with JDK 1.0 selected to observe the JDK 1.2 version requirement error, then switch to JDK 1.2 and run it."
                    ),
                    relatedApiDocs = listOf("javax.swing.JFrame", "javax.swing.JButton", "javax.swing.JLabel", "javax.swing.JTextField", "javax.swing.JPanel")
                )
            )
        ),

        // CHAPTER 11 - JDBC
        TutorialChapter(
            id = "CH_11",
            numberLabel = "Chapter 11",
            title = "JDBC (Java Database Connectivity)",
            categoryTag = "CORE CHAPTER",
            referenceSource = "IIT Kharagpur Module 11 & Oracle JDBC Guide",
            lessons = listOf(
                TutorialLesson(
                    id = "L_11_1",
                    chapterId = "CH_11",
                    title = "JDBC Architecture, Drivers, Connections, Statements, ResultSets & Transactions",
                    minimumJdkVersion = "JDK 1.1",
                    compatibleJdkRange = "JDK 1.1 – JDK 25+",
                    deprecationOrStatusNote = "JDBC 2.0 added in JDK 1.2 (Scrollable ResultSets, Batch Updates); Type-1 JDBC-ODBC Bridge removed in JDK 8",
                    topicIntroduction = "JDBC (java.sql) is the standard Java API for connecting to relational databases (SQLite, PostgreSQL, MySQL, Oracle), executing SQL queries, and processing tabular result sets.",
                    definition = "JDBC uses DriverManager or DataSource to obtain a Connection, executes SQL via Statement or PreparedStatement, and iterates rows via ResultSet.",
                    detailedExplanation = "In JDK 1.2–5, developers explicitly called Class.forName(\"driver.ClassName\") to register the JDBC driver. Starting in JDBC 4.0 (JDK 6), Service Provider discovery automatically loads drivers on the classpath. Furthermore, the legacy sun.jdbc.odbc.JdbcOdbcDriver was removed in JDK 8.",
                    syntaxBlock = "Connection conn = DriverManager.getConnection(url);\nPreparedStatement ps = conn.prepareStatement(\"SELECT * FROM students WHERE id = ?\");\nResultSet rs = ps.executeQuery();",
                    importantConcepts = listOf(
                        "PreparedStatement Security: Always use parameterized PreparedStatements (?) instead of string concatenation to prevent SQL Injection.",
                        "Transaction Control: conn.setAutoCommit(false), conn.commit(), and conn.rollback() ensure ACID atomicity.",
                        "JDBC Evolution: JDBC 1.0 (JDK 1.1) -> JDBC 2.0 (JDK 1.2) -> JDBC 3.0 (JDK 1.4) -> JDBC 4.0 (JDK 6) -> JDBC 4.3 (JDK 9+)."
                    ),
                    java2ExampleCode = """import java.sql.*;
import java.util.Vector;

public class JdbcJava2Demo {
    public static void main(String args[]) {
        System.out.println("[JDBC 2.0 - JDK 1.2] Connecting to local SQLite learning store...");
        Vector rows = new Vector();
        rows.addElement("101 | Ada Lovelace | Score: 98");
        rows.addElement("102 | James Gosling | Score: 100");
        for (int i = 0; i < rows.size(); i++) {
            System.out.println("ResultSet Row " + (i + 1) + ": " + rows.elementAt(i));
        }
        System.out.println("Transaction committed successfully.");
    }
}""",
                    modernJavaExampleCode = """import java.sql.*;
import java.util.List;

public class JdbcModernDemo {
    record StudentRow(int id, String name, int score) {}

    public static void main(String[] args) {
        System.out.println("[JDBC 4.3 - Modern Java] Mapping ResultSet to Immutable Records:");
        StudentRow s1 = new StudentRow(101, "Ada Lovelace", 98);
        StudentRow s2 = new StudentRow(102, "James Gosling", 100);
        System.out.println(s1);
        System.out.println(s2);
    }
}""",
                    programExplanation = "Illustrates tabular query processing in Java 2 using Vector vs Modern Java using immutable Record mappings.",
                    sampleOutput = "[JDBC 2.0 - JDK 1.2] Connecting to local SQLite learning store...\nResultSet Row 1: 101 | Ada Lovelace | Score: 98\nResultSet Row 2: 102 | James Gosling | Score: 100\nTransaction committed successfully.",
                    commonErrors = listOf(
                        "Using sun.jdbc.odbc.JdbcOdbcDriver on JDK 8+ fails because the JDBC-ODBC bridge was removed.",
                        "Forgetting to close ResultSet, Statement, and Connection (use try-with-resources in JDK 7+)."
                    ),
                    practiceQuestions = listOf(
                        "Add a third student record to the JDBC result list and compute the average score."
                    ),
                    relatedApiDocs = listOf("java.sql.Connection", "java.sql.DriverManager", "java.sql.PreparedStatement", "java.sql.ResultSet")
                )
            )
        ),

        // ADDITIONAL JAVA API MODULES
        TutorialChapter(
            id = "CH_API_MODULES",
            numberLabel = "Core API Modules",
            title = "Core Java Packages (java.lang, java.util, java.io, java.net, AWT, Swing, SQL)",
            categoryTag = "API MODULE",
            referenceSource = "Oracle Java SE API Specification (JDK 1.2 to JDK 25)",
            lessons = listOf(
                TutorialLesson(
                    id = "L_API_1",
                    chapterId = "CH_API_MODULES",
                    title = "Mastering java.lang, java.util (Legacy Vector/Hashtable vs Collections), java.io & java.net",
                    minimumJdkVersion = "JDK 1.0 / JDK 1.2",
                    compatibleJdkRange = "JDK 1.0 – JDK 25+",
                    deprecationOrStatusNote = "Vector & Hashtable synchronized since JDK 1.0; ArrayList & HashMap introduced in JDK 1.2; Generics added in JDK 5",
                    topicIntroduction = "Understanding how core Java packages evolved from JDK 1.0 to JDK 1.2 (Java 2) and Modern Java is critical for maintaining both legacy and modern codebases.",
                    definition = "java.lang is implicitly imported into every source file; java.util provides data structures, collections, and utilities; java.io and java.net provide I/O and networking.",
                    detailedExplanation = "In JDK 1.0/1.1, developers only had Vector, Stack, Hashtable, and Enumeration. JDK 1.2 introduced the unified Collections Framework (List, ArrayList, LinkedList, Set, HashSet, Map, HashMap, Iterator, Collections). However, in JDK 1.2–1.4, collections stored raw Object references requiring explicit casts. JDK 5 added compile-time Generics <T>.",
                    syntaxBlock = "// JDK 1.2 Raw ArrayList\nList list = new ArrayList();\nlist.add(\"Java 2\");\nString s = (String) list.get(0);\n\n// JDK 5+ Generic ArrayList\nList<String> list = new ArrayList<String>();",
                    importantConcepts = listOf(
                        "java.lang: Object, String, StringBuffer, Math, System, Thread, Throwable, Integer, Double.",
                        "java.util (JDK 1.0): Vector, Hashtable, Stack, Dictionary, Enumeration, Date.",
                        "java.util (JDK 1.2): Collection, List, ArrayList, LinkedList, Set, HashSet, TreeSet, Map, HashMap, TreeMap, Iterator."
                    ),
                    java2ExampleCode = """import java.util.*;

public class CollectionsJava2Demo {
    public static void main(String args[]) {
        // Raw ArrayList & HashMap in Java 2 (No Generics!)
        ArrayList languages = new ArrayList();
        languages.add("Java 1.2 (1998)");
        languages.add("Java 1.4 (2002)");
        languages.add("Collections Framework");

        System.out.println("Count: " + languages.size());
        for (int i = 0; i < languages.size(); i++) {
            String item = (String) languages.get(i);
            System.out.println("Item " + i + ": " + item);
        }
    }
}""",
                    modernJavaExampleCode = """import java.util.*;

public class CollectionsModernDemo {
    public static void main(String[] args) {
        List<String> languages = List.of("Java 8 LTS", "Java 17 LTS", "Java 21 LTS", "Java 25 LTS");
        System.out.println("Immutable List Count: " + languages.size());
        for (String item : languages) {
            System.out.println("Modern Item: " + item);
        }
    }
}""",
                    programExplanation = "Contrasts raw-type ArrayList with explicit (String) casting in Java 2 against generic List<String> and JDK 9 List.of() in Modern Java.",
                    sampleOutput = "Count: 3\nItem 0: Java 1.2 (1998)\nItem 1: Java 1.4 (2002)\nItem 2: Collections Framework",
                    commonErrors = listOf(
                        "Writing ArrayList<String> in JDK 1.2 or 1.4 fails because Generics require JDK 5+.",
                        "Using ArrayList in JDK 1.0 or 1.1 fails because ArrayList was introduced in JDK 1.2 (use Vector in 1.0/1.1)."
                    ),
                    practiceQuestions = listOf(
                        "Run CollectionsJava2Demo under JDK 1.2, then try compiling it under JDK 1.0 to see the Java 2 API boundary check."
                    ),
                    relatedApiDocs = listOf("java.lang.Object", "java.util.Vector", "java.util.ArrayList", "java.util.HashMap", "java.util.Collections")
                )
            )
        ),

        // MODERN JAVA LEARNING (JDK 5 TO JDK 25)
        TutorialChapter(
            id = "CH_MODERN_JAVA",
            numberLabel = "Modern Java (JDK 5–25)",
            title = "Modern Java Evolution: Generics, Lambdas, Streams, Records, Sealed Classes & Virtual Threads",
            categoryTag = "MODERN JAVA",
            referenceSource = "Oracle Java SE 5 through Java SE 25 Release Specifications",
            lessons = listOf(
                TutorialLesson(
                    id = "L_MOD_1",
                    chapterId = "CH_MODERN_JAVA",
                    title = "Generics (JDK 5), Lambdas & Stream API (JDK 8)",
                    minimumJdkVersion = "JDK 5 / JDK 8",
                    compatibleJdkRange = "JDK 8 – JDK 25+",
                    deprecationOrStatusNote = "Active Modern Foundation (Stream Gatherers added in JDK 24)",
                    topicIntroduction = "Java 5 introduced compile-time type safety with Generics, and Java 8 revolutionized the language with functional programming via Lambda Expressions (->), Method References (::), and the Stream API.",
                    definition = "A Stream is a declarative pipeline of aggregate operations (filter, map, sorted, collect, reduce) evaluated lazily over a data source without mutating the underlying collection.",
                    detailedExplanation = "Before Java 8, filtering and transforming collections required verbose imperative loops or anonymous inner classes. With Lambdas and Streams, developers express data transformations concisely using functional interfaces (Predicate, Function, Consumer).",
                    syntaxBlock = "List<Integer> nums = Arrays.asList(10, 15, 20, 25, 30);\nnums.stream()\n    .filter(n -> n >= 20)\n    .map(n -> n * 2)\n    .forEach(System.out::println);",
                    importantConcepts = listOf(
                        "Type Erasure (JDK 5): Generic type parameters are verified at compile time and erased to Object/bounds in bytecode.",
                        "Functional Interfaces (JDK 8): Interfaces with a single abstract method (@FunctionalInterface) can be instantiated via lambda expressions.",
                        "Stream Pipeline (JDK 8): Source -> Intermediate Operations (filter, map, sorted) -> Terminal Operation (forEach, collect, count)."
                    ),
                    java2ExampleCode = """import java.util.*;

public class ImperativeFilterJava2 {
    public static void main(String args[]) {
        int values[] = {10, 15, 20, 25, 30};
        System.out.println("Java 2 Imperative Filter (n >= 20, doubled):");
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= 20) {
                System.out.println(values[i] * 2);
            }
        }
    }
}""",
                    modernJavaExampleCode = """import java.util.*;

public class StreamLambdaModern {
    public static void main(String[] args) {
        List<Integer> values = Arrays.asList(10, 15, 20, 25, 30);
        System.out.println("JDK 8+ Declarative Stream Pipeline (n >= 20, doubled):");
        values.stream()
            .filter(n -> n >= 20)
            .map(n -> n * 2)
            .forEach(System.out::println);
    }
}""",
                    programExplanation = "Both programs filter numbers >= 20 and double them, outputting 40, 50, 60. The Java 2 version runs on all JDKs (1.0–25), while the Stream version requires JDK 8+.",
                    sampleOutput = "JDK 8+ Declarative Stream Pipeline (n >= 20, doubled):\n40\n50\n60",
                    commonErrors = listOf(
                        "Compiling -> or .stream() with JDK 1.2–7 selected fails with LAMBDA_JDK8_REQUIRED.",
                        "Reusing a consumed Stream instance twice throws IllegalStateException."
                    ),
                    practiceQuestions = listOf(
                        "Modify the stream pipeline to filter odd numbers and triple them."
                    ),
                    relatedApiDocs = listOf("java.util.stream.Stream", "java.util.function.Predicate", "java.util.Optional")
                ),
                TutorialLesson(
                    id = "L_MOD_2",
                    chapterId = "CH_MODERN_JAVA",
                    title = "Switch Expressions (14), Text Blocks (15), Records (16), Sealed Classes (17) & JDK 21–25",
                    minimumJdkVersion = "JDK 17 / JDK 21 / JDK 25",
                    compatibleJdkRange = "JDK 17 – JDK 25+",
                    deprecationOrStatusNote = "Active Modern Java (Project Amber & Project Loom)",
                    topicIntroduction = "Between JDK 14 and JDK 25, Project Amber modernized Java with data-oriented programming: Switch Expressions, multi-line Text Blocks, immutable Records, Sealed Classes, and Pattern Matching.",
                    definition = "A Record (JDK 16+) is a transparent, immutable data carrier class that automatically generates canonical constructors, component accessors, equals(), hashCode(), and toString().",
                    detailedExplanation = "Combining Records (JDK 16), Sealed Interfaces (JDK 17), Text Blocks (JDK 15), Switch Expressions (JDK 14), and Virtual Threads (JDK 21) eliminates boilerplate while maintaining strict static typing.",
                    syntaxBlock = "record Developer(String name, int jdkVersion) {}\nDeveloper dev = new Developer(\"Gosling\", 25);\nSystem.out.println(dev.name() + \" -> \" + dev);",
                    importantConcepts = listOf(
                        "Switch Expressions (JDK 14): Arrow labels (case 1 -> ...) prevent accidental fall-through.",
                        "Text Blocks (JDK 15): Triple quotes (\"\"\") preserve multi-line JSON/SQL/HTML formatting without escape clutter.",
                        "Records (JDK 16) & Sealed Classes (JDK 17): Enable algebraic data types and exhaustive pattern matching in JDK 21–25."
                    ),
                    java2ExampleCode = """class DeveloperJava2 {
    String name;
    int jdkVersion;

    DeveloperJava2(String name, int jdkVersion) {
        this.name = name;
        this.jdkVersion = jdkVersion;
    }

    String describe() {
        return "DeveloperJava2[name=" + name + ", jdkVersion=" + jdkVersion + "]";
    }
}

public class DataCarrierJava2 {
    public static void main(String args[]) {
        DeveloperJava2 d = new DeveloperJava2("Duke", 2);
        System.out.println(d.describe());
    }
}""",
                    modernJavaExampleCode = """record Developer(String name, int jdkVersion) {}

public class ModernJava25Showcase {
    public static void main(String[] args) {
        Developer dev = new Developer("Duke", 25);
        System.out.println("Record toString : " + dev);
        System.out.println("Accessor name() : " + dev.name());

        int release = dev.jdkVersion();
        switch (release) {
            case 2 -> System.out.println("Era: Historical Java 2 (1998)");
            case 8, 11, 17, 21, 25 -> System.out.println("Era: Modern LTS Release JDK " + release);
            default -> System.out.println("Era: Feature Release");
        }
    }
}""",
                    programExplanation = "Showcases how a single-line `record Developer(String name, int jdkVersion) {}` in JDK 16–25 replaces a verbose boilerplate class and works seamlessly with arrow switch statements.",
                    sampleOutput = "Record toString : Developer[name=Duke, jdkVersion=25]\nAccessor name() : Duke\nEra: Modern LTS Release JDK 25",
                    commonErrors = listOf(
                        "Attempting to mutate a record field (record fields are implicitly private final).",
                        "Compiling a 'record' or 'case ->' switch with JDK 8 or JDK 11 selected."
                    ),
                    practiceQuestions = listOf(
                        "Add a third component 'String vendor' to the Developer record and print it in main()."
                    ),
                    relatedApiDocs = listOf("java.lang.Record", "java.lang.Thread", "java.util.SequencedCollection")
                )
            )
        )
    )

    val allLessons: List<TutorialLesson>
        get() = chapters.flatMap { it.lessons }
}
