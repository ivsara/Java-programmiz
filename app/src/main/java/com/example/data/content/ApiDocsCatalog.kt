package com.example.data.content

data class ApiDocEntry(
    val id: String,
    val packageName: String,
    val typeName: String,
    val kind: String, // "Class", "Interface", "Record", "Exception"
    val introducedInLabel: String, // e.g. "JDK 1.0", "JDK 1.2", "JDK 5", "JDK 8", "JDK 11", "JDK 21"
    val introducedMajor: Int, // 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 16, 17, 21, 25
    val deprecatedInLabel: String? = null,
    val removedInLabel: String? = null,
    val inheritanceHierarchy: String,
    val summary: String,
    val constructors: List<String>,
    val fields: List<String>,
    val methods: List<Pair<String, Int>>, // Signature to minimum major version of that method!
    val relatedClasses: List<String>
)

object ApiDocsCatalog {

    val versionFilterOptions: List<Pair<String, Int>> = listOf(
        "JDK 1.0 (1996)" to 0,
        "JDK 1.1 (1997)" to 1,
        "JDK 1.2 – Java 2 (1998)" to 2,
        "JDK 1.3 – Java 2 (2000)" to 3,
        "JDK 1.4 – Java 2 (2002)" to 4,
        "Java SE 5 (2004)" to 5,
        "Java SE 6 (2006)" to 6,
        "Java SE 7 (2011)" to 7,
        "Java SE 8 LTS (2014)" to 8,
        "Java SE 9 (2017)" to 9,
        "Java SE 11 LTS (2018)" to 11,
        "Java SE 17 LTS (2021)" to 17,
        "Java SE 21 LTS (2023)" to 21,
        "Java SE 25 LTS (2025)" to 25
    )

    val entries: List<ApiDocEntry> = listOf(
        ApiDocEntry(
            id = "API_OBJECT",
            packageName = "java.lang",
            typeName = "Object",
            kind = "Class",
            introducedInLabel = "JDK 1.0",
            introducedMajor = 0,
            inheritanceHierarchy = "java.lang.Object",
            summary = "Class Object is the root of the class hierarchy. Every class has Object as a superclass. All objects, including arrays, implement the methods of this class.",
            constructors = listOf("public Object()"),
            fields = emptyList(),
            methods = listOf(
                "public boolean equals(Object obj)" to 0,
                "public int hashCode()" to 0,
                "public String toString()" to 0,
                "public final Class getClass()" to 0,
                "public final void wait() throws InterruptedException" to 0,
                "public final void notify()" to 0,
                "public final void notifyAll()" to 0
            ),
            relatedClasses = listOf("java.lang.Class", "java.lang.String")
        ),
        ApiDocEntry(
            id = "API_STRING",
            packageName = "java.lang",
            typeName = "String",
            kind = "Class",
            introducedInLabel = "JDK 1.0",
            introducedMajor = 0,
            inheritanceHierarchy = "java.lang.Object -> java.lang.String (implements Serializable, Comparable, CharSequence)",
            summary = "The String class represents immutable character strings. All string literals in Java programs, such as \"abc\", are implemented as instances of this class.",
            constructors = listOf(
                "public String()",
                "public String(String original)",
                "public String(char[] value)",
                "public String(byte[] bytes)"
            ),
            fields = listOf("public static final Comparator CASE_INSENSITIVE_ORDER (Since JDK 1.2)"),
            methods = listOf(
                "public int length()" to 0,
                "public char charAt(int index)" to 0,
                "public String substring(int beginIndex, int endIndex)" to 0,
                "public String toUpperCase()" to 0,
                "public String toLowerCase()" to 0,
                "public String trim()" to 0,
                "public int compareToIgnoreCase(String str)" to 2,
                "public String[] split(String regex)" to 4,
                "public boolean contains(CharSequence s)" to 5,
                "public boolean isEmpty()" to 6,
                "public static String join(CharSequence delimiter, CharSequence... elements)" to 8,
                "public boolean isBlank()" to 11,
                "public String repeat(int count)" to 11,
                "public Stream<String> lines()" to 11,
                "public String formatted(Object... args)" to 15
            ),
            relatedClasses = listOf("java.lang.StringBuffer", "java.lang.StringBuilder", "java.lang.CharSequence")
        ),
        ApiDocEntry(
            id = "API_STRINGBUFFER",
            packageName = "java.lang",
            typeName = "StringBuffer",
            kind = "Class",
            introducedInLabel = "JDK 1.0",
            introducedMajor = 0,
            inheritanceHierarchy = "java.lang.Object -> java.lang.StringBuffer",
            summary = "A thread-safe, mutable sequence of characters. Primary mutable string builder in JDK 1.0–1.4 before unsynchronized StringBuilder was introduced in JDK 5.",
            constructors = listOf("public StringBuffer()", "public StringBuffer(int capacity)", "public StringBuffer(String str)"),
            fields = emptyList(),
            methods = listOf(
                "public synchronized StringBuffer append(String str)" to 0,
                "public synchronized StringBuffer insert(int offset, String str)" to 0,
                "public synchronized StringBuffer reverse()" to 0,
                "public synchronized StringBuffer delete(int start, int end)" to 2,
                "public int length()" to 0
            ),
            relatedClasses = listOf("java.lang.String", "java.lang.StringBuilder")
        ),
        ApiDocEntry(
            id = "API_VECTOR",
            packageName = "java.util",
            typeName = "Vector",
            kind = "Class",
            introducedInLabel = "JDK 1.0",
            introducedMajor = 0,
            inheritanceHierarchy = "java.lang.Object -> java.util.AbstractCollection -> java.util.AbstractList -> java.util.Vector",
            summary = "The Vector class implements a growable array of objects. Retrofitted into the Collections Framework in Java 2 (JDK 1.2) to implement the List interface.",
            constructors = listOf("public Vector()", "public Vector(int initialCapacity)"),
            fields = listOf("protected Object[] elementData", "protected int elementCount"),
            methods = listOf(
                "public synchronized void addElement(Object obj)" to 0,
                "public synchronized Object elementAt(int index)" to 0,
                "public synchronized int size()" to 0,
                "public Enumeration elements()" to 0,
                "public synchronized boolean add(Object e) [Added in Java 2 / JDK 1.2]" to 2,
                "public synchronized Object get(int index) [Added in Java 2 / JDK 1.2]" to 2
            ),
            relatedClasses = listOf("java.util.ArrayList", "java.util.Enumeration", "java.util.Stack")
        ),
        ApiDocEntry(
            id = "API_ARRAYLIST",
            packageName = "java.util",
            typeName = "ArrayList",
            kind = "Class",
            introducedInLabel = "JDK 1.2 (Java 2)",
            introducedMajor = 2,
            inheritanceHierarchy = "java.lang.Object -> java.util.AbstractCollection -> java.util.AbstractList -> java.util.ArrayList (implements List, RandomAccess, Cloneable, Serializable)",
            summary = "Resizable-array implementation of the List interface introduced in Java 2 (JDK 1.2). Unsynchronized and faster than Vector for single-threaded workloads. Parameterized with Generics <E> in JDK 5.",
            constructors = listOf("public ArrayList()", "public ArrayList(int initialCapacity)", "public ArrayList(Collection c)"),
            fields = emptyList(),
            methods = listOf(
                "public boolean add(Object e)" to 2,
                "public Object get(int index)" to 2,
                "public Object set(int index, Object element)" to 2,
                "public Object remove(int index)" to 2,
                "public int size()" to 2,
                "public void clear()" to 2,
                "public void forEach(Consumer action)" to 8,
                "public boolean removeIf(Predicate filter)" to 8,
                "public Object getFirst() [SequencedCollection]" to 21,
                "public Object getLast() [SequencedCollection]" to 21
            ),
            relatedClasses = listOf("java.util.List", "java.util.LinkedList", "java.util.Vector", "java.util.Collections")
        ),
        ApiDocEntry(
            id = "API_HASHMAP",
            packageName = "java.util",
            typeName = "HashMap",
            kind = "Class",
            introducedInLabel = "JDK 1.2 (Java 2)",
            introducedMajor = 2,
            inheritanceHierarchy = "java.lang.Object -> java.util.AbstractMap -> java.util.HashMap (implements Map, Cloneable, Serializable)",
            summary = "Hash table based implementation of the Map interface introduced in Java 2 (JDK 1.2). Permits null values and the null key, unlike legacy JDK 1.0 Hashtable.",
            constructors = listOf("public HashMap()", "public HashMap(int initialCapacity, float loadFactor)"),
            fields = emptyList(),
            methods = listOf(
                "public Object put(Object key, Object value)" to 2,
                "public Object get(Object key)" to 2,
                "public boolean containsKey(Object key)" to 2,
                "public Set keySet()" to 2,
                "public Collection values()" to 2,
                "public Object getOrDefault(Object key, Object defaultValue)" to 8,
                "public Object computeIfAbsent(Object key, Function mappingFunction)" to 8,
                "public static HashMap newHashMap(int numMappings)" to 19
            ),
            relatedClasses = listOf("java.util.Map", "java.util.TreeMap", "java.util.Hashtable")
        ),
        ApiDocEntry(
            id = "API_AWT_FRAME",
            packageName = "java.awt",
            typeName = "Frame",
            kind = "Class",
            introducedInLabel = "JDK 1.0",
            introducedMajor = 0,
            inheritanceHierarchy = "java.lang.Object -> java.awt.Component -> java.awt.Container -> java.awt.Window -> java.awt.Frame",
            summary = "A Frame is a top-level heavyweight window with a title and a border using native OS window peers.",
            constructors = listOf("public Frame()", "public Frame(String title)"),
            fields = listOf("public static final int NORMAL"),
            methods = listOf(
                "public void setTitle(String title)" to 0,
                "public void setSize(int width, int height)" to 1,
                "public void setVisible(boolean b)" to 1,
                "public Component add(Component comp)" to 0
            ),
            relatedClasses = listOf("javax.swing.JFrame", "java.awt.Button", "java.awt.Label", "java.awt.Panel")
        ),
        ApiDocEntry(
            id = "API_APPLET",
            packageName = "java.applet",
            typeName = "Applet",
            kind = "Class",
            introducedInLabel = "JDK 1.0",
            introducedMajor = 0,
            deprecatedInLabel = "Deprecated in JDK 9 (JEP 289)",
            removedInLabel = "Deprecated for Removal in JDK 17 (JEP 398)",
            inheritanceHierarchy = "java.lang.Object -> java.awt.Component -> java.awt.Container -> java.awt.Panel -> java.applet.Applet",
            summary = "Historical embeddable graphical Java component executed inside a browser or Sun/Oracle appletviewer. Core part of JDK 1.0–8; deprecated in JDK 9.",
            constructors = listOf("public Applet()"),
            fields = emptyList(),
            methods = listOf(
                "public void init()" to 0,
                "public void start()" to 0,
                "public void paint(Graphics g)" to 0,
                "public void stop()" to 0,
                "public void destroy()" to 0,
                "public String getParameter(String name)" to 0
            ),
            relatedClasses = listOf("java.awt.Panel", "java.awt.Graphics", "javax.swing.JApplet")
        ),
        ApiDocEntry(
            id = "API_SWING_JFRAME",
            packageName = "javax.swing",
            typeName = "JFrame",
            kind = "Class",
            introducedInLabel = "JDK 1.2 (Java 2)",
            introducedMajor = 2,
            inheritanceHierarchy = "java.lang.Object -> java.awt.Component -> java.awt.Container -> java.awt.Window -> java.awt.Frame -> javax.swing.JFrame",
            summary = "An extended version of java.awt.Frame that adds support for the JFC/Swing lightweight component architecture introduced in Java 2 (JDK 1.2).",
            constructors = listOf("public JFrame()", "public JFrame(String title)"),
            fields = listOf("public static final int EXIT_ON_CLOSE (Since JDK 1.3)"),
            methods = listOf(
                "public Container getContentPane()" to 2,
                "public void setDefaultCloseOperation(int operation)" to 2,
                "public void setJMenuBar(JMenuBar menubar)" to 2,
                "public Component add(Component comp) [Direct add delegated to contentPane in JDK 5+]" to 5
            ),
            relatedClasses = listOf("javax.swing.JButton", "javax.swing.JLabel", "javax.swing.JPanel", "javax.swing.JTextField")
        ),
        ApiDocEntry(
            id = "API_JDBC_CONNECTION",
            packageName = "java.sql",
            typeName = "Connection",
            kind = "Interface",
            introducedInLabel = "JDK 1.1",
            introducedMajor = 1,
            inheritanceHierarchy = "java.sql.Connection (extends Wrapper, AutoCloseable in JDK 7+)",
            summary = "A connection (session) with a specific relational database. SQL statements are executed and results are returned within the context of a connection.",
            constructors = emptyList(),
            fields = listOf("int TRANSACTION_SERIALIZABLE", "int TRANSACTION_READ_COMMITTED"),
            methods = listOf(
                "Statement createStatement() throws SQLException" to 1,
                "PreparedStatement prepareStatement(String sql) throws SQLException" to 1,
                "void setAutoCommit(boolean autoCommit) throws SQLException" to 1,
                "void commit() throws SQLException" to 1,
                "void rollback() throws SQLException" to 1,
                "Statement createStatement(int resultSetType, int resultSetConcurrency) [JDBC 2.0]" to 2,
                "Savepoint setSavepoint() [JDBC 3.0]" to 4,
                "boolean isValid(int timeout) [JDBC 4.0]" to 6
            ),
            relatedClasses = listOf("java.sql.DriverManager", "java.sql.PreparedStatement", "java.sql.ResultSet")
        ),
        ApiDocEntry(
            id = "API_SCANNER",
            packageName = "java.util",
            typeName = "Scanner",
            kind = "Class",
            introducedInLabel = "JDK 5",
            introducedMajor = 5,
            inheritanceHierarchy = "java.lang.Object -> java.util.Scanner (implements Iterator<String>, Closeable)",
            summary = "A simple text scanner which can parse primitive types and strings using regular expressions. Introduced in Java 5 (NOT available in JDK 1.0–1.4; use BufferedReader in Java 2).",
            constructors = listOf("public Scanner(InputStream source)", "public Scanner(File source)", "public Scanner(String source)"),
            fields = emptyList(),
            methods = listOf(
                "public String next()" to 5,
                "public String nextLine()" to 5,
                "public int nextInt()" to 5,
                "public double nextDouble()" to 5,
                "public boolean hasNext()" to 5,
                "public Stream<MatchResult> findAll(Pattern pattern)" to 9
            ),
            relatedClasses = listOf("java.io.BufferedReader", "java.util.regex.Pattern")
        ),
        ApiDocEntry(
            id = "API_FILES_NIO2",
            packageName = "java.nio.file",
            typeName = "Files",
            kind = "Class",
            introducedInLabel = "JDK 7",
            introducedMajor = 7,
            inheritanceHierarchy = "java.lang.Object -> java.nio.file.Files",
            summary = "Consists exclusively of static methods that operate on files, directories, or other types of files using NIO.2 Path objects. Introduced in Java 7.",
            constructors = emptyList(),
            fields = emptyList(),
            methods = listOf(
                "public static byte[] readAllBytes(Path path) throws IOException" to 7,
                "public static List<String> readAllLines(Path path) throws IOException" to 7,
                "public static Stream<String> lines(Path path) throws IOException" to 8,
                "public static String readString(Path path) throws IOException" to 11,
                "public static Path writeString(Path path, CharSequence csq) throws IOException" to 11
            ),
            relatedClasses = listOf("java.nio.file.Path", "java.nio.file.Paths", "java.io.File")
        ),
        ApiDocEntry(
            id = "API_STREAM",
            packageName = "java.util.stream",
            typeName = "Stream<T>",
            kind = "Interface",
            introducedInLabel = "JDK 8",
            introducedMajor = 8,
            inheritanceHierarchy = "java.util.stream.BaseStream -> java.util.stream.Stream<T>",
            summary = "A sequence of elements supporting sequential and parallel aggregate operations (filter, map, reduce, collect). Introduced in Java SE 8.",
            constructors = emptyList(),
            fields = emptyList(),
            methods = listOf(
                "Stream<T> filter(Predicate<? super T> predicate)" to 8,
                "<R> Stream<R> map(Function<? super T, ? extends R> mapper)" to 8,
                "void forEach(Consumer<? super T> action)" to 8,
                "<R, A> R collect(Collector<? super T, A, R> collector)" to 8,
                "Stream<T> takeWhile(Predicate<? super T> predicate)" to 9,
                "default List<T> toList()" to 16,
                "<R> Stream<R> gather(Gatherer<? super T, ?, R> gatherer)" to 24
            ),
            relatedClasses = listOf("java.util.stream.Collectors", "java.util.Optional", "java.util.Collection")
        ),
        ApiDocEntry(
            id = "API_HTTPCLIENT",
            packageName = "java.net.http",
            typeName = "HttpClient",
            kind = "Class",
            introducedInLabel = "JDK 11",
            introducedMajor = 11,
            inheritanceHierarchy = "java.lang.Object -> java.net.http.HttpClient (implements AutoCloseable in JDK 21+)",
            summary = "An HTTP Client for sending synchronous and asynchronous HTTP/1.1 and HTTP/2 requests. Standardized in JDK 11.",
            constructors = emptyList(),
            fields = emptyList(),
            methods = listOf(
                "public static HttpClient newHttpClient()" to 11,
                "public <T> HttpResponse<T> send(HttpRequest req, BodyHandler<T> handler)" to 11,
                "public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest req, BodyHandler<T> handler)" to 11,
                "public void close()" to 21
            ),
            relatedClasses = listOf("java.net.http.HttpRequest", "java.net.http.HttpResponse", "java.net.URL")
        ),
        ApiDocEntry(
            id = "API_THREAD_LOOM",
            packageName = "java.lang",
            typeName = "Thread",
            kind = "Class",
            introducedInLabel = "JDK 1.0 (Virtual Threads in JDK 21)",
            introducedMajor = 0,
            deprecatedInLabel = "stop()/suspend()/resume() deprecated in JDK 1.2",
            inheritanceHierarchy = "java.lang.Object -> java.lang.Thread (implements Runnable)",
            summary = "A thread of execution in a program. Supports OS platform threads since JDK 1.0 and lightweight Project Loom Virtual Threads since JDK 21 LTS.",
            constructors = listOf("public Thread()", "public Thread(Runnable target)", "public Thread(String name)"),
            fields = listOf("public static final int MIN_PRIORITY = 1", "public static final int NORM_PRIORITY = 5", "public static final int MAX_PRIORITY = 10"),
            methods = listOf(
                "public void start()" to 0,
                "public void run()" to 0,
                "public final void join() throws InterruptedException" to 0,
                "public static void sleep(long millis) throws InterruptedException" to 0,
                "public static Thread.Builder.OfVirtual ofVirtual() [Project Loom]" to 21,
                "public static Thread startVirtualThread(Runnable task) [Project Loom]" to 21,
                "public final boolean isVirtual()" to 21
            ),
            relatedClasses = listOf("java.lang.Runnable", "java.util.concurrent.Executors")
        )
    )
}
