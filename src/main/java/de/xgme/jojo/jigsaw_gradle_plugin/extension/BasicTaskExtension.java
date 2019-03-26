package de.xgme.jojo.jigsaw_gradle_plugin.extension;

import de.xgme.jojo.jigsaw_gradle_plugin.extension._impl.AdditionalExports;
import de.xgme.jojo.jigsaw_gradle_plugin.extension._impl.AdditionalReads;
import de.xgme.jojo.jigsaw_gradle_plugin.extension._impl.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.AdditionalExportsSpec;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.AdditionalReadsSpec;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.util.JavaDocOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicTaskExtension {
  private final     BaseProjectExtension    projectExtension;
  private @Nullable Boolean                 enabled;
  private @Nullable String                  moduleName;
  private final     List<AdditionalExports> additionalExports = new ArrayList<>();
  private final     List<AdditionalExports> additionalOpens   = new ArrayList<>();
  private final     List<AdditionalReads>   additionalReads   = new ArrayList<>();

  public BasicTaskExtension(@NotNull BaseProjectExtension projectExtension) {
    this.projectExtension = projectExtension;
  }

  public boolean isEnabled() {
    return enabled == null ? projectExtension.isEnabled() : enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public @Nullable String getModuleName() {
    return moduleName == null ? projectExtension.getModuleName() : moduleName;
  }

  public void setModuleName(@Nullable String moduleName) {
    this.moduleName = moduleName;
  }

  public @NotNull AdditionalExportsSpec export(@NotNull String... packageNames) {
    AdditionalExports exports = new AdditionalExports(Arrays.asList(packageNames));
    additionalExports.add(exports);
    return exports;
  }

  public @NotNull AdditionalExportsSpec open(@NotNull String... packageNames) {
    // todo Not required for all tasks. Only relevant at runtime.
    AdditionalExports exports = new AdditionalExports(Arrays.asList(packageNames));
    additionalOpens.add(exports);
    return exports;
  }

  public @NotNull AdditionalReadsSpec require(@NotNull String... requiredModules) {
    AdditionalReads reads = new AdditionalReads(Arrays.asList(requiredModules));
    additionalReads.add(reads);
    return reads;
  }

  public @NotNull List<String> getModuleInfoAdditionsAsCommandLineArguments(@NotNull String thisModuleName) {
    return OptionGenerator.generateArguments(thisModuleName, additionalExports, additionalOpens, additionalReads);
  }

  public @NotNull List<JavaDocOption> getModuleInfoAdditionsAsJavaDocOptions(@NotNull String thisModuleName) {
    return OptionGenerator.generateJavaDocOptions(thisModuleName, additionalExports, additionalOpens, additionalReads);
  }
}
