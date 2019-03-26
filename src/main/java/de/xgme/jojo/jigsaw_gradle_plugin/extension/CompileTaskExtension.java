package de.xgme.jojo.jigsaw_gradle_plugin.extension;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompileTaskExtension extends BasicTaskExtension {
  private @Nullable String moduleVersion = null;

  public CompileTaskExtension(@NotNull BaseProjectExtension projectExtension) {
    super(projectExtension);
  }

  public @Nullable String getModuleVersion() {
    return moduleVersion;
  }

  public void setModuleVersion(@Nullable String moduleVersion) {
    this.moduleVersion = moduleVersion;
  }
}
