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
class TestTest {
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
      "compileTestJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'junit'\n" +
      "}\n" +
      "test.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'junit'\n" +
      "    open 'consumer' to 'junit'\n" +
      "}\n" +
      "repositories {\n" +
      "    mavenCentral()\n" +
      "}\n" +
      "dependencies {\n" +
      "    implementation project(':library')\n" +
      "    testImplementation 'junit:junit:4.12'\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module consumer {\n" +
      "  requires library;\n" +
      "}\n");
    project.createFile("src/test/java/consumer/Main.java").rawContent(
      // language=java
      "package consumer;\n" +
      "import library.EmptyClass;\n" +
      "import org.junit.Test;\n" +
      "public class Main {\n" +
      "  @Test\n" +
      "  public void main() {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    BuildResult result   = project.createRunner().withArguments("--stacktrace", ":test").build();
    BuildTask   testTask = result.task(":test");

    Assertions.assertNotNull(testTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, testTask.getOutcome());
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
      "compileTestJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'junit'\n" +
      "    export 'library' from 'library'\n" +
      "}\n" +
      "test.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'junit'\n" +
      "    open 'consumer' to 'junit'\n" +
      "}\n" +
      "repositories {\n" +
      "    mavenCentral()\n" +
      "}\n" +
      "dependencies {\n" +
      "    implementation project(':library')\n" +
      "    testImplementation 'junit:junit:4.12'\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module consumer {\n" +
      "  requires library;\n" +
      "}\n");
    project.createFile("src/test/java/consumer/Main.java").rawContent(
      // language=java
      "package consumer;\n" +
      "import library.EmptyClass;\n" +
      "import org.junit.Test;\n" +
      "public class Main {\n" +
      "  @Test\n" +
      "  public void main() {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    BuildResult result   = project.createRunner().withArguments("--stacktrace", ":test").buildAndFail();
    BuildTask   testTask = result.task(":test");

    Assertions.assertNotNull(testTask);
    Assertions.assertEquals(TaskOutcome.FAILED, testTask.getOutcome());
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
      "compileTestJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'junit'\n" +
      "    require 'library'\n" +
      "}\n" +
      "test.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'junit'\n" +
      "    open 'consumer' to 'junit'\n" +
      "}\n" +
      "repositories {\n" +
      "    mavenCentral()\n" +
      "}\n" +
      "dependencies {\n" +
      "    implementation project(':library')\n" +
      "    testImplementation 'junit:junit:4.12'\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module consumer {\n" +
      "}\n");
    project.createFile("src/test/java/consumer/Main.java").rawContent(
      // language=java
      "package consumer;\n" +
      "import library.EmptyClass;\n" +
      "import org.junit.Test;\n" +
      "public class Main {\n" +
      "  @Test\n" +
      "  public void main() {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    BuildResult result   = project.createRunner().withArguments("--stacktrace", ":test").buildAndFail();
    BuildTask   testTask = result.task(":test");

    Assertions.assertNotNull(testTask);
    Assertions.assertEquals(TaskOutcome.FAILED, testTask.getOutcome());
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
      "compileTestJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'junit'\n" +
      "    require 'library'\n" +
      "}\n" +
      "test.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'junit'\n" +
      "    open 'consumer' to 'junit'\n" +
      "    require 'library'\n" +
      "}\n" +
      "repositories {\n" +
      "    mavenCentral()\n" +
      "}\n" +
      "dependencies {\n" +
      "    implementation project(':library')\n" +
      "    testImplementation 'junit:junit:4.12'\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module consumer {\n" +
      "}\n");
    project.createFile("src/test/java/consumer/Main.java").rawContent(
      // language=java
      "package consumer;\n" +
      "import library.EmptyClass;\n" +
      "import org.junit.Test;\n" +
      "public class Main {\n" +
      "  @Test\n" +
      "  public void main() {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    BuildResult result   = project.createRunner().withArguments("--stacktrace", ":test").build();
    BuildTask   testTask = result.task(":test");

    Assertions.assertNotNull(testTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, testTask.getOutcome());
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
      "compileTestJava.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'junit'\n" +
      "    export 'library' from 'library'\n" +
      "}\n" +
      "test.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'consumer'\n" +
      "    require 'junit'\n" +
      "    open 'consumer' to 'junit'\n" +
      "    export 'library' from 'library'\n" +
      "}\n" +
      "repositories {\n" +
      "    mavenCentral()\n" +
      "}\n" +
      "dependencies {\n" +
      "    implementation project(':library')\n" +
      "    testImplementation 'junit:junit:4.12'\n" +
      "}\n");
    project.createFile("src/main/java/module-info.java").rawContent(
      // language=java
      "module consumer {\n" +
      "  requires library;\n" +
      "}\n");
    project.createFile("src/test/java/consumer/Main.java").rawContent(
      // language=java
      "package consumer;\n" +
      "import library.EmptyClass;\n" +
      "import org.junit.Test;\n" +
      "public class Main {\n" +
      "  @Test\n" +
      "  public void main() {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    BuildResult result   = project.createRunner().withArguments("--stacktrace", ":test").build();
    BuildTask   testTask = result.task(":test");

    Assertions.assertNotNull(testTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, testTask.getOutcome());
  }

  @Test
  @DisplayName("All classes of other source sets must be accessible")
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
      "    require 'junit'\n" +
      "}\n" +
      "test.jigsaw {\n" +
      "    enabled = true\n" +
      "    moduleName = 'some.module'\n" +
      "    require 'junit'\n" +
      "    open 'pkg' to 'junit'\n" +
      "}\n" +
      "repositories {\n" +
      "    mavenCentral()\n" +
      "}\n" +
      "dependencies {\n" +
      "    testImplementation 'junit:junit:4.12'\n" +
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
    project.createFile("src/test/java/consumer/Main.java").rawContent(
      // language=java
      "package pkg;\n" +
      "import org.junit.Test;\n" +
      "public class Main {\n" +
      "  @Test\n" +
      "  public void main() {\n" +
      "    System.out.println(EmptyClass.class);\n" +
      "  }\n" +
      "}\n");

    BuildResult result   = project.createRunner().withArguments("--stacktrace", ":test").build();
    BuildTask   testTask = result.task(":test");

    Assertions.assertNotNull(testTask);
    Assertions.assertEquals(TaskOutcome.SUCCESS, testTask.getOutcome());
  }
}
