package de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicReadsDeclaration;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicReadsSpec;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.property_marker.Input;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithDynamicReads {
  @Input
  @NotNull List<DynamicReadsDeclaration> getReads();
  @NotNull DynamicReadsSpec require(@NotNull String... requiredModules);
}
