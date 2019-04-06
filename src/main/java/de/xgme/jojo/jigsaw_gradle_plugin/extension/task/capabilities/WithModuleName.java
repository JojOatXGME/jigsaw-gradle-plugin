package de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities;

import org.jetbrains.annotations.Nullable;

public interface WithModuleName {
  @Nullable String getModuleName();
  void setModuleName(@Nullable String moduleName);
}
