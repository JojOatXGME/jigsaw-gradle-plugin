package de.xgme.jojo.jigsaw_gradle_plugin.extension._impl;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicExportsDeclaration;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicExportsSpec;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

final class DynamicExports implements DynamicExportsDeclaration, DynamicExportsSpec, Serializable {
  private final @NotNull List<String> packageNames;
  private @NotNull       String       sourceModule;
  private @NotNull       List<String> targetModules;

  DynamicExports(@NotNull Collection<String> packageNames) {
    this.packageNames = List.copyOf(packageNames);
    this.sourceModule = THIS;
    this.targetModules = List.of(THIS);
  }

  @Override
  public @NotNull List<String> getPackageNames() {
    return packageNames;
  }

  @Override
  public @NotNull String getSourceModule() {
    return sourceModule;
  }

  @Override
  public @NotNull List<String> getTargetModules() {
    return targetModules;
  }

  @Override
  public DynamicExportsSpec from(@NotNull String sourceModule) {
    this.sourceModule = sourceModule;
    return this;
  }

  @Override
  public DynamicExportsSpec to(@NotNull String... targetModules) {
    this.targetModules = List.copyOf(Arrays.asList(targetModules));
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DynamicExports that = (DynamicExports) o;
    return packageNames.equals(that.packageNames) &&
           sourceModule.equals(that.sourceModule) &&
           targetModules.equals(that.targetModules);
  }

  @Override
  public int hashCode() {
    return Objects.hash(packageNames, sourceModule, targetModules);
  }
}
