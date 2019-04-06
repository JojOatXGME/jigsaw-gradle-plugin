package de.xgme.jojo.jigsaw_gradle_plugin._action;

import de.xgme.jojo.jigsaw_gradle_plugin._util.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.JavaExecExtension;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.JavaExec;
import org.jetbrains.annotations.NotNull;

public final class JavaExecReconfigurationAction implements Action<Task> {
  private @NotNull JavaExecExtension extension;

  public JavaExecReconfigurationAction(@NotNull JavaExecExtension extension) {
    this.extension = extension;
  }

  @Override
  public void execute(@NotNull Task looselyTypedTask) {
    if (!(looselyTypedTask instanceof JavaExec)) {
      throw new IllegalStateException("This action must only be used on tasks of type JavaExec");
    }
    if (!extension.isEnabled()) {
      return;
    }

    JavaExec task       = (JavaExec) looselyTypedTask;
    String   moduleName = extension.getModuleName();
    String   mainClass  = task.getMain();

    if (moduleName == null) {
      // todo How to detect module name? Might not always be required.
    }
    if (mainClass != null && !mainClass.contains("/")) {
      mainClass = moduleName + "/" + mainClass;
    }

    // todo A --patch-module option might be required in some cases.
    task.jvmArgs("--module-path", task.getClasspath().getAsPath());
    if (mainClass != null) {
      task.jvmArgs("--module", mainClass);
    }
    task.jvmArgs(OptionGenerator.generateArguments(moduleName,
                                                   extension.getExports(), extension.getOpens(),
                                                   extension.getReads()));
    task.setClasspath(task.getProject().files());
    // todo Should task.setMain(null) be called?
  }
}
