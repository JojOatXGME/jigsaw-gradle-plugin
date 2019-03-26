package de.xgme.jojo.jigsaw_gradle_plugin;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.BasicTaskExtension;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.CompileTaskExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.jetbrains.annotations.NotNull;

public class JigsawConventionPlugin implements Plugin<Project> {
  @Override
  public void apply(@NotNull Project target) {
    target.getPluginManager().apply(JigsawBasePlugin.class);

    // todo Enable option to use jigsaw by default in base plugin.
    // todo Add project extension for module info additions. (also for test environments)

    target.getPluginManager().withPlugin("org.gradle.java", __ -> {
      target.getTasks().withType(JavaCompile.class).named(JavaPlugin.COMPILE_JAVA_TASK_NAME, task -> {
        CompileTaskExtension extension = task.getExtensions().getByType(CompileTaskExtension.class);
        extension.setEnabled(true);
        extension.setModuleVersion(target.getVersion().toString());
      });

      target.getTasks().withType(JavaCompile.class).named(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME, task -> {
        CompileTaskExtension extension = task.getExtensions().getByType(CompileTaskExtension.class);
        extension.setEnabled(true);
        extension.setModuleVersion(target.getVersion().toString());
      });

      target.getTasks().withType(Test.class).named(JavaPlugin.TEST_TASK_NAME, task -> {
        BasicTaskExtension extension = task.getExtensions().getByType(BasicTaskExtension.class);
        extension.setEnabled(true);
      });

      target.getTasks().withType(Javadoc.class).named(JavaPlugin.JAVADOC_TASK_NAME, task -> {
        BasicTaskExtension extension = task.getExtensions().getByType(BasicTaskExtension.class);
        extension.setEnabled(true);
      });

      target.getTasks().withType(Jar.class).named(JavaPlugin.JAR_TASK_NAME, task -> {
        // todo Handle this task.
      });
    });

    target.getPluginManager().withPlugin("org.gradle.application", __ -> {
      target.getTasks().withType(JavaExec.class).named(ApplicationPlugin.TASK_RUN_NAME, task -> {
        BasicTaskExtension extension = task.getExtensions().getByType(BasicTaskExtension.class);
        extension.setEnabled(true);
      });

      target.getTasks().withType(CreateStartScripts.class).named(ApplicationPlugin.TASK_START_SCRIPTS_NAME, task -> {
        BasicTaskExtension extension = task.getExtensions().getByType(BasicTaskExtension.class);
        extension.setEnabled(true);
      });
    });
  }
}
