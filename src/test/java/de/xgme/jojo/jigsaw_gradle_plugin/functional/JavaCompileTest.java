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
class JavaCompileTest {
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
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
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

    BuildResult result          = project.createRunner().withArguments("--stacktrace", ":compileJava").build();
    BuildTask   compileJavaTask = result.task(":compileJava");

    Assertions.assertNotNull(compileJavaTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, compileJavaTask.getOutcome());
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
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
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

    BuildResult result          = project.createRunner().withArguments("--stacktrace", ":compileJava").buildAndFail();
    BuildTask   compileJavaTask = result.task(":compileJava");

    Assertions.assertNotNull(compileJavaTask);
    Assertions.assertEquals(TaskOutcome.FAILED, compileJavaTask.getOutcome());
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
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
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

    BuildResult result          = project.createRunner().withArguments("--stacktrace", ":compileJava").buildAndFail();
    BuildTask   compileJavaTask = result.task(":compileJava");

    Assertions.assertNotNull(compileJavaTask);
    Assertions.assertEquals(TaskOutcome.FAILED, compileJavaTask.getOutcome());
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
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
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
      "import library.EmptyClass;\n" +
      "public class Main {\n" +
      "  public static void main(String[] args) {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    BuildResult result          = project.createRunner().withArguments("--stacktrace", ":compileJava").build();
    BuildTask   compileJavaTask = result.task(":compileJava");

    Assertions.assertNotNull(compileJavaTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, compileJavaTask.getOutcome());
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
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
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
      "import library.EmptyClass;\n" +
      "public class Main {\n" +
      "  public static void main(String[] args) {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    BuildResult result          = project.createRunner().withArguments("--stacktrace", ":compileJava").build();
    BuildTask   compileJavaTask = result.task(":compileJava");

    Assertions.assertNotNull(compileJavaTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, compileJavaTask.getOutcome());
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
      "public class TestClass {\n" +
      "  public void test() {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    BuildResult result              = project.createRunner().withArguments("--stacktrace", ":compileTestJava").build();
    BuildTask   compileJavaTask     = result.task(":compileJava");
    BuildTask   compileTestJavaTask = result.task(":compileTestJava");

    Assertions.assertNotNull(compileJavaTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, compileJavaTask.getOutcome());
    Assertions.assertNotNull(compileTestJavaTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, compileTestJavaTask.getOutcome());
  }

  @Test
  @DisplayName("Other source sets must not be part of the module path")
  void testOtherSourceSetNotPartOfModulePath(TestProject project) throws IOException {
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
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "configurations {\n" +
      "    foo\n" +
      "}\n" +
      "dependencies {\n" +
      "    foo project(':library')\n" +
      "}\n" +
      "task copyLibrary(type: Copy) {\n" +
      "    from configurations.foo\n" +
      "    into sourceSets.main.resources.srcDirs[0]\n" +
      "}\n" +
      "compileJava {\n" +
      "    dependsOn('copyLibrary')\n" +
      "}\n" +
      "compileJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "}\n" +
      "compileTestJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'library'\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module consumer {\n" +
      "}\n");
    project.createFile("src/test/java/consumer/Main.java").rawContent(
      // language=java
      "package consumer;\n" +
      "import library.EmptyClass;\n" +
      "public class Main {\n" +
      "  public static void main(String[] args) {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    BuildResult result = project.createRunner().withArguments("--stacktrace", ":compileTestJava")
                                .buildAndFail();
    BuildTask compileJavaTask     = result.task(":compileJava");
    BuildTask compileTestJavaTask = result.task(":compileTestJava");

    Assertions.assertNotNull(compileJavaTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, compileJavaTask.getOutcome());
    Assertions.assertNotNull(compileTestJavaTask);
    Assertions.assertEquals(TaskOutcome.FAILED, compileTestJavaTask.getOutcome());
  }
}
