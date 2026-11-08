# Universal Java Studio

**All-Version Offline Java IDE, Compiler, Execution Environment, and Learning Platform (JDK 1.0 through JDK 25+)**

---

## 1. Overview
**Universal Java Studio** is an offline Java programming IDE, version compatibility analyzer, compiler/runtime environment, and educational laboratory supporting historical Java releases (**JDK 1.0, JDK 1.1, JDK 1.2 [Java 2], JDK 1.3, JDK 1.4**), intermediate releases (**JDK 5–8**), modular releases (**JDK 9–17**), and modern releases (**JDK 18–25+**), with an extensible registry for future JDK versions.

## 2. Core Modules
1. **Home Dashboard**: Displays active JDK, installed JDK count (26+ releases), learning progress, quiz performance, multi-version projects, and recent compilation/execution logs.
2. **Universal JDK Manager**: Manages JDK 1.0 through JDK 25+, probes host JVM properties, allows adding custom/future JDK installations, and inspects the **5-Layer Compatibility Matrix** (Language, API, Bytecode `CAFEBABE` major.minor, Actual Compiler `javac`, and Actual Runtime `java`).
3. **Java Code Editor**: Multi-tab editor with Java syntax highlighting, line numbers, Undo/Redo, Find & Replace, code snippets, and real-time JDK version compatibility checking.
4. **Java Compiler Engine**: Validates syntax, enforces version-specific grammar and API boundaries (e.g., rejecting JDK 5 Generics/Annotations or JDK 8 Lambdas when JDK 1.2 is selected), builds authentic `-source`/`-target`/`-bootclasspath` vs `--release` commands, and generates `.class` bytecode headers (`45.3` to `69.0`).
5. **Java Execution Engine**: Executes single-class, multi-class, and package-based Java programs, supports `System.in` / `Scanner` standard input, command-line arguments, OOP inheritance/polymorphism, records, streams, AWT/Swing/Applet component inspection, runtime exception stack traces, and execution timeouts.
6. **Offline Java Tutorial Platform**: 11 core chapters based on the IIT Kharagpur Java course and Oracle Java Tutorials, plus Core API modules and Modern Java lessons. Every lesson includes all 12 required sections and side-by-side **Java 2 (JDK 1.2)** and **Modern Java** runnable examples.
7. **Offline Java API Documentation Browser**: Version-filtered Javadoc browser from JDK 1.0 to JDK 25 that never displays modern APIs as available when a historical JDK (such as Java 2) is selected.
8. **Java Version Explorer**: Interactive side-by-side comparison across any two Java versions (including preset pairs `1.2 vs 1.4`, `1.4 vs 5`, `5 vs 8`, `8 vs 11`, `11 vs 17`, `17 vs 21`, `21 vs 25`).
9. **Java Practice Laboratory**: 25 categorized programming exercises with problem statements, algorithms, editable code buffers, expected output, and direct Compile & Run integration.
10. **Quiz, Project Manager & SQLite Database**: Room/SQLite persistence for multi-JDK projects (each project remembers its own assigned JDK), completed lessons, bookmarks, quiz scores, offline resources, and the 35-case verification test suite.

## 3. Building & Running Tests
- **Compile APK**: Run `gradle assembleDebug`
- **Execute Unit & Robolectric Verification Suite (35 Test Cases)**: Run `gradle :app:testDebugUnitTest`
