package de.xgme.jojo.jigsaw_gradle_plugin.functional;

import de.xgme.jojo.jigsaw_gradle_plugin.classification.Functional;
import de.xgme.jojo.jigsaw_gradle_plugin.util.GradleVersion;
import de.xgme.jojo.jigsaw_gradle_plugin.util.TestProject;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@GradleVersion("5.2")
@Functional
class UpToDateTest {
  TestProject project;

  @BeforeEach
  void setUp() throws IOException {
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
      "compileJava.jigsaw {\n" +
      "    enabled = enableForCompileJava.toBoolean()\n" +
      "    moduleName = 'some.module'\n" +
      "    moduleVersion = myVersion\n" +
      "}\n" +
      "javadoc.jigsaw {\n" +
      "    enabled = enableForJavadoc.toBoolean()\n" +
      "    moduleName = 'some.module'\n" +
      "}\n" +
      "jar.jigsaw {\n" +
      "    enabled = enableForJar.toBoolean()\n" +
      "    moduleName = 'some.module'\n" +
      "}\n" +
      "startScripts.jigsaw {\n" +
      "    enabled = enableForStartScripts.toBoolean()\n" +
      "    moduleName = 'some.module'\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module some.module {\n" +
      "}\n");
    project.createFile("src/main/java/some/pkg/Main.java").rawContent(
      // language=java
      "package some.pkg;\n" +
      "public class Main {\n" +
      "  public static void main(String[] args) {\n" +
      "  }\n" +
      "}\n");
    project.createFile("gradle.properties").rawContent(
      // language=properties
      "myVersion=1.0\n" +
      "enableForCompileJava=true\n" +
      "enableForJavadoc=true\n" +
      "enableForJar=true\n" +
      "enableForStartScripts=true\n");
  }

  @Test
  void testCompileJavaTask() {
    project.createRunner().withArguments(":build", "-PmyVersion=0.1").build();

    {
      BuildResult result        = project.createRunner().withArguments(":build", "-PmyVersion=0.1").build();
      BuildTask   compileResult = result.task(":compileJava");
      Assertions.assertNotNull(compileResult);
      Assertions.assertEquals(TaskOutcome.UP_TO_DATE, compileResult.getOutcome());
    }

    {
      BuildResult result        = project.createRunner().withArguments(":build", "-PmyVersion=1.0").build();
      BuildTask   compileResult = result.task(":compileJava");
      Assertions.assertNotNull(compileResult);
      Assertions.assertEquals(TaskOutcome.SUCCESS, compileResult.getOutcome());
    }
  }

  @Test
  void testJavadocTask() {
    project.createRunner().withArguments(":javadoc", "-PenableForJavadoc=true").build();

    {
      BuildResult result        = project.createRunner().withArguments(":javadoc", "-PenableForJavadoc=true").build();
      BuildTask   compileResult = result.task(":compileJava");
      BuildTask   javadocResult = result.task(":javadoc");
      Assertions.assertNotNull(compileResult);
      Assertions.assertNotNull(javadocResult);
      Assertions.assertEquals(TaskOutcome.UP_TO_DATE, compileResult.getOutcome());
      Assertions.assertEquals(TaskOutcome.UP_TO_DATE, javadocResult.getOutcome());
    }

    {
      BuildResult result        = project.createRunner().withArguments(":javadoc", "-PenableForJavadoc=false").build();
      BuildTask   compileResult = result.task(":compileJava");
      BuildTask   javadocResult = result.task(":javadoc");
      Assertions.assertNotNull(compileResult);
      Assertions.assertNotNull(javadocResult);
      Assertions.assertEquals(TaskOutcome.UP_TO_DATE, compileResult.getOutcome());
      Assertions.assertEquals(TaskOutcome.SUCCESS, javadocResult.getOutcome());
    }
  }

  @Test
  @Disabled("There is nothing implemented for this task yet.")
  void testJarTask() {
    project.createRunner().withArguments(":jar", "-PenableForJar=true").build();

    {
      BuildResult result        = project.createRunner().withArguments(":jar", "-PenableForJar=true").build();
      BuildTask   compileResult = result.task(":compileJava");
      BuildTask   jarResult     = result.task(":jar");
      Assertions.assertNotNull(compileResult);
      Assertions.assertNotNull(jarResult);
      Assertions.assertEquals(TaskOutcome.UP_TO_DATE, compileResult.getOutcome());
      Assertions.assertEquals(TaskOutcome.UP_TO_DATE, jarResult.getOutcome());
    }

    {
      BuildResult result        = project.createRunner().withArguments(":jar", "-PenableForJar=false").build();
      BuildTask   compileResult = result.task(":compileJava");
      BuildTask   jarResult     = result.task(":jar");
      Assertions.assertNotNull(compileResult);
      Assertions.assertNotNull(jarResult);
      Assertions.assertEquals(TaskOutcome.UP_TO_DATE, compileResult.getOutcome());
      Assertions.assertEquals(TaskOutcome.SUCCESS, jarResult.getOutcome());
    }
  }

  @Test
  void testStartScriptsTask() {
    project.createRunner().withArguments(":startScripts", "-PenableForStartScripts=true").build();

    {
      BuildResult result = project.createRunner().withArguments(":startScripts", "-PenableForStartScripts=true")
                                  .build();
      BuildTask scriptsResult = result.task(":startScripts");
      Assertions.assertNotNull(scriptsResult);
      Assertions.assertEquals(TaskOutcome.UP_TO_DATE, scriptsResult.getOutcome());
    }

    {
      BuildResult result = project.createRunner().withArguments(":startScripts", "-PenableForStartScripts=false")
                                  .build();
      BuildTask scriptsResult = result.task(":startScripts");
      Assertions.assertNotNull(scriptsResult);
      Assertions.assertEquals(TaskOutcome.SUCCESS, scriptsResult.getOutcome());
    }
  }
}
