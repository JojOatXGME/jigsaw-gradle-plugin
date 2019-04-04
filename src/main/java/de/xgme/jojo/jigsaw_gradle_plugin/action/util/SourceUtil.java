package de.xgme.jojo.jigsaw_gradle_plugin.action.util;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileTree;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;

public final class SourceUtil {
  private SourceUtil() {
    // This class cannot be instantiated.
  }

  public static @NotNull String findModuleNameFromSource(@NotNull FileTree sources) {
    Set<File> moduleInfoSet = sources.matching(filter -> filter.include("module-info.java")).getFiles();
    if (moduleInfoSet.isEmpty()) {
      throw new GradleException("No module-info.java could be found in " + sources.getAsPath());
    }
    if (moduleInfoSet.size() > 1) {
      throw new GradleException("Multiple files named module-info.java found: " + moduleInfoSet);
    }
    // todo How to extract the information from module-info.java?
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
