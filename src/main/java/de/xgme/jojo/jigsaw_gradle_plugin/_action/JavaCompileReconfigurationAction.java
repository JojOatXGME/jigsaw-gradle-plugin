package de.xgme.jojo.jigsaw_gradle_plugin._action;

import de.xgme.jojo.jigsaw_gradle_plugin._util.ListUtil;
import de.xgme.jojo.jigsaw_gradle_plugin._util.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin._util.SourceUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.JavaCompileExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    FileCollection localClasspath = getLocalClasspath(task.getProject(), task.getClasspath());
    FileCollection modulePath     = task.getClasspath().minus(localClasspath);

    if (moduleName == null) {
      moduleName = SourceUtil.findModuleNameFromSource(task.getSource());
    }

    task.getOptions().setCompilerArgs(ListUtil.concat(
      task.getOptions().getCompilerArgs(),
      List.of("--module-path", modulePath.getAsPath()),
      moduleVersion == null ? Collections.emptyList() : List.of("--module-version", moduleVersion),
      OptionGenerator.generateArguments(moduleName,
                                        extension.getExports(), Collections.emptyList(), extension.getReads()),
      getOptionsToAddAllModules(modulePath)));
    task.setClasspath(localClasspath);
  }

  private static @NotNull List<String> getOptionsToAddAllModules(@NotNull FileCollection modulePath) {
    Path[]       modulePathArray = modulePath.getFiles().stream().map(File::toPath).toArray(Path[]::new);
    ModuleFinder moduleFinder    = ModuleFinder.of(modulePathArray);
    List<String> moduleNames = moduleFinder.findAll().stream()
                                           .map(ref -> ref.descriptor().name())
                                           .collect(Collectors.toList());
    if (moduleNames.isEmpty()) {
      return Collections.emptyList();
    }
    else {
      return List.of("--add-modules", String.join(",", moduleNames));
    }
  }

  private static @NotNull FileCollection getLocalClasspath(@NotNull Project project,
                                                           @NotNull FileCollection fullClasspath)
  {
    JavaPluginConvention       javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
    ConfigurableFileCollection localClasspath = project.files();
    for (File classpathElement : fullClasspath.getFiles()) {
      for (SourceSet sourceSet : javaConvention.getSourceSets()) {
        if (sourceSet.getOutput().contains(classpathElement)) {
          localClasspath.from(classpathElement);
        }
      }
    }
    return localClasspath;
  }
}
