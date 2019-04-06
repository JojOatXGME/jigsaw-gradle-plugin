package de.xgme.jojo.jigsaw_gradle_plugin._action;

import de.xgme.jojo.jigsaw_gradle_plugin._util.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.TestExtension;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.testing.Test;
import org.jetbrains.annotations.NotNull;

public final class TestReconfigurationAction implements Action<Task> {
  private @NotNull TestExtension extension;

  public TestReconfigurationAction(@NotNull TestExtension extension) {
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
    task.jvmArgs(OptionGenerator.generateArguments(moduleName,
                                                   extension.getExports(), extension.getOpens(),
                                                   extension.getReads()));
    task.setClasspath(task.getProject().files());
  }
}
