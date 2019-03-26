package de.xgme.jojo.jigsaw_gradle_plugin.extension._impl;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.AdditionalExportsSpec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AdditionalExports implements AdditionalExportsSpec {
  private final @NotNull List<String> packageNames;
  private @NotNull       String       sourceModule;
  private final @NotNull List<String> targetModules;

  public AdditionalExports(@NotNull Collection<String> packageNames) {
    this.packageNames = List.copyOf(packageNames);
    this.sourceModule = THIS;
    this.targetModules = new ArrayList<>();
  }

  @Override
  public AdditionalExportsSpec from(@NotNull String module) {
    sourceModule = module;
    return this;
  }

  @Override
  public AdditionalExportsSpec to(@NotNull String... modules) {
    targetModules.addAll(Arrays.asList(modules));
    return this;
  }

  @NotNull List<String> getPackageNames() {
    return packageNames;
  }

  @NotNull String getSourceModule() {
    return sourceModule;
  }

  @NotNull List<String> getTargetModules() {
    if (targetModules.isEmpty()) {
      return List.of(THIS);
    }
    return targetModules;
  }
}
