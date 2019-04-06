package de.xgme.jojo.jigsaw_gradle_plugin.action;

import de.xgme.jojo.jigsaw_gradle_plugin.action.util.ListUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.action.util.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin.action.util.SourceUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.JavaCompileExtension;
import org.gradle.api.Action;
import org.gradle.api.Task;
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
    // todo Add option --module to command line?

    if (!(looselyTypedTask instanceof JavaCompile)) {
      throw new IllegalStateException("This action must only be used on tasks of type JavaCompile");
    }
    if (!extension.isEnabled()) {
      return;
    }

    JavaCompile task          = (JavaCompile) looselyTypedTask;
    String      moduleName    = extension.getModuleName();
    String      moduleVersion = extension.getModuleVersion();

    if (moduleName == null) {
      moduleName = SourceUtil.findModuleNameFromSource(task.getSource());
    }

    task.getOptions().setCompilerArgs(ListUtil.concat(
      task.getOptions().getCompilerArgs(),
      List.of("--module-path", task.getClasspath().getAsPath(),
              "--patch-module", moduleName + "=" + task.getSource().getAsPath()), // todo Should not be necessary.
      moduleVersion == null ? Collections.emptyList() : List.of("--module-version", moduleVersion),
      OptionGenerator.generateArguments(moduleName,
                                        extension.getExports(), Collections.emptyList(), extension.getReads())));
    task.setClasspath(task.getProject().files()); // todo "pre-compiled" files should be passed here.
  }
}
