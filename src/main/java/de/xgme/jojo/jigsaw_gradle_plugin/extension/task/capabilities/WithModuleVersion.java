package de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.task._property_marker.Input;
import org.jetbrains.annotations.Nullable;

public interface WithModuleVersion {
  @Input(optional = true)
  @Nullable String getModuleVersion();
  void setModuleVersion(@Nullable String moduleVersion);
}
