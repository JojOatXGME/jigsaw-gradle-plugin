package de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.property_marker.Input;

public interface Activatable {
  @Input
  boolean isEnabled();
  void setEnabled(boolean enabled);
}
