package de.xgme.jojo.jigsaw_gradle_plugin.extension.spec;

import de.xgme.jojo.jigsaw_gradle_plugin.extension._impl.OptionGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface AdditionalExportsSpec {
  String THIS = OptionGenerator.THIS;

  @Contract("_ -> this")
  AdditionalExportsSpec from(@NotNull String module);

  @Contract("_ -> this")
  AdditionalExportsSpec to(@NotNull String... modules);
}
