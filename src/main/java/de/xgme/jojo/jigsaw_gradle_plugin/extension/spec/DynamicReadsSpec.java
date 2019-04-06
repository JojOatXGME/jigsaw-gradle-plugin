package de.xgme.jojo.jigsaw_gradle_plugin.extension.spec;

import de.xgme.jojo.jigsaw_gradle_plugin._util.OptionGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface DynamicReadsSpec {
  String THIS = OptionGenerator.THIS;

  @Contract("_ -> this")
  DynamicReadsSpec in(@NotNull String... affectedModules);
}
