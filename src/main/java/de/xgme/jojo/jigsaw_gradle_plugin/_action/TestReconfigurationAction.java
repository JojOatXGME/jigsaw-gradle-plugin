package de.xgme.jojo.jigsaw_gradle_plugin._action;

import de.xgme.jojo.jigsaw_gradle_plugin._util.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin._util.SourceSetUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.TestExtension;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
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

    Test           task           = (Test) looselyTypedTask;
    String         moduleName     = extension.getModuleName();
    FileCollection localClasspath = SourceSetUtil.getLocalClasspath(task.getProject(), task.getClasspath());

    if (moduleName == null) {
      // todo How to detect module name?
    }

    task.jvmArgs("--module-path", task.getClasspath().getAsPath(),
                 "--patch-module", moduleName + "=" + localClasspath.getAsPath(),
                 "--add-modules", "ALL-MODULE-PATH");
    task.jvmArgs(OptionGenerator.generateArguments(moduleName,
                                                   extension.getExports(), extension.getOpens(),
                                                   extension.getReads()));
    task.setClasspath(task.getProject().files());
  }
}
