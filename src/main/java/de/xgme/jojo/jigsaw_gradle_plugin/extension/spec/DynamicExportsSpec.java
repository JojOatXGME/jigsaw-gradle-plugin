package de.xgme.jojo.jigsaw_gradle_plugin.extension.spec;

import de.xgme.jojo.jigsaw_gradle_plugin._util.OptionGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface DynamicExportsSpec {
  String THIS = OptionGenerator.THIS;

  @Contract("_ -> this")
  DynamicExportsSpec from(@NotNull String sourceModule);

  @Contract("_ -> this")
  DynamicExportsSpec to(@NotNull String... targetModules);
}
