package de.xgme.jojo.jigsaw_gradle_plugin.action;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.BasicTaskExtension;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.testing.Test;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

public final class TestMod {

  private TestMod() {
    // This class cannot be instantiated.
  }

  public static void apply(@NotNull Test task, @NotNull BasicTaskExtension extension) {
    task.doFirst(new ReconfigurationAction(extension));
    task.getInputs().property("jigsaw.enabled", callable(extension::isEnabled));
    task.getInputs().property("jigsaw.moduleName", callable(extension::getModuleName)).optional(true);
  }

  private static <T> @NotNull Callable<T> callable(@NotNull Callable<T> callable) {
    return callable;
  }

  private static class ReconfigurationAction implements Action<Task> {
    private @NotNull BasicTaskExtension extension;

    private ReconfigurationAction(@NotNull BasicTaskExtension extension) {
      this.extension = extension;
    }

    @Override
    public void execute(@NotNull Task looselyTypedTask) {
      if (!(looselyTypedTask instanceof Test)) {
        throw new IllegalStateException("This action must only be used on tasks of type Test");
      }
      if (!extension.isEnabled()) {
        return;
      }

      Test   task       = (Test) looselyTypedTask;
      String moduleName = extension.getModuleName();

      if (moduleName == null) {
        // todo How to detect module name?
      }

      task.jvmArgs("--module-path", task.getClasspath().getAsPath(),
                   "--patch-module", moduleName + "=" + task.getTestClassesDirs().getAsPath(), // todo does it work?
                   "--add-modules", "ALL-MODULE-PATH");
      task.jvmArgs(extension.getModuleInfoAdditionsAsCommandLineArguments(moduleName));
      task.setClasspath(task.getProject().files());
    }
  }
}
