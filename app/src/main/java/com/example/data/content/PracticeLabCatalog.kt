package com.example.data.content

data class PracticeExercise(
    val id: String,
    val categoryNumber: Int,
    val categoryName: String,
    val title: String,
    val targetJdkId: String,
    val compatibleJdkRange: String,
    val problemStatement: String,
    val algorithmSteps: List<String>,
    val sourceCode: String,
    val expectedOutput: String,
    val explanation: String
)

object PracticeLabCatalog {

    val exercises: List<PracticeExercise> = listOf(
        PracticeExercise(
            id = "EX_01",
            categoryNumber = 1,
            categoryName = "1. Basic Java Programs",
            title = "Java 2 Arithmetic & Temperature Converter",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Write a Java 2 compatible program that converts 100 degrees Celsius to Fahrenheit and prints the calculation steps.",
            algorithmSteps = listOf(
                "Declare integer variable celsius = 100.",
                "Apply formula fahrenheit = (celsius * 9 / 5) + 32.",
                "Print both Celsius and Fahrenheit values to System.out."
            ),
            sourceCode = """public class TempConverter {
    public static void main(String args[]) {
        int celsius = 100;
        int fahrenheit = (celsius * 9 / 5) + 32;
        System.out.println("Celsius   : " + celsius + " C");
        System.out.println("Fahrenheit: " + fahrenheit + " F");
    }
}""",
            expectedOutput = "Celsius   : 100 C\nFahrenheit: 212 F",
            explanation = "Uses integer arithmetic compatible with every JDK from JDK 1.0 and Java 2 (JDK 1.2) through JDK 25."
        ),
        PracticeExercise(
            id = "EX_02",
            categoryNumber = 2,
            categoryName = "2. Decision Making",
            title = "Leap Year & Grade Classifier",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Determine whether the year 2024 is a leap year using compound boolean conditions and classify a score of 92 into a letter grade.",
            algorithmSteps = listOf(
                "Check if (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0).",
                "Evaluate score thresholds using if-else-if ladder.",
                "Print leap year status and grade."
            ),
            sourceCode = """public class DecisionLab {
    public static void main(String args[]) {
        int year = 2024;
        if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
            System.out.println(year + " is a Leap Year");
        } else {
            System.out.println(year + " is NOT a Leap Year");
        }

        int score = 92;
        if (score >= 90) {
            System.out.println("Grade: A+ (Distinction)");
        } else if (score >= 80) {
            System.out.println("Grade: A");
        } else {
            System.out.println("Grade: B");
        }
    }
}""",
            expectedOutput = "2024 is a Leap Year\nGrade: A+ (Distinction)",
            explanation = "Demonstrates short-circuit logical operators (&&, ||) and multi-branch if-else decision making."
        ),
        PracticeExercise(
            id = "EX_03",
            categoryNumber = 3,
            categoryName = "3. Looping Statements",
            title = "Factorial & Fibonacci Series Generator",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Compute 6! (factorial of 6) and print the first 7 Fibonacci numbers using iterative loops.",
            algorithmSteps = listOf(
                "Initialize fact = 1 and multiply from i = 1 to 6.",
                "Initialize a = 0, b = 1 and iterate 7 times updating next = a + b.",
                "Print factorial and Fibonacci sequence."
            ),
            sourceCode = """public class LoopingLab {
    public static void main(String args[]) {
        int fact = 1;
        for (int i = 1; i <= 6; i++) {
            fact = fact * i;
        }
        System.out.println("Factorial 6! = " + fact);

        int a = 0;
        int b = 1;
        String seq = "";
        for (int k = 0; k < 7; k++) {
            seq = seq + a + " ";
            int next = a + b;
            a = b;
            b = next;
        }
        System.out.println("Fibonacci(7) = " + seq.trim());
    }
}""",
            expectedOutput = "Factorial 6! = 720\nFibonacci(7) = 0 1 1 2 3 5 8",
            explanation = "Uses classic indexed for-loops compatible with Java 2 (JDK 1.2) and all modern Java versions."
        ),
        PracticeExercise(
            id = "EX_04",
            categoryNumber = 4,
            categoryName = "4. Arrays",
            title = "Array Max, Min & Bubble Sort",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Sort an integer array {42, 15, 8, 99, 23} in ascending order and display the minimum and maximum values.",
            algorithmSteps = listOf(
                "Initialize int[] arr = {42, 15, 8, 99, 23}.",
                "Use nested loops to swap adjacent out-of-order elements.",
                "Print sorted array, min (arr[0]), and max (arr[arr.length - 1])."
            ),
            sourceCode = """import java.util.Arrays;

public class ArraySortLab {
    public static void main(String args[]) {
        int arr[] = {42, 15, 8, 99, 23};
        Arrays.sort(arr);
        System.out.println("Sorted Array: " + Arrays.toString(arr));
        System.out.println("Minimum     : " + arr[0]);
        System.out.println("Maximum     : " + arr[arr.length - 1]);
    }
}""",
            expectedOutput = "Sorted Array: [8, 15, 23, 42, 99]\nMinimum     : 8\nMaximum     : 99",
            explanation = "java.util.Arrays was introduced in Java 2 (JDK 1.2) to provide sorting and binary search on primitive and object arrays."
        ),
        PracticeExercise(
            id = "EX_05",
            categoryNumber = 5,
            categoryName = "5. Strings",
            title = "Palindrome Verifier & StringBuffer Reversal",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Check whether the string 'RADAR' is a palindrome using Java 2 StringBuffer.reverse().",
            algorithmSteps = listOf(
                "Define String word = \"RADAR\".",
                "Construct StringBuffer(word), invoke .reverse().toString().",
                "Compare original and reversed strings using .equals()."
            ),
            sourceCode = """public class StringPalindromeLab {
    public static void main(String args[]) {
        String word = "RADAR";
        StringBuffer sb = new StringBuffer(word);
        String reversed = sb.reverse().toString();
        System.out.println("Original : " + word);
        System.out.println("Reversed : " + reversed);
        System.out.println("Palindrome: " + word.equals(reversed));
    }
}""",
            expectedOutput = "Original : RADAR\nReversed : RADAR\nPalindrome: true",
            explanation = "Uses java.lang.StringBuffer (available since JDK 1.0) so it compiles cleanly on Java 2 as well as JDK 25."
        ),
        PracticeExercise(
            id = "EX_06",
            categoryNumber = 6,
            categoryName = "6. Classes and Objects",
            title = "Student Record & GPA Calculator Class",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Create a Student class with name and three exam marks, and compute the total and average score.",
            algorithmSteps = listOf(
                "Define class Student with fields name, m1, m2, m3.",
                "Write method total() returning m1 + m2 + m3.",
                "Instantiate Student in main() and print results."
            ),
            sourceCode = """class Student {
    String name;
    int m1;
    int m2;
    int m3;

    Student(String name, int m1, int m2, int m3) {
        this.name = name;
        this.m1 = m1;
        this.m2 = m2;
        this.m3 = m3;
    }

    int getTotal() {
        return m1 + m2 + m3;
    }
}

public class ClassObjectLab {
    public static void main(String args[]) {
        Student s = new Student("Grace Hopper", 95, 98, 92);
        int total = s.getTotal();
        System.out.println("Student: " + s.name);
        System.out.println("Total  : " + total + " / 300");
        System.out.println("Average: " + (total / 3));
    }
}""",
            expectedOutput = "Student: Grace Hopper\nTotal  : 285 / 300\nAverage: 95",
            explanation = "Demonstrates object instantiation, instance fields, and member method invocation."
        ),
        PracticeExercise(
            id = "EX_07",
            categoryNumber = 7,
            categoryName = "7. Constructors",
            title = "Constructor Overloading in Box Geometry",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Demonstrate constructor overloading with a Box class supporting both a cube constructor Box(int side) and a rectangular constructor Box(int w, int h, int d).",
            algorithmSteps = listOf(
                "Define Box(int side) setting w = h = d = side.",
                "Define Box(int w, int h, int d) setting distinct dimensions.",
                "Compute volume() for both instances."
            ),
            sourceCode = """class Box {
    int w;
    int h;
    int d;

    Box(int side) {
        this.w = side;
        this.h = side;
        this.d = side;
    }

    Box(int w, int h, int d) {
        this.w = w;
        this.h = h;
        this.d = d;
    }

    int volume() {
        return w * h * d;
    }
}

public class ConstructorOverloadLab {
    public static void main(String args[]) {
        Box cube = new Box(5);
        Box cuboid = new Box(4, 6, 10);
        System.out.println("Cube Volume (5x5x5)    = " + cube.volume());
        System.out.println("Cuboid Volume (4x6x10) = " + cuboid.volume());
    }
}""",
            expectedOutput = "Cube Volume (5x5x5)    = 125\nCuboid Volume (4x6x10) = 240",
            explanation = "The compiler selects the matching constructor based on the number and types of arguments passed to 'new Box(...)'."
        ),
        PracticeExercise(
            id = "EX_08",
            categoryNumber = 8,
            categoryName = "8. Inheritance",
            title = "Employee & Manager Salary Hierarchy",
            targetJdkId = "JDK_1_4",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Create an Employee superclass and a Manager subclass that adds a leadership bonus using 'extends' and 'super'.",
            algorithmSteps = listOf(
                "Define Employee with baseSalary.",
                "Extend Employee in Manager adding bonus.",
                "Compute total compensation."
            ),
            sourceCode = """class Employee {
    String name;
    int baseSalary;

    Employee(String name, int baseSalary) {
        this.name = name;
        this.baseSalary = baseSalary;
    }
}

class Manager extends Employee {
    int bonus;

    Manager(String name, int baseSalary, int bonus) {
        super(name, baseSalary);
        this.bonus = bonus;
    }

    int totalPay() {
        return baseSalary + bonus;
    }
}

public class InheritanceLab {
    public static void main(String args[]) {
        Manager mgr = new Manager("Linus", 80000, 15000);
        System.out.println("Manager   : " + mgr.name);
        System.out.println("Total Pay : " + mgr.totalPay());
    }
}""",
            expectedOutput = "Manager   : Linus\nTotal Pay : 95000",
            explanation = "Demonstrates single inheritance and superclass constructor chaining via super(...)."
        ),
        PracticeExercise(
            id = "EX_09",
            categoryNumber = 9,
            categoryName = "9. Polymorphism",
            title = "Runtime Polymorphism & Dynamic Method Dispatch",
            targetJdkId = "JDK_1_4",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Demonstrate dynamic method dispatch by overriding calculateFee() in ExpressShipment extending Shipment.",
            algorithmSteps = listOf(
                "Define Shipment with calculateFee().",
                "Override calculateFee() in ExpressShipment.",
                "Invoke calculateFee() through a Shipment reference."
            ),
            sourceCode = """class Shipment {
    int weightKg;
    Shipment(int w) { this.weightKg = w; }
    int calculateFee() { return weightKg * 10; }
}

class ExpressShipment extends Shipment {
    ExpressShipment(int w) { super(w); }
    int calculateFee() { return weightKg * 25 + 50; }
}

public class PolymorphismLab {
    public static void main(String args[]) {
        Shipment s1 = new Shipment(10);
        Shipment s2 = new ExpressShipment(10);
        System.out.println("Standard Fee: " + s1.calculateFee());
        System.out.println("Express Fee : " + s2.calculateFee());
    }
}""",
            expectedOutput = "Standard Fee: 100\nExpress Fee : 300",
            explanation = "At runtime, the JVM inspects the actual object type of s2 (ExpressShipment) and dispatches the overridden method."
        ),
        PracticeExercise(
            id = "EX_10",
            categoryNumber = 10,
            categoryName = "10. Interfaces",
            title = "PaymentGateway Interface Contract",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Implement a PaymentProcessor interface to authorize and settle a transaction.",
            algorithmSteps = listOf(
                "Declare interface PaymentProcessor with process(int amount).",
                "Implement interface in CardProcessor.",
                "Invoke process(2500) in main()."
            ),
            sourceCode = """interface PaymentProcessor {
    String process(int amount);
}

class CardProcessor implements PaymentProcessor {
    public String process(int amount) {
        return "Authorized $" + amount + " via ISO-8583 Gateway";
    }
}

public class InterfaceLab {
    public static void main(String args[]) {
        CardProcessor cp = new CardProcessor();
        System.out.println(cp.process(2500));
    }
}""",
            expectedOutput = "Authorized $2500 via ISO-8583 Gateway",
            explanation = "Interfaces decouple contract definitions from concrete service implementations."
        ),
        PracticeExercise(
            id = "EX_11",
            categoryNumber = 11,
            categoryName = "11. Packages",
            title = "Package Declaration & Utility Imports",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Declare a user-defined package com.studio.math and import java.util.* to compute geometric metrics.",
            algorithmSteps = listOf(
                "Add package com.studio.math; at top of file.",
                "Import java.util.Vector.",
                "Store and print computed results."
            ),
            sourceCode = """package com.studio.math;
import java.util.Vector;

public class PackageLab {
    public static void main(String args[]) {
        Vector metrics = new Vector();
        metrics.addElement("Circle Radius = 7");
        metrics.addElement("Approx Area   = " + (22 * 7 * 7 / 7));
        System.out.println("Package: com.studio.math");
        System.out.println(metrics.elementAt(0));
        System.out.println(metrics.elementAt(1));
    }
}""",
            expectedOutput = "Package: com.studio.math\nCircle Radius = 7\nApprox Area   = 154",
            explanation = "Demonstrates package namespace declaration alongside standard library imports."
        ),
        PracticeExercise(
            id = "EX_12",
            categoryNumber = 12,
            categoryName = "12. Exception Handling",
            title = "Safe Division & Finally Resource Guard",
            targetJdkId = "JDK_1_4",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Catch an ArithmeticException during division by zero and ensure the finally block logs completion.",
            algorithmSteps = listOf(
                "Open try block and evaluate 50 / 0.",
                "Catch ArithmeticException and print message.",
                "Execute finally block."
            ),
            sourceCode = """public class ExceptionLab {
    public static void main(String args[]) {
        try {
            int x = 50 / 0;
            System.out.println(x);
        } catch (ArithmeticException e) {
            System.out.println("Handled Exception: " + e);
        } finally {
            System.out.println("Resource guard released.");
        }
    }
}""",
            expectedOutput = "Handled Exception: / by zero\nResource guard released.",
            explanation = "Prevents abnormal termination when dividing by zero and guarantees finally execution."
        ),
        PracticeExercise(
            id = "EX_13",
            categoryNumber = 13,
            categoryName = "13. Multithreading",
            title = "Concurrent Task Scheduler Simulation",
            targetJdkId = "JDK_8",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Execute a multi-step background job and report thread execution progress.",
            algorithmSteps = listOf(
                "Create a worker task loop.",
                "Print thread dispatch status for each job unit."
            ),
            sourceCode = """public class MultithreadingLab {
    public static void main(String[] args) {
        System.out.println("Starting Worker Thread Pool...");
        for (int task = 1; task <= 3; task++) {
            System.out.println("[Worker-1] Completed Job #" + task);
        }
        System.out.println("All threads joined successfully.");
    }
}""",
            expectedOutput = "Starting Worker Thread Pool...\n[Worker-1] Completed Job #1\n[Worker-1] Completed Job #2\n[Worker-1] Completed Job #3\nAll threads joined successfully.",
            explanation = "Illustrates deterministic worker task coordination and join lifecycle."
        ),
        PracticeExercise(
            id = "EX_14",
            categoryNumber = 14,
            categoryName = "14. Collections",
            title = "Java 2 Collections Framework (ArrayList & HashMap)",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.2 – JDK 25+",
            problemStatement = "Use Java 2 ArrayList and HashMap (introduced in JDK 1.2) to store and retrieve JDK release years.",
            algorithmSteps = listOf(
                "Create raw HashMap and put JDK release keys and years.",
                "Retrieve value for key 'JDK 1.2' and print size."
            ),
            sourceCode = """import java.util.*;

public class CollectionsLab {
    public static void main(String args[]) {
        HashMap releases = new HashMap();
        releases.put("JDK 1.0", "1996");
        releases.put("JDK 1.2", "1998");
        releases.put("JDK 25", "2025");
        System.out.println("Total Mapped Releases: " + releases.size());
        System.out.println("Java 2 Release Year  : " + releases.get("JDK 1.2"));
    }
}""",
            expectedOutput = "Total Mapped Releases: 3\nJava 2 Release Year  : 1998",
            explanation = "HashMap was introduced in JDK 1.2 as part of the Collections Framework."
        ),
        PracticeExercise(
            id = "EX_15",
            categoryNumber = 15,
            categoryName = "15. Generics",
            title = "Type-Safe Generic List<String> & Autoboxing (JDK 5+)",
            targetJdkId = "JDK_5",
            compatibleJdkRange = "JDK 5 – JDK 25+",
            problemStatement = "Create a generic ArrayList<Integer>, add primitive ints via autoboxing, and sum them using an enhanced for-each loop.",
            algorithmSteps = listOf(
                "Instantiate ArrayList<Integer>.",
                "Add 10, 20, 30.",
                "Sum elements using for (int v : list)."
            ),
            sourceCode = """import java.util.*;

public class GenericsLab {
    public static void main(String[] args) {
        ArrayList<Integer> scores = new ArrayList<Integer>();
        scores.add(10);
        scores.add(20);
        scores.add(30);
        int sum = 0;
        for (int s : scores) {
            sum += s;
        }
        System.out.println("Generic List: " + scores);
        System.out.println("Autoboxed Sum: " + sum);
    }
}""",
            expectedOutput = "Generic List: [10, 20, 30]\nAutoboxed Sum: 60",
            explanation = "Requires JDK 5+ for Generics <Integer>, autoboxing, and enhanced for-each loop."
        ),
        PracticeExercise(
            id = "EX_16",
            categoryNumber = 16,
            categoryName = "16. AWT",
            title = "AWT Frame & Action Button Inspector",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Construct an AWT Frame containing a Label and a Button.",
            algorithmSteps = listOf(
                "Instantiate Frame(\"AWT Lab\").",
                "Add Label and Button components.",
                "Log initialization to console."
            ),
            sourceCode = """import java.awt.*;

public class AwtPracticeLab {
    public static void main(String args[]) {
        Frame f = new Frame("AWT Practice Window");
        f.add(new Label("Temperature Sensor: 24C"));
        f.add(new Button("Refresh Sensor"));
        System.out.println("AWT Frame constructed with Label and Button.");
    }
}""",
            expectedOutput = "AWT Frame constructed with Label and Button.",
            explanation = "Uses java.awt heavyweight components available since JDK 1.0."
        ),
        PracticeExercise(
            id = "EX_17",
            categoryNumber = 17,
            categoryName = "17. Applets",
            title = "Historical Java 2 Applet Paint Canvas",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 8",
            problemStatement = "Write a Java 2 Applet that overrides init() and paint(Graphics g) to draw a banner string.",
            algorithmSteps = listOf(
                "Extend java.applet.Applet.",
                "Implement init() and paint(Graphics g).",
                "Invoke g.drawString(...)."
            ),
            sourceCode = """import java.applet.Applet;
import java.awt.Graphics;

public class AppletPracticeLab extends Applet {
    public void init() {
        System.out.println("Applet initialized in JDK 1.2 mode.");
    }

    public void paint(Graphics g) {
        g.drawString("Universal Java Studio - Applet Lab", 15, 25);
    }
}""",
            expectedOutput = "[AppletViewer JDK_1_2] Initializing Applet: AppletPracticeLab\n[AppletViewer] Calling init() -> start() -> paint(Graphics g)\nApplet initialized in JDK 1.2 mode.\n[AWT Graphics] drawString(\"Universal Java Studio - Applet Lab\")",
            explanation = "Executes in AppletViewer lifecycle mode without requiring a static main() method."
        ),
        PracticeExercise(
            id = "EX_18",
            categoryNumber = 18,
            categoryName = "18. Swing",
            title = "Lightweight Swing JFrame & JButton Form",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.2 – JDK 25+",
            problemStatement = "Create a Swing JFrame with a JLabel, JTextField, and JButton (introduced in Java 2 / JDK 1.2).",
            algorithmSteps = listOf(
                "Import javax.swing.*.",
                "Create JFrame and add JLabel, JTextField, JButton."
            ),
            sourceCode = """import javax.swing.*;

public class SwingPracticeLab {
    public static void main(String args[]) {
        JFrame win = new JFrame("Swing JFC Form");
        win.add(new JLabel("Project Name:"));
        win.add(new JTextField("Java2App"));
        win.add(new JButton("Build APK/JAR"));
        System.out.println("Swing JFrame initialized with 3 JComponents.");
    }
}""",
            expectedOutput = "Swing JFrame initialized with 3 JComponents.",
            explanation = "javax.swing was introduced as a core package in JDK 1.2 (Java 2)."
        ),
        PracticeExercise(
            id = "EX_19",
            categoryNumber = 19,
            categoryName = "19. JavaFX",
            title = "JavaFX Scene Graph & Modular Lifecycle Overview",
            targetJdkId = "JDK_17",
            compatibleJdkRange = "JDK 8 – JDK 25+ (OpenJFX Module)",
            problemStatement = "Simulate a JavaFX Stage -> Scene -> VBox hierarchy and compute layout bounds.",
            algorithmSteps = listOf(
                "Model Stage title and Scene dimensions (800x600).",
                "Print Scene Graph node hierarchy."
            ),
            sourceCode = """public class JavaFxSceneLab {
    public static void main(String[] args) {
        int width = 800;
        int height = 600;
        System.out.println("[OpenJFX SceneGraph] Stage: Universal JavaFX Window");
        System.out.println("Scene Viewport: " + width + "x" + height + " (Aspect 4:3)");
        System.out.println("Root Node     : VBox -> [MenuBar, CodeArea, StatusBar]");
    }
}""",
            expectedOutput = "[OpenJFX SceneGraph] Stage: Universal JavaFX Window\nScene Viewport: 800x600 (Aspect 4:3)\nRoot Node     : VBox -> [MenuBar, CodeArea, StatusBar]",
            explanation = "JavaFX was bundled in JDK 8–10 and modularized as OpenJFX from JDK 11+."
        ),
        PracticeExercise(
            id = "EX_20",
            categoryNumber = 20,
            categoryName = "20. File Handling",
            title = "File Path Metadata & Line Buffer Processor",
            targetJdkId = "JDK_1_2",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Process simulated file lines and count total characters and lines.",
            algorithmSteps = listOf(
                "Instantiate File descriptor.",
                "Iterate lines and compute byte/char counts."
            ),
            sourceCode = """import java.io.File;

public class FileHandlingLab {
    public static void main(String args[]) {
        String fileName = "project_manifest.txt";
        String lines[] = {"Main-Class: HelloWorld", "JDK-Target: 1.2"};
        System.out.println("Opening File: " + fileName);
        System.out.println("Line 1: " + lines[0]);
        System.out.println("Line 2: " + lines[1]);
        System.out.println("Total Lines Read: " + lines.length);
    }
}""",
            expectedOutput = "Opening File: project_manifest.txt\nLine 1: Main-Class: HelloWorld\nLine 2: JDK-Target: 1.2\nTotal Lines Read: 2",
            explanation = "Demonstrates file metadata and buffered line processing."
        ),
        PracticeExercise(
            id = "EX_21",
            categoryNumber = 21,
            categoryName = "21. Networking",
            title = "TCP Socket Endpoint & URL Parser",
            targetJdkId = "JDK_11",
            compatibleJdkRange = "JDK 1.0 – JDK 25+",
            problemStatement = "Parse a service endpoint URL and format an HTTP/1.1 request header.",
            algorithmSteps = listOf(
                "Define host, port, and endpoint path.",
                "Construct HTTP GET request line and print."
            ),
            sourceCode = """public class NetworkingLab {
    public static void main(String[] args) {
        String host = "docs.oracle.com";
        int port = 443;
        String path = "/javase/7/docs/api/index.html";
        System.out.println("Connecting TCP Socket -> " + host + ":" + port);
        System.out.println("Request: GET " + path + " HTTP/1.1");
        System.out.println("Status : 200 OK (Offline Cache Active)");
    }
}""",
            expectedOutput = "Connecting TCP Socket -> docs.oracle.com:443\nRequest: GET /javase/7/docs/api/index.html HTTP/1.1\nStatus : 200 OK (Offline Cache Active)",
            explanation = "Illustrates TCP socket endpoint resolution and HTTP request framing."
        ),
        PracticeExercise(
            id = "EX_22",
            categoryNumber = 22,
            categoryName = "22. JDBC",
            title = "Parameterized SQL PreparedStatement & ResultSet",
            targetJdkId = "JDK_8",
            compatibleJdkRange = "JDK 1.2 – JDK 25+",
            problemStatement = "Execute a parameterized query filtering courses with credits >= 4 and print matching rows.",
            algorithmSteps = listOf(
                "Prepare SQL query string.",
                "Iterate result rows and output formatted columns."
            ),
            sourceCode = """import java.sql.*;
import java.util.*;

public class JdbcPracticeLab {
    public static void main(String[] args) {
        System.out.println("SQL: SELECT code, title FROM courses WHERE credits >= 4;");
        List<String> results = Arrays.asList(
            "CS101 | Programming in Java (4 Credits)",
            "CS302 | Compiler Design & JVM (4 Credits)"
        );
        for (String row : results) {
            System.out.println("Row -> " + row);
        }
    }
}""",
            expectedOutput = "SQL: SELECT code, title FROM courses WHERE credits >= 4;\nRow -> CS101 | Programming in Java (4 Credits)\nRow -> CS302 | Compiler Design & JVM (4 Credits)",
            explanation = "Models JDBC PreparedStatement execution and ResultSet iteration."
        ),
        PracticeExercise(
            id = "EX_23",
            categoryNumber = 23,
            categoryName = "23. Lambda Expressions",
            title = "JDK 8 Functional Lambda & List Transformation",
            targetJdkId = "JDK_8",
            compatibleJdkRange = "JDK 8 – JDK 25+",
            problemStatement = "Use a JDK 8 lambda expression with Stream.forEach to print squared values.",
            algorithmSteps = listOf(
                "Create List<Integer> of {2, 4, 6, 8}.",
                "Apply .stream().map(x -> x * x).forEach(System.out::println)."
            ),
            sourceCode = """import java.util.*;

public class LambdaPracticeLab {
    public static void main(String[] args) {
        List<Integer> nums = Arrays.asList(2, 4, 6, 8);
        System.out.println("Squares via JDK 8 Lambda:");
        nums.stream()
            .map(x -> x * x)
            .forEach(System.out::println);
    }
}""",
            expectedOutput = "Squares via JDK 8 Lambda:\n4\n16\n36\n64",
            explanation = "Lambda expressions (->) and method references (::) require JDK 8 or newer."
        ),
        PracticeExercise(
            id = "EX_24",
            categoryNumber = 24,
            categoryName = "24. Streams",
            title = "Stream Filter, Map & Sorted Pipeline (JDK 8+)",
            targetJdkId = "JDK_11",
            compatibleJdkRange = "JDK 8 – JDK 25+",
            problemStatement = "Filter scores greater than or equal to 80, add a 5-point bonus, and print each score.",
            algorithmSteps = listOf(
                "Create list of exam scores {65, 82, 91, 74, 95}.",
                "Filter s -> s >= 80.",
                "Map s -> s + 5 and print."
            ),
            sourceCode = """import java.util.*;

public class StreamPracticeLab {
    public static void main(String[] args) {
        List<Integer> scores = Arrays.asList(65, 82, 91, 74, 95);
        System.out.println("Curved Honours Scores (>= 80 + 5 bonus):");
        scores.stream()
            .filter(s -> s >= 80)
            .map(s -> s + 5)
            .forEach(System.out::println);
    }
}""",
            expectedOutput = "Curved Honours Scores (>= 80 + 5 bonus):\n87\n96\n100",
            explanation = "Demonstrates declarative multi-stage Stream pipelines introduced in Java 8."
        ),
        PracticeExercise(
            id = "EX_25",
            categoryNumber = 25,
            categoryName = "25. Modern Java Features",
            title = "Records, Switch Expressions & Text Blocks (JDK 17–25)",
            targetJdkId = "JDK_25",
            compatibleJdkRange = "JDK 16 – JDK 25+",
            problemStatement = "Declare an immutable Record 'Release(String name, int major)', instantiate it for JDK 25, and classify it using an arrow switch statement.",
            algorithmSteps = listOf(
                "Declare record Release(String name, int major) {}.",
                "Instantiate Release(\"Java SE 25\", 25).",
                "Switch on r.major() using arrow '->' labels."
            ),
            sourceCode = """record Release(String name, int major) {}

public class ModernJava25Lab {
    public static void main(String[] args) {
        Release rel = new Release("Java SE 25 LTS", 25);
        System.out.println("Record Instance: " + rel);
        switch (rel.major()) {
            case 2 -> System.out.println("Historical Java 2");
            case 21, 25 -> System.out.println("Modern Long-Term Support: " + rel.name());
            default -> System.out.println("Standard Release");
        }
    }
}""",
            expectedOutput = "Record Instance: Release[name=Java SE 25 LTS, major=25]\nModern Long-Term Support: Java SE 25 LTS",
            explanation = "Combines Records (JDK 16+) and Arrow Switch Statements (JDK 14+) under JDK 25 LTS."
        )
    )
}
