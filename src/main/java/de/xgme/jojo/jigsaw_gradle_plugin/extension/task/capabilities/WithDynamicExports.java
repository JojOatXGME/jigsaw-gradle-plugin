package de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicExportsDeclaration;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicExportsSpec;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task._property_marker.Input;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithDynamicExports {
  @Input
  @NotNull List<DynamicExportsDeclaration> getExports();
  @NotNull DynamicExportsSpec export(@NotNull String... packageNames);
}
