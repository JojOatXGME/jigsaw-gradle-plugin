package de.xgme.jojo.jigsaw_gradle_plugin._util;

import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class SourceSetUtil {
  private SourceSetUtil() {
    // This class cannot be instantiated.
  }

  public static @NotNull FileCollection getLocalClasspath(@NotNull Project project,
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

  public static @NotNull FileCollection getSourceDirs(@NotNull Project project, @NotNull FileTree sources) {
    JavaPluginConvention       javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
    ConfigurableFileCollection sourceDirs     = project.files();
    for (File sourceFile : sources) {
      for (SourceSet sourceSet : javaConvention.getSourceSets()) {
        for (File sourceDirectory : sourceSet.getAllSource().getSourceDirectories()) {
          if (sourceFile.toPath().startsWith(sourceDirectory.toPath())) {
            sourceDirs.from(sourceDirectory);
          }
        }
      }
    }
    return sourceDirs;
  }
}
