package de.xgme.jojo.jigsaw_gradle_plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class JigsawGeneratorPlugin implements Plugin<Project> {
  @Override
  public void apply(@NotNull Project target) {
    target.getPluginManager().apply(JigsawConventionPlugin.class);

    // todo ...
  }
}
