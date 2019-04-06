package de.xgme.jojo.jigsaw_gradle_plugin._action;

import de.xgme.jojo.jigsaw_gradle_plugin._util.ListUtil;
import de.xgme.jojo.jigsaw_gradle_plugin._util.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.CreateStartScriptsExtension;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class CreateStartScriptsReconfigurationAction implements Action<Task> {
  private @NotNull CreateStartScriptsExtension extension;

  public CreateStartScriptsReconfigurationAction(@NotNull CreateStartScriptsExtension extension) {
    this.extension = extension;
  }

  @Override
  public void execute(@NotNull Task looselyTypedTask) {
    if (!(looselyTypedTask instanceof CreateStartScripts)) {
      throw new IllegalStateException("This action must only be used on tasks of type CreateStartScripts");
    }
    if (!extension.isEnabled()) {
      return;
    }

    CreateStartScripts task       = (CreateStartScripts) looselyTypedTask;
    String             moduleName = extension.getModuleName();
    String             mainClass  = task.getMainClassName();

    if (moduleName == null) {
      // todo How to detect module name? Might not always be required.
    }
    if (mainClass != null && !mainClass.contains("/")) {
      mainClass = moduleName + "/" + mainClass;
    }

    task.setDefaultJvmOpts(ListUtil.concat(task.getDefaultJvmOpts(),
                                           List.of("--module-path", "APP_HOME_LIBS_PLACEHOLDER"),
                                           mainClass == null ? Collections.emptyList()
                                                             : List.of("--module", mainClass),
                                           OptionGenerator.generateArguments(moduleName,
                                                                             extension.getExports(),
                                                                             extension.getOpens(),
                                                                             extension.getReads())));
    task.setClasspath(task.getProject().files());
    // todo Should task.setMainClassName(null) be called?
  }
}
