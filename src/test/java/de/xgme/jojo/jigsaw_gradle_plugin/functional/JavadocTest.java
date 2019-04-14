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
class JavadocTest {
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
      "javadoc.jigsaw {\n" +
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
      "/**\n" +
      " * {@link library.EmptyClass}\n" +
      " */\n" +
      "public class Main {\n" +
      "}\n");

    BuildResult result      = project.createRunner().withArguments("--stacktrace", ":javadoc").build();
    BuildTask   javadocTask = result.task(":javadoc");

    Assertions.assertNotNull(javadocTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, javadocTask.getOutcome());
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
      "javadoc.jigsaw {\n" +
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
      "/**\n" +
      " * {@link library.EmptyClass}\n" +
      " */\n" +
      "public class Main {\n" +
      "}\n");

    BuildResult result      = project.createRunner().withArguments("--stacktrace", ":javadoc").buildAndFail();
    BuildTask   javadocTask = result.task(":javadoc");

    Assertions.assertNotNull(javadocTask);
    Assertions.assertEquals(TaskOutcome.FAILED, javadocTask.getOutcome());
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
      "javadoc.jigsaw {\n" +
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
      "/**\n" +
      " * {@link library.EmptyClass}\n" +
      " */\n" +
      "public class Main {\n" +
      "}\n");

    BuildResult result      = project.createRunner().withArguments("--stacktrace", ":javadoc").buildAndFail();
    BuildTask   javadocTask = result.task(":javadoc");

    Assertions.assertNotNull(javadocTask);
    Assertions.assertEquals(TaskOutcome.FAILED, javadocTask.getOutcome());
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
      "javadoc.jigsaw {\n" +
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
      "/**\n" +
      " * {@link library.EmptyClass}\n" +
      " */\n" +
      "public class Main {\n" +
      "}\n");

    BuildResult result      = project.createRunner().withArguments("--stacktrace", ":javadoc").build();
    BuildTask   javadocTask = result.task(":javadoc");

    Assertions.assertNotNull(javadocTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, javadocTask.getOutcome());
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
      "javadoc.jigsaw {\n" +
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
      "/**\n" +
      " * {@link library.EmptyClass}\n" +
      " */\n" +
      "public class Main {\n" +
      "}\n");

    BuildResult result      = project.createRunner().withArguments("--stacktrace", ":javadoc").build();
    BuildTask   javadocTask = result.task(":javadoc");

    Assertions.assertNotNull(javadocTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, javadocTask.getOutcome());
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
      "task javadocTest(type: Javadoc) {\n" +
      "    classpath = sourceSets.test.output + sourceSets.test.compileClasspath\n" +
      "    source = sourceSets.test.allJava\n" +
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
      "/**\n" +
      " * {@link EmptyClass}\n" +
      " */\n" +
      "public class TestClass {\n" +
      "}\n");

    BuildResult result              = project.createRunner().withArguments("--stacktrace", ":javadocTest").build();
    BuildTask   compileJavaTask     = result.task(":compileJava");
    BuildTask   compileTestJavaTask = result.task(":compileTestJava");
    BuildTask   javadocTask         = result.task(":javadocTest");

    Assertions.assertNotNull(compileJavaTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, compileJavaTask.getOutcome());
    Assertions.assertNotNull(compileTestJavaTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, compileTestJavaTask.getOutcome());
    Assertions.assertNotNull(javadocTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, javadocTask.getOutcome());
  }
}
