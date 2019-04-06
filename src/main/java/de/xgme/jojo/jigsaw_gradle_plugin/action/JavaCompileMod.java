package de.xgme.jojo.jigsaw_gradle_plugin.action;

import de.xgme.jojo.jigsaw_gradle_plugin.action.util.ListUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.action.util.SourceUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.CompileTaskExtension;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public final class JavaCompileMod {

  private JavaCompileMod() {
    // This class cannot be instantiated.
  }

  public static void apply(@NotNull JavaCompile task, @NotNull CompileTaskExtension extension) {
    task.doFirst(new ReconfigurationAction(extension));
    task.getInputs().property("jigsaw.enabled", callable(extension::isEnabled));
    task.getInputs().property("jigsaw.moduleName", callable(extension::getModuleName)).optional(true);
    task.getInputs().property("jigsaw.moduleVersion", callable(extension::getModuleVersion)).optional(true);
  }

  private static <T> @NotNull Callable<T> callable(@NotNull Callable<T> callable) {
    return callable;
  }

  private static class ReconfigurationAction implements Action<Task> {
    private @NotNull CompileTaskExtension extension;

    private ReconfigurationAction(@NotNull CompileTaskExtension extension) {
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
        extension.getModuleInfoAdditionsAsCommandLineArguments(moduleName)));
      task.setClasspath(task.getProject().files()); // todo "pre-compiled" files should be passed here.
    }
  }
}