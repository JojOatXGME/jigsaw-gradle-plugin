package de.xgme.jojo.jigsaw_gradle_plugin.extension._impl;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.project.BaseProjectExtension;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicExportsDeclaration;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicExportsSpec;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicReadsDeclaration;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicReadsSpec;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TaskExtensionImpl
  implements CreateStartScriptsExtension,
             JarExtension,
             JavaCompileExtension,
             JavadocExtension,
             JavaExecExtension,
             TestExtension
{
  private final     BaseProjectExtension projectExtension;
  private @Nullable Boolean              enabled;
  private @Nullable String               moduleName;
  private @Nullable String               moduleVersion;
  private final     List<DynamicExports> dynamicExports = new ArrayList<>();
  private final     List<DynamicExports> dynamicOpens   = new ArrayList<>();
  private final     List<DynamicReads>   dynamicReads   = new ArrayList<>();

  public TaskExtensionImpl(@NotNull BaseProjectExtension projectExtension) {
    this.projectExtension = projectExtension;
  }

  @Override
  public boolean isEnabled() {
    return enabled == null ? projectExtension.isEnabled() : enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public @Nullable String getModuleName() {
    return moduleName == null ? projectExtension.getModuleName() : moduleName;
  }

  @Override
  public void setModuleName(@Nullable String moduleName) {
    this.moduleName = moduleName;
  }

  @Override
  public @Nullable String getModuleVersion() {
    return moduleVersion == null ? projectExtension.getModuleVersion() : moduleVersion;
  }

  @Override
  public void setModuleVersion(@Nullable String moduleVersion) {
    this.moduleVersion = moduleVersion;
  }

  @Override
  public @NotNull List<DynamicExportsDeclaration> getExports() {
    return Collections.unmodifiableList(dynamicExports);
  }

  @Override
  public @NotNull DynamicExportsSpec export(@NotNull String... packageNames) {
    DynamicExports exports = new DynamicExports(Arrays.asList(packageNames));
    dynamicExports.add(exports);
    return exports;
  }

  @Override
  public @NotNull List<DynamicExportsDeclaration> getOpens() {
    return Collections.unmodifiableList(dynamicOpens);
  }

  @Override
  public @NotNull DynamicExportsSpec open(@NotNull String... packageNames) {
    // todo Not required for all tasks. Only relevant at runtime.
    DynamicExports exports = new DynamicExports(Arrays.asList(packageNames));
    dynamicOpens.add(exports);
    return exports;
  }

  @Override
  public @NotNull List<DynamicReadsDeclaration> getReads() {
    return Collections.unmodifiableList(dynamicReads);
  }

  @Override
  public @NotNull DynamicReadsSpec require(@NotNull String... requiredModules) {
    DynamicReads reads = new DynamicReads(Arrays.asList(requiredModules));
    dynamicReads.add(reads);
    return reads;
  }
}
