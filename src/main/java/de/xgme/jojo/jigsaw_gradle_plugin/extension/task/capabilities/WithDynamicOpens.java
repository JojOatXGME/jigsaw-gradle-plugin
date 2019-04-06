package de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicExportsDeclaration;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicExportsSpec;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.property_marker.Input;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithDynamicOpens {
  @Input
  @NotNull List<DynamicExportsDeclaration> getOpens();
  @NotNull DynamicExportsSpec open(@NotNull String... packageNames);
}
