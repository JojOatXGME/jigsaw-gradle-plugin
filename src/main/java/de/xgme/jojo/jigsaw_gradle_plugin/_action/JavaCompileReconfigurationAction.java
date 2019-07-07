package de.xgme.jojo.jigsaw_gradle_plugin._action;

import de.xgme.jojo.jigsaw_gradle_plugin._util.ListUtil;
import de.xgme.jojo.jigsaw_gradle_plugin._util.ModuleUtil;
import de.xgme.jojo.jigsaw_gradle_plugin._util.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin._util.SourceSetUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.JavaCompileExtension;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class JavaCompileReconfigurationAction implements Action<Task> {
  private @NotNull JavaCompileExtension extension;

  public JavaCompileReconfigurationAction(@NotNull JavaCompileExtension extension) {
    this.extension = extension;
  }

  @Override
  public void execute(@NotNull Task looselyTypedTask) {
    if (!(looselyTypedTask instanceof JavaCompile)) {
      throw new IllegalStateException("This action must only be used on tasks of type JavaCompile");
    }
    if (!extension.isEnabled()) {
      return;
    }

    JavaCompile    task           = (JavaCompile) looselyTypedTask;
    String         moduleName     = extension.getModuleName();
    String         moduleVersion  = extension.getModuleVersion();
    FileCollection localClasspath = SourceSetUtil.getLocalClasspath(task.getProject(), task.getClasspath());
    FileCollection sourceDirs     = SourceSetUtil.getSourceDirs(task.getProject(), task.getSource());

    if (moduleName == null) {
      moduleName = ModuleUtil.findModuleNameFromSource(task.getSource());
    }

    task.getOptions().setCompilerArgs(ListUtil.concat(
      task.getOptions().getCompilerArgs(),
      List.of("--module-path", task.getClasspath().getAsPath()),
      moduleVersion == null ? Collections.emptyList() : List.of("--module-version", moduleVersion),
      OptionGenerator.generateArguments(moduleName,
                                        extension.getExports(), Collections.emptyList(), extension.getReads()),
      List.of("--patch-module", moduleName + "=" + localClasspath.plus(sourceDirs).getAsPath()),
      getOptionsToAddAllModules(task.getClasspath()))); // todo Only add modules specified in extension.getReads()
    task.setClasspath(task.getProject().files());
  }

  private static @NotNull List<String> getOptionsToAddAllModules(@NotNull FileCollection modulePath) {
    List<String> moduleNames = ModuleUtil.findModuleNames(modulePath);
    if (moduleNames.isEmpty()) {
      return Collections.emptyList();
    }
    else {
      return List.of("--add-modules", String.join(",", moduleNames));
    }
  }

}
