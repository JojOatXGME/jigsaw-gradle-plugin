package de.xgme.jojo.jigsaw_gradle_plugin.integration;

import de.xgme.jojo.jigsaw_gradle_plugin.JigsawBasePlugin;
import de.xgme.jojo.jigsaw_gradle_plugin.classification.Integration;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.project.BaseProjectExtension;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities.Activatable;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities.WithModuleName;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.testfixtures.ProjectBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BaseProjectExtension ...")
@Integration
class BaseProjectExtensionTest {
  private static final String MODULE_NAME = "some_module_name";

  @Test
  @DisplayName("must be applied to tasks of the Java Plugin")
  void testJavaPlugin() {
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply("java");
    project.getPluginManager().apply(JigsawBasePlugin.class);
    project.getExtensions().getByType(BaseProjectExtension.class).setEnabled(true);
    project.getExtensions().getByType(BaseProjectExtension.class).setModuleName(MODULE_NAME);

    Assertions.assertAll(
      () -> assertEnabled(getTask(project, JavaPlugin.COMPILE_JAVA_TASK_NAME), true),
      () -> assertModuleName(getTask(project, JavaPlugin.COMPILE_JAVA_TASK_NAME), MODULE_NAME),
      () -> assertEnabled(getTask(project, JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME), true),
      () -> assertModuleName(getTask(project, JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME), MODULE_NAME),
      () -> assertEnabled(getTask(project, JavaPlugin.TEST_TASK_NAME), true),
      () -> assertModuleName(getTask(project, JavaPlugin.TEST_TASK_NAME), MODULE_NAME),
      () -> assertEnabled(getTask(project, JavaPlugin.JAVADOC_TASK_NAME), true),
      () -> assertModuleName(getTask(project, JavaPlugin.JAVADOC_TASK_NAME), MODULE_NAME));
  }

  @Test
  @DisplayName("must be applied to tasks of the Application Plugin")
  void testApplicationPlugin() {
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply("application");
    project.getPluginManager().apply(JigsawBasePlugin.class);
    project.getExtensions().getByType(BaseProjectExtension.class).setEnabled(true);
    project.getExtensions().getByType(BaseProjectExtension.class).setModuleName(MODULE_NAME);

    Assertions.assertAll(
      () -> assertEnabled(getTask(project, ApplicationPlugin.TASK_RUN_NAME), true),
      () -> assertModuleName(getTask(project, ApplicationPlugin.TASK_RUN_NAME), MODULE_NAME),
      () -> assertEnabled(getTask(project, ApplicationPlugin.TASK_START_SCRIPTS_NAME), true),
      () -> assertModuleName(getTask(project, ApplicationPlugin.TASK_START_SCRIPTS_NAME), MODULE_NAME));
  }

  @Test
  @DisplayName("must be applied to custom tasks")
  void testCustomTasks() {
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply(JigsawBasePlugin.class);
    project.getExtensions().getByType(BaseProjectExtension.class).setEnabled(true);
    project.getExtensions().getByType(BaseProjectExtension.class).setModuleName(MODULE_NAME);

    Task javaCompileTask = createTask(project, JavaCompile.class);
    Task testTask = createTask(project, org.gradle.api.tasks.testing.Test.class);
    Task javadocTask = createTask(project, Javadoc.class);
    Task runTask = createTask(project, JavaExec.class);
    Task createStartScriptsTask = createTask(project, CreateStartScripts.class);

    Assertions.assertAll(
      () -> assertEnabled(javaCompileTask, true),
      () -> assertModuleName(javaCompileTask, MODULE_NAME),
      () -> assertEnabled(testTask, true),
      () -> assertModuleName(testTask, MODULE_NAME),
      () -> assertEnabled(javadocTask, true),
      () -> assertModuleName(javadocTask, MODULE_NAME),
      () -> assertEnabled(runTask, true),
      () -> assertModuleName(runTask, MODULE_NAME),
      () -> assertEnabled(createStartScriptsTask, true),
      () -> assertModuleName(createStartScriptsTask, MODULE_NAME));
  }

  private static Task getTask(@NotNull Project project, @NotNull String taskName) {
    return project.getTasks().named(taskName).get();
  }

  private static Task createTask(@NotNull Project project, @NotNull Class<? extends Task> type) {
    return project.getTasks().register("custom" + type.getSimpleName(), type).get();
  }

  private static void assertEnabled(@NotNull Task task, boolean expected) {
    Assertions.assertEquals(expected, getExtension(task, Activatable.class).isEnabled(),
                            task.getName() + ".enabled");
  }

  private static void assertModuleName(@NotNull Task task, String expected) {
    Assertions.assertEquals(expected, getExtension(task, WithModuleName.class).getModuleName(),
                            task.getName() + ".moduleName");
  }

  private static <T> @NotNull T getExtension(@NotNull Task task, @NotNull Class<T> extensionType) {
    return task.getExtensions().getByType(extensionType);
  }
}
