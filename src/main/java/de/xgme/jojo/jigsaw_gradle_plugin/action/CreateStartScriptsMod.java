package de.xgme.jojo.jigsaw_gradle_plugin.action;

import de.xgme.jojo.jigsaw_gradle_plugin.action.util.ListUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.BasicTaskExtension;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

public final class CreateStartScriptsMod {

  private CreateStartScriptsMod() {
    // This class cannot be instantiated.
  }

  public static void apply(@NotNull CreateStartScripts task, @NotNull BasicTaskExtension extension) {
    task.doFirst(new ReconfigurationAction(extension));
    task.doLast(new PostprocessingAction(extension));
    task.getInputs().property("jigsaw.enabled", callable(extension::isEnabled));
    task.getInputs().property("jigsaw.moduleName", callable(extension::getModuleName)).optional(true);
  }

  private static <T> @NotNull Callable<T> callable(@NotNull Callable<T> callable) {
    return callable;
  }

  private static @NotNull String read(@NotNull File file) throws IOException {
    return Files.readString(file.toPath(), StandardCharsets.UTF_8);
  }

  private static void write(@NotNull File file, @NotNull String content) throws IOException {
    Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
  }

  private static class ReconfigurationAction implements Action<Task> {
    private @NotNull BasicTaskExtension extension;

    private ReconfigurationAction(@NotNull BasicTaskExtension extension) {
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
                                             extension.getModuleInfoAdditionsAsCommandLineArguments(moduleName)));
      task.setClasspath(task.getProject().files());
      // todo Should task.setMainClassName(null) be called?
    }
  }

  private static class PostprocessingAction implements Action<Task> {
    private @NotNull BasicTaskExtension extension;

    private PostprocessingAction(@NotNull BasicTaskExtension extension) {
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

      CreateStartScripts task            = (CreateStartScripts) looselyTypedTask;
      String             applicationName = task.getApplicationName();
      if (applicationName == null) {
        throw new GradleException("Application name is null in task " + task.getPath());
      }
      try {
        File   bashFile    = new File(task.getOutputDir(), applicationName);
        String bashContent = read(bashFile);
        write(bashFile, bashContent.replaceFirst("APP_HOME_LIBS_PLACEHOLDER",
                                                 Matcher.quoteReplacement("$APP_HOME/lib")));
        File   batFile    = new File(task.getOutputDir(), applicationName + ".bat");
        String batContent = read(batFile);
        write(batFile, batContent.replaceFirst("APP_HOME_LIBS_PLACEHOLDER",
                                               Matcher.quoteReplacement("%APP_HOME%\\lib")));
      }
      catch (IOException e) {
        throw new GradleException("Could not modify start scripts for " + task.getPath(), e);
      }
    }
  }
}
