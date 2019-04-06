package de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities;

public interface Activatable {
  boolean isEnabled();
  void setEnabled(boolean enabled);
}
