package de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicExportsDeclaration;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicExportsSpec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithDynamicExports {
  @NotNull List<DynamicExportsDeclaration> getExports();
  @NotNull DynamicExportsSpec export(@NotNull String... packageNames);
}
