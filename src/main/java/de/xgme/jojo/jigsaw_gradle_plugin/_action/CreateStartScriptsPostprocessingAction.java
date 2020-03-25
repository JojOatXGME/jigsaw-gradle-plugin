package de.xgme.jojo.jigsaw_gradle_plugin._action;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.CreateStartScriptsExtension;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public final class CreateStartScriptsPostprocessingAction implements Action<Task> {
  private final @NotNull CreateStartScriptsExtension             extension;
  private final @NotNull CreateStartScriptsReconfigurationAction reconfigurationAction;

  public CreateStartScriptsPostprocessingAction(
    @NotNull CreateStartScriptsExtension extension,
    @NotNull CreateStartScriptsReconfigurationAction reconfigurationAction)
  {
    this.extension = extension;
    this.reconfigurationAction = reconfigurationAction;
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
      replaceLibsPlaceholder(new File(task.getOutputDir(), applicationName),
                             reconfigurationAction.getModulePath(), false);
      replaceLibsPlaceholder(new File(task.getOutputDir(), applicationName + ".bat"),
                             reconfigurationAction.getModulePath(), true);
    }
    catch (IOException e) {
      throw new GradleException("Could not modify start scripts for " + task.getPath(), e);
    }
  }

  private static void replaceLibsPlaceholder(@NotNull File file, @Nullable FileCollection modulePath, boolean windows)
    throws IOException
  {
    String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
    Files.writeString(
      file.toPath(),
      content.replaceFirst(
        "APP_HOME_LIBS_PLACEHOLDER",
        Matcher.quoteReplacement(buildRelativeModulePath(modulePath, windows))),
      StandardCharsets.UTF_8);
  }

  private static @NotNull String buildRelativeModulePath(@Nullable FileCollection modulePath, boolean windows) {
    if (modulePath == null) {
      return "";
    }

    String prefix;
    String pathSeparator;
    String segmentSeparator;
    if (windows) {
      prefix = "%APP_HOME%";
      pathSeparator = ";";
      segmentSeparator = "\\";
    }
    else {
      prefix = "$APP_HOME";
      pathSeparator = ":";
      segmentSeparator = "/";
    }

    return modulePath.getFiles().stream()
                     .map(file -> prefix + segmentSeparator + "lib" + segmentSeparator + file.getName())
                     .collect(Collectors.joining(pathSeparator));
  }
}
