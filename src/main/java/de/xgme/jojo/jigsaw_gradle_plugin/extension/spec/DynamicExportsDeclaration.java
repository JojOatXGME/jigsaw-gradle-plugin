package de.xgme.jojo.jigsaw_gradle_plugin.extension.spec;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DynamicExportsDeclaration {
  @NotNull List<String> getPackageNames();
  @NotNull String getSourceModule();
  @NotNull List<String> getTargetModules();
}
