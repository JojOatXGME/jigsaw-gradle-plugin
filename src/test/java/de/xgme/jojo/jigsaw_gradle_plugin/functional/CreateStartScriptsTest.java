package de.xgme.jojo.jigsaw_gradle_plugin.functional;

import de.xgme.jojo.jigsaw_gradle_plugin.classification.Functional;
import de.xgme.jojo.jigsaw_gradle_plugin.util.GradleVersion;
import de.xgme.jojo.jigsaw_gradle_plugin.util.TestProject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@GradleVersion("5.2")
@Functional
class CreateStartScriptsTest {
  @Test
  @DisplayName("Exported packages of required libraries must be accessible")
  void testReferenceToRequiredLibrary(TestProject project) throws IOException, InterruptedException {
    project.createFile("settings.gradle").rawContent(
      // language=groovy
      "rootProject.name = 'simple-project'\n" +
      "include 'library'\n");
    project.createFile("library/build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'library'\n" +
      "}\n");
    project.createFile("library/src/main/java/module-info.java").rawContent(
      // language=java
      "module library {\n" +
      "  exports library;\n" +
      "}\n");
    project.createFile("library/src/main/java/library/EmptyClass.java").rawContent(
      // language=java
      "package library;\n" +
      "public class EmptyClass {\n" +
      "}\n");
    project.createFile("build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'application'\n" +
      "    id 'distribution'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "application {\n" +
      "    mainClassName = \"consumer.Main\"\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "}\n" +
      "startScripts.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "}\n" +
      "dependencies {\n" +
      "    implementation project(':library')\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module consumer {\n" +
      "  requires library;\n" +
      "}\n");
    project.createFile("src/main/java/consumer/Main.java").rawContent(
      // language=java
      "package consumer;\n" +
      "import java.util.Arrays;\n" +
      "import library.EmptyClass;\n" +
      "public class Main {\n" +
      "  public static void main(String[] args) {\n" +
      "    if (args.length != 0) {\n" +
      "      throw new AssertionError(\"Unexpected arguments: \" + Arrays.asList(args));\n" +
      "    }\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    project.createRunner().withArguments("--stacktrace", ":startScripts", ":installDist").build();
    run(project);
  }

  @Test
  @DisplayName("Internal packages of libraries must not be accessible")
  void testInternalReferenceToLibrary(TestProject project) throws IOException, InterruptedException {
    project.createFile("settings.gradle").rawContent(
      // language=groovy
      "rootProject.name = 'simple-project'\n" +
      "include 'library'\n");
    project.createFile("library/build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'library'\n" +
      "}\n");
    project.createFile("library/src/main/java/module-info.java").rawContent(
      // language=java
      "module library {\n" +
      "}\n");
    project.createFile("library/src/main/java/library/EmptyClass.java").rawContent(
      // language=java
      "package library;\n" +
      "public class EmptyClass {\n" +
      "}\n");
    project.createFile("build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'application'\n" +
      "    id 'distribution'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "application {\n" +
      "    mainClassName = \"consumer.Main\"\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    export 'library' from 'library'\n" +
      "}\n" +
      "startScripts.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "}\n" +
      "dependencies {\n" +
      "    implementation project(':library')\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module consumer {\n" +
      "  requires library;\n" +
      "}\n");
    project.createFile("src/main/java/consumer/Main.java").rawContent(
      // language=java
      "package consumer;\n" +
      "import library.EmptyClass;\n" +
      "public class Main {\n" +
      "  public static void main(String[] args) {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    project.createRunner().withArguments("--stacktrace", ":startScripts", ":installDist").build();
    runAndFail(project);
  }

  @Test
  @DisplayName("Exported packages of unfamiliar libraries must not be accessible")
  void testReferenceToUnfamiliarLibrary(TestProject project) throws IOException, InterruptedException {
    project.createFile("settings.gradle").rawContent(
      // language=groovy
      "rootProject.name = 'simple-project'\n" +
      "include 'library'\n");
    project.createFile("library/build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'library'\n" +
      "}\n");
    project.createFile("library/src/main/java/module-info.java").rawContent(
      // language=java
      "module library {\n" +
      "  exports library;\n" +
      "}\n");
    project.createFile("library/src/main/java/library/EmptyClass.java").rawContent(
      // language=java
      "package library;\n" +
      "public class EmptyClass {\n" +
      "}\n");
    project.createFile("build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'application'\n" +
      "    id 'distribution'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "application {\n" +
      "    mainClassName = \"consumer.Main\"\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'library'\n" +
      "}\n" +
      "startScripts.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "}\n" +
      "dependencies {\n" +
      "    implementation project(':library')\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module consumer {\n" +
      "}\n");
    project.createFile("src/main/java/consumer/Main.java").rawContent(
      // language=java
      "package consumer;\n" +
      "import library.EmptyClass;\n" +
      "public class Main {\n" +
      "  public static void main(String[] args) {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    project.createRunner().withArguments("--stacktrace", ":startScripts", ":installDist").build();
    runAndFail(project);
  }

  @Test
  @DisplayName("Exported packages of dynamically required libraries must be accessible")
  void testReferenceToDynamicallyRequiredLibrary(TestProject project) throws IOException, InterruptedException {
    project.createFile("settings.gradle").rawContent(
      // language=groovy
      "rootProject.name = 'simple-project'\n" +
      "include 'library'\n");
    project.createFile("library/build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'library'\n" +
      "}\n");
    project.createFile("library/src/main/java/module-info.java").rawContent(
      // language=java
      "module library {\n" +
      "  exports library;\n" +
      "}\n");
    project.createFile("library/src/main/java/library/EmptyClass.java").rawContent(
      // language=java
      "package library;\n" +
      "public class EmptyClass {\n" +
      "}\n");
    project.createFile("build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'application'\n" +
      "    id 'distribution'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "application {\n" +
      "    mainClassName = \"consumer.Main\"\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'library'\n" +
      "}\n" +
      "startScripts.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'library'\n" +
      "}\n" +
      "dependencies {\n" +
      "    implementation project(':library')\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module consumer {\n" +
      "}\n");
    project.createFile("src/main/java/consumer/Main.java").rawContent(
      // language=java
      "package consumer;\n" +
      "import java.util.Arrays;\n" +
      "import library.EmptyClass;\n" +
      "public class Main {\n" +
      "  public static void main(String[] args) {\n" +
      "    if (args.length != 0) {\n" +
      "      throw new AssertionError(\"Unexpected arguments: \" + Arrays.asList(args));\n" +
      "    }\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    project.createRunner().withArguments("--stacktrace", ":startScripts", ":installDist").build();
    run(project);
  }

  @Test
  @DisplayName("Dynamically exported packages of required libraries must be accessible")
  void testReferenceToDynamicallyExportedPackage(TestProject project) throws IOException, InterruptedException {
    project.createFile("settings.gradle").rawContent(
      // language=groovy
      "rootProject.name = 'simple-project'\n" +
      "include 'library'\n");
    project.createFile("library/build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'library'\n" +
      "}\n");
    project.createFile("library/src/main/java/module-info.java").rawContent(
      // language=java
      "module library {\n" +
      "}\n");
    project.createFile("library/src/main/java/library/EmptyClass.java").rawContent(
      // language=java
      "package library;\n" +
      "public class EmptyClass {\n" +
      "}\n");
    project.createFile("build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'application'\n" +
      "    id 'distribution'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "application {\n" +
      "    mainClassName = \"consumer.Main\"\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    export 'library' from 'library'\n" +
      "}\n" +
      "startScripts.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    export 'library' from 'library'\n" +
      "}\n" +
      "dependencies {\n" +
      "    implementation project(':library')\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module consumer {\n" +
      "  requires library;\n" +
      "}\n");
    project.createFile("src/main/java/consumer/Main.java").rawContent(
      // language=java
      "package consumer;\n" +
      "import java.util.Arrays;\n" +
      "import library.EmptyClass;\n" +
      "public class Main {\n" +
      "  public static void main(String[] args) {\n" +
      "    if (args.length != 0) {\n" +
      "      throw new AssertionError(\"Unexpected arguments: \" + Arrays.asList(args));\n" +
      "    }\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    project.createRunner().withArguments("--stacktrace", ":startScripts", ":installDist").build();
    run(project);
  }

  private static void run(@NotNull TestProject project) throws IOException, InterruptedException {
    RunResult result = run0(project);
    if (result.exitValue != 0) {
      Assertions.fail("Execution failed with exit status " + result.exitValue + ". Output:\n" +
                      indented("| ", result.output));
    }
  }

  private static void runAndFail(@NotNull TestProject project) throws IOException, InterruptedException {
    RunResult result = run0(project);
    if (result.exitValue == 0) {
      Assertions.fail("Execution succeeded but was expected to fail. Output:\n" +
                      indented("| ", result.output));
    }
  }

  private static @NotNull String indented(@NotNull String linePrefix, @NotNull String text) {
    return text.lines().collect(Collectors.joining("\n" + linePrefix, linePrefix, ""));
  }

  private static @NotNull RunResult run0(@NotNull TestProject project)
    throws IOException, InterruptedException
  {
    Path dist = project.getRootDir().resolve("build").resolve("install").resolve("simple-project");
    Path bin = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")
               ? dist.resolve("bin").resolve("simple-project.bat")
               : dist.resolve("bin").resolve("simple-project");
    Process process = useSameJre(new ProcessBuilder(bin.toString()))
      .redirectErrorStream(true)
      .start();

    String output;
    try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      output = reader.lines().collect(Collectors.joining("\n"));
    }
    catch (UncheckedIOException e) {
      throw e.getCause();
    }

    process.waitFor();
    return new RunResult(process.exitValue(), output);
  }

  private static @NotNull ProcessBuilder useSameJre(@NotNull ProcessBuilder processBuilder) {
    Map<String, String> env = processBuilder.environment();
    env.put("JAVA_HOME", System.getProperty("java.home"));
    return processBuilder;
  }

  private static final class RunResult {
    private final          int    exitValue;
    private final @NotNull String output;

    public RunResult(int exitValue, @NotNull String output) {
      this.exitValue = exitValue;
      this.output = output;
    }
  }
}
