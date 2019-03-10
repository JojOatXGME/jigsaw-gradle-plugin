package de.xgme.jojo.jigsaw_gradle_plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class JigsawConventionPlugin implements Plugin<Project> {
  @Override
  public void apply(@NotNull Project target) {
    target.getPluginManager().apply(JigsawBasePlugin.class);

    // todo ...
  }
}
