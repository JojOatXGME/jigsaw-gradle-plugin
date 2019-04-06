package de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicReadsDeclaration;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicReadsSpec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithDynamicReads {
  @NotNull List<DynamicReadsDeclaration> getReads();
  @NotNull DynamicReadsSpec require(@NotNull String... requiredModules);
}
