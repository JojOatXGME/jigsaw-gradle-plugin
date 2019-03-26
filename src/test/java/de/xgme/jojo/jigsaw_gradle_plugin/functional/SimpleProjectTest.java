package de.xgme.jojo.jigsaw_gradle_plugin.functional;

import de.xgme.jojo.jigsaw_gradle_plugin.classification.Functional;
import de.xgme.jojo.jigsaw_gradle_plugin.util.GradleVersion;
import de.xgme.jojo.jigsaw_gradle_plugin.util.TestProject;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;

@GradleVersion("5.2")
@Functional
class SimpleProjectTest {
  @Test
  void testCompileTask(TestProject project) throws IOException {
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
      "    moduleVersion = '0.1-a'\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module some.module {\n" +
      "}\n");

    Path            buildOutput     = project.getRootDir().resolve(Path.of("build", "classes", "java", "main"));
    BuildResult     result          = project.createRunner().withArguments(":compileJava").build();
    BuildTask       task            = result.task(":compileJava");
    ModuleReference moduleReference = ModuleFinder.of(buildOutput).find("some.module").orElse(null);

    Assertions.assertNotNull(task);
    Assertions.assertEquals(TaskOutcome.SUCCESS, task.getOutcome());
    Assertions.assertNotNull(moduleReference);
    Assertions.assertTrue(moduleReference.descriptor().rawVersion().isPresent());
    Assertions.assertEquals("0.1-a", moduleReference.descriptor().rawVersion().get());
  }

  @Test
  void testRunTask(TestProject project) throws IOException {
    project.createFile("settings.gradle").rawContent(
      // language=groovy
      "rootProject.name = 'simple-project'\n");
    project.createFile("build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'application'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "application {\n" +
      "    mainClassName = 'some.pkg.Main'\n" +
      "}\n" +
      "run.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'some.module'\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module some.module {\n" +
      "}\n");
    project.createFile("src/main/java/some/pkg/Main.java").rawContent(
      // language=java
      "package some.pkg;\n" +
      "import java.lang.module.ModuleDescriptor;\n" +
      "public class Main {\n" +
      "  public static void main(String[] args) {\n" +
      "    System.out.println(\"SUCCESSFUL_RUN_MARKER\");\n" +
      "    ModuleDescriptor descriptor = Main.class.getModule().getDescriptor();\n" +
      "    if (descriptor != null && descriptor.name().equals(\"some.module\"))\n" +
      "      System.out.println(\"CORRECT_MODULE_MARKER\");\n" +
      "  }\n" +
      "}\n");

    BuildResult result = project.createRunner().withArguments(":run").build();
    BuildTask   task   = result.task(":run");

    Assertions.assertNotNull(task);
    Assertions.assertEquals(TaskOutcome.SUCCESS, task.getOutcome());
    Assertions.assertTrue(result.getOutput().contains("SUCCESSFUL_RUN_MARKER"));
    Assertions.assertTrue(result.getOutput().contains("CORRECT_MODULE_MARKER"));
  }

  @Test
  void testTestTask(TestProject project) throws IOException {
    project.createFile("settings.gradle").rawContent(
      // language=groovy
      "rootProject.name = 'simple-project'\n");
    project.createFile("build.gradle").rawContent(
      // language=groovy
      "plugins {\n" +
      "    id 'java'\n" +
      "    id 'de.xgme.jojo.jigsaw-base'\n" +
      "}\n" +
      "repositories {\n" +
      "    mavenCentral()\n" +
      "}\n" +
      "dependencies {\n" +
      "    testImplementation 'junit:junit:4.12'\n" +
      "}\n" +
      "test.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'some.module'\n" +
      "    require 'junit'\n" +
      "    open 'some.pkg' to 'junit'\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module some.module {\n" +
      "}\n");
    project.createFile("src/test/java/some/pkg/TestClass.java").rawContent(
      // language=java
      "package some.pkg;\n" +
      "import java.lang.module.ModuleDescriptor;\n" +
      "import org.junit.Assert;\n" +
      "import org.junit.Test;\n" +
      "public class TestClass {\n" +
      "  @Test\n" +
      "  public void test() {\n" +
      "    ModuleDescriptor descriptor = TestClass.class.getModule().getDescriptor();\n" +
      "    Assert.assertNotNull(\"Module must be named\", descriptor);\n" +
      "    Assert.assertEquals(\"Module name must match\", \"some.module\", descriptor.name());\n" +
      "  }\n" +
      "}\n");

    BuildResult result = project.createRunner().withArguments(":test").build();
    BuildTask   task   = result.task(":test");

    Assertions.assertNotNull(task);
    Assertions.assertEquals(TaskOutcome.SUCCESS, task.getOutcome());
  }

  // todo javadoc, CreateStartScripts, jar
}
