package de.xgme.jojo.jigsaw_gradle_plugin.extension;

import org.jetbrains.annotations.Nullable;

public class BaseProjectExtension {
  private           boolean enabled    = false;
  private @Nullable String  moduleName = null;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public @Nullable String getModuleName() {
    return moduleName;
  }

  public void setModuleName(@Nullable String moduleName) {
    this.moduleName = moduleName;
  }
}
