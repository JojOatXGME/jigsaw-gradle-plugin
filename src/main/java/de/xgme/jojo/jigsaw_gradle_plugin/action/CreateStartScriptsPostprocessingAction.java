package de.xgme.jojo.jigsaw_gradle_plugin.action;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.CreateStartScriptsExtension;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;

public final class CreateStartScriptsPostprocessingAction implements Action<Task> {
  private @NotNull CreateStartScriptsExtension extension;

  public CreateStartScriptsPostprocessingAction(@NotNull CreateStartScriptsExtension extension) {
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

  private static @NotNull String read(@NotNull File file) throws IOException {
    return Files.readString(file.toPath(), StandardCharsets.UTF_8);
  }

  private static void write(@NotNull File file, @NotNull String content) throws IOException {
    Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
  }
}
