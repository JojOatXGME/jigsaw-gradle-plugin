package de.xgme.jojo.jigsaw_gradle_plugin._util;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModuleUtil {
  private ModuleUtil() {
    // This class cannot be instantiated.
  }

  public static @NotNull List<String> findModuleNames(@NotNull FileCollection modulePath) {
    Path[]       modulePathArray = modulePath.getFiles().stream().map(File::toPath).toArray(Path[]::new);
    ModuleFinder moduleFinder    = ModuleFinder.of(modulePathArray);
    return moduleFinder.findAll().stream()
                       .map(ref -> ref.descriptor().name())
                       .collect(Collectors.toList());
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
