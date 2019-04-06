package de.xgme.jojo.jigsaw_gradle_plugin.extension._impl;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicReadsDeclaration;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicReadsSpec;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

final class DynamicReads implements DynamicReadsDeclaration, DynamicReadsSpec, Serializable {
  private final @NotNull List<String> requiredModules;
  private @NotNull       List<String> affectedModules;

  DynamicReads(@NotNull Collection<String> requiredModules) {
    this.requiredModules = requiredModules.isEmpty() ? List.of(THIS) : List.copyOf(requiredModules);
    this.affectedModules = List.of(THIS);
  }

  @Override
  public @NotNull List<String> getAffectedModules() {
    return affectedModules;
  }

  @Override
  public @NotNull List<String> getRequiredModules() {
    return requiredModules;
  }

  @Override
  public DynamicReadsSpec in(@NotNull String... affectedModules) {
    this.affectedModules = List.copyOf(Arrays.asList(affectedModules));
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DynamicReads that = (DynamicReads) o;
    return requiredModules.equals(that.requiredModules) &&
           affectedModules.equals(that.affectedModules);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requiredModules, affectedModules);
  }
}
