package de.xgme.jojo.jigsaw_gradle_plugin.action.util;

import org.jetbrains.annotations.NotNull;

public class JavaDocOption {
  private final @NotNull String name;
  private final @NotNull String value;

  public JavaDocOption(@NotNull String name, @NotNull String value) {
    this.name = name;
    this.value = value;
  }

  public @NotNull String getName() {
    return name;
  }

  public @NotNull String getValue() {
    return value;
  }
}
