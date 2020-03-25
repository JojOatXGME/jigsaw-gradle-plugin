package de.xgme.jojo.jigsaw_gradle_plugin._action;

import de.xgme.jojo.jigsaw_gradle_plugin._util.ListUtil;
import de.xgme.jojo.jigsaw_gradle_plugin._util.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.CreateStartScriptsExtension;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CreateStartScriptsReconfigurationAction implements Action<Task> {
  private static final String DEFAULT_UNIX_GENERATOR_CLASS = "org.gradle.api.internal.plugins.UnixStartScriptGenerator";
  private static final String DEFAULT_WIN_GENERATOR_CLASS  = "org.gradle.api.internal.plugins.WindowsStartScriptGenerator";

  private final @NotNull CreateStartScriptsExtension extension;
  private @Nullable FileCollection modulePath;

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
    FileCollection     modulePath = task.getClasspath();
    String             moduleName = extension.getModuleName();
    String             mainClass  = task.getMainClassName();

    if (!DEFAULT_UNIX_GENERATOR_CLASS.equals(task.getUnixStartScriptGenerator().getClass().getName()) ||
        !DEFAULT_WIN_GENERATOR_CLASS.equals(task.getWindowsStartScriptGenerator().getClass().getName())) {
      task.getLogger().warn("Using Jigsaw plugin and custom ScriptGenerator together might have unexpected results");
    }

    if (moduleName == null) {
      // todo How to detect module name? Might not always be required.
    }
    if (mainClass != null && !mainClass.contains("/")) {
      mainClass = moduleName + "/" + mainClass;
    }

    this.modulePath = modulePath; // Will be used by CreateStartScriptsPostprocessingAction
    task.setClasspath(task.getProject().files());
    task.setDefaultJvmOpts(ListUtil.concat(
      task.getDefaultJvmOpts(),
      List.of("--module-path", "APP_HOME_LIBS_PLACEHOLDER"),
      OptionGenerator.generateArguments(moduleName,
                                        extension.getExports(),
                                        extension.getOpens(),
                                        extension.getReads()),
      List.of("--add-modules", "ALL-MODULE-PATH")));
    task.setMainClassName("--module " + mainClass);
  }

  @Nullable FileCollection getModulePath() {
    return modulePath;
  }
}
