package de.xgme.jojo.jigsaw_gradle_plugin.extension.spec;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DynamicReadsDeclaration {
  @NotNull List<String> getAffectedModules();
  @NotNull List<String> getRequiredModules();
}
