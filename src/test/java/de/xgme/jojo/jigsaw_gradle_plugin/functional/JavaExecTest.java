package de.xgme.jojo.jigsaw_gradle_plugin.functional;

import de.xgme.jojo.jigsaw_gradle_plugin.classification.Functional;
import de.xgme.jojo.jigsaw_gradle_plugin.util.GradleVersion;
import de.xgme.jojo.jigsaw_gradle_plugin.util.TestProject;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@GradleVersion("5.2")
@Functional
class JavaExecTest {
  @Test
  @DisplayName("Exported packages of required libraries must be accessible")
  void testReferenceToRequiredLibrary(TestProject project) throws IOException {
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
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "application {\n" +
      "    mainClassName = \"consumer.Main\"\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "}\n" +
      "run.jigsaw {\n" +
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

    BuildResult result  = project.createRunner().withArguments("--stacktrace", ":run").build();
    BuildTask   runTask = result.task(":run");

    Assertions.assertNotNull(runTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, runTask.getOutcome());
  }

  @Test
  @DisplayName("Internal packages of libraries must not be accessible")
  void testInternalReferenceToLibrary(TestProject project) throws IOException {
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
      "run.jigsaw {\n" +
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

    BuildResult result  = project.createRunner().withArguments("--stacktrace", ":run").buildAndFail();
    BuildTask   runTask = result.task(":run");

    Assertions.assertNotNull(runTask);
    Assertions.assertEquals(TaskOutcome.FAILED, runTask.getOutcome());
  }

  @Test
  @DisplayName("Exported packages of unfamiliar libraries must not be accessible")
  void testReferenceToUnfamiliarLibrary(TestProject project) throws IOException {
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
      "run.jigsaw {\n" +
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

    BuildResult result  = project.createRunner().withArguments("--stacktrace", ":run").buildAndFail();
    BuildTask   runTask = result.task(":run");

    Assertions.assertNotNull(runTask);
    Assertions.assertEquals(TaskOutcome.FAILED, runTask.getOutcome());
  }

  @Test
  @DisplayName("Exported packages of dynamically required libraries must be accessible")
  void testReferenceToDynamicallyRequiredLibrary(TestProject project) throws IOException {
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
      "run.jigsaw {\n" +
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

    BuildResult result  = project.createRunner().withArguments("--stacktrace", ":run").build();
    BuildTask   runTask = result.task(":run");

    Assertions.assertNotNull(runTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, runTask.getOutcome());
  }

  @Test
  @DisplayName("Dynamically exported packages of required libraries must be accessible")
  void testReferenceToDynamicallyExportedPackage(TestProject project) throws IOException {
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
      "run.jigsaw {\n" +
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

    BuildResult result  = project.createRunner().withArguments("--stacktrace", ":run").build();
    BuildTask   runTask = result.task(":run");

    Assertions.assertNotNull(runTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, runTask.getOutcome());
  }

  @Test
  @DisplayName("All packages of other source sets must be accessible")
  void testInternalReferenceToOtherSourceSet(TestProject project) throws IOException {
    project.createFile("settings.gradle").rawContent(
      // language=groovy
      "rootProject.name = 'simple-project'\n");
    project.createFile("build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'some.module'\n" +
      "}\n" +
      "compileTestJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'some.module'\n" +
      "}\n" +
      "task run(type: JavaExec) {\n" +
      "    main = \"pkg.TestClass\"\n" +
      "    classpath = sourceSets.test.runtimeClasspath\n" +
      "    jigsaw {\n" +
      "        enabled = true\n" +
      "        moduleName = 'some.module'\n" +
      "    }\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module some.module {\n" +
      "}\n");
    project.createFile("src/main/java/pkg/EmptyClass.java").rawContent(
      // language=java
      "package pkg;\n" +
      "public class EmptyClass {\n" +
      "}\n");
    project.createFile("src/test/java/pkg/TestClass.java").rawContent(
      // language=java
      "package pkg;\n" +
      "import java.util.Arrays;\n" +
      "public class TestClass {\n" +
      "  public static void main(String[] args) {\n" +
      "    if (args.length != 0) {\n" +
      "      throw new AssertionError(\"Unexpected arguments: \" + Arrays.asList(args));\n" +
      "    }\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    BuildResult result  = project.createRunner().withArguments("--stacktrace", ":run").build();
    BuildTask   runTask = result.task(":run");

    Assertions.assertNotNull(runTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, runTask.getOutcome());
  }
}
