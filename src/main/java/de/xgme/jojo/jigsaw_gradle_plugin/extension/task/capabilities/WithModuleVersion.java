package de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities;

import org.jetbrains.annotations.Nullable;

public interface WithModuleVersion {
  @Nullable String getModuleVersion();
  void setModuleVersion(@Nullable String moduleVersion);
}
