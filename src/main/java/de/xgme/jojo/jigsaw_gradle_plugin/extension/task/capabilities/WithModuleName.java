package de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.property_marker.Input;
import org.jetbrains.annotations.Nullable;

public interface WithModuleName {
  @Input(optional = true)
  @Nullable String getModuleName();
  void setModuleName(@Nullable String moduleName);
}
