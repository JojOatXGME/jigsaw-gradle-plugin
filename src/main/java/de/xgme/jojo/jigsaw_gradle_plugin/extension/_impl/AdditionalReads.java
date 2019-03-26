package de.xgme.jojo.jigsaw_gradle_plugin.extension._impl;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.AdditionalReadsSpec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AdditionalReads implements AdditionalReadsSpec {
  private final @NotNull List<String> affectedModules;
  private final @NotNull List<String> requiredModules;

  public AdditionalReads(@NotNull Collection<String> requiredModules) {
    this.affectedModules = new ArrayList<>();
    this.requiredModules = List.copyOf(requiredModules);
  }

  @Override
  public AdditionalReadsSpec in(@NotNull String... modules) {
    affectedModules.addAll(Arrays.asList(modules));
    return this;
  }

  @NotNull List<String> getAffectedModules() {
    if (affectedModules.isEmpty()) {
      return List.of(THIS);
    }
    return affectedModules;
  }

  @NotNull List<String> getRequiredModules() {
    if (requiredModules.isEmpty()) {
      return List.of(THIS);
    }
    return requiredModules;
  }
}
