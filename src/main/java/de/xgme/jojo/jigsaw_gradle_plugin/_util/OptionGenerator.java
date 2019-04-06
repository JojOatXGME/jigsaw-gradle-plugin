package de.xgme.jojo.jigsaw_gradle_plugin._util;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicExportsDeclaration;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.spec.DynamicReadsDeclaration;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OptionGenerator {
  public static final String THIS = "this";

  private OptionGenerator() {
    // This class cannot be instantiated.
  }

  public static @NotNull List<String> generateArguments(
    @NotNull String thisModuleName,
    @NotNull Collection<? extends DynamicExportsDeclaration> additionalExports,
    @NotNull Collection<? extends DynamicExportsDeclaration> additionalOpens,
    @NotNull Collection<? extends DynamicReadsDeclaration> additionalReads)
  {
    Options options = generateOptions(thisModuleName, additionalExports, additionalOpens, additionalReads);

    List<String> result = new ArrayList<>();
    for (Map.Entry<String, Set<String>> openData : options.opens.entrySet()) {
      result.add("--add-opens");
      result.add(openData.getKey() + "=" + String.join(",", openData.getValue()));
    }
    for (Map.Entry<String, Set<String>> openData : options.exports.entrySet()) {
      result.add("--add-exports");
      result.add(openData.getKey() + "=" + String.join(",", openData.getValue()));
    }
    for (Map.Entry<String, Set<String>> openData : options.reads.entrySet()) {
      result.add("--add-reads");
      result.add(openData.getKey() + "=" + String.join(",", openData.getValue()));
    }

    return result;
  }

  public static @NotNull List<JavaDocOption> generateJavaDocOptions(
    @NotNull String thisModuleName,
    @NotNull Collection<? extends DynamicExportsDeclaration> additionalExports,
    @NotNull Collection<? extends DynamicReadsDeclaration> additionalReads)
  {
    Options options = generateOptions(thisModuleName, additionalExports, Collections.emptyList(), additionalReads);

    List<JavaDocOption> result = new ArrayList<>();
    for (Map.Entry<String, Set<String>> openData : options.exports.entrySet()) {
      result.add(new JavaDocOption("-add-exports",
                                   openData.getKey() + "=" + String.join(",", openData.getValue())));
    }
    for (Map.Entry<String, Set<String>> openData : options.reads.entrySet()) {
      result.add(new JavaDocOption("-add-reads",
                                   openData.getKey() + "=" + String.join(",", openData.getValue())));
    }

    return result;
  }

  private static @NotNull Options generateOptions(
    @NotNull String thisModuleName,
    @NotNull Collection<? extends DynamicExportsDeclaration> additionalExports,
    @NotNull Collection<? extends DynamicExportsDeclaration> additionalOpens,
    @NotNull Collection<? extends DynamicReadsDeclaration> additionalReads)
  {
    Map<String, Set<String>> exports = processExports(additionalExports, thisModuleName);
    Map<String, Set<String>> opens   = processExports(additionalOpens, thisModuleName);
    Map<String, Set<String>> reads   = processReads(additionalReads, thisModuleName);

    // The option --add-open implies --add-export. Remove exports that are already handled by opens.
    for (Map.Entry<String, Set<String>> entry : opens.entrySet()) {
      String      source     = entry.getKey();
      Set<String> opendTo    = entry.getValue();
      Set<String> exportedTo = exports.get(source);
      if (exportedTo != null) {
        exportedTo.removeAll(opendTo);
        if (exportedTo.isEmpty()) {
          exports.remove(source);
        }
      }
    }

    return new Options(exports, opens, reads);
  }

  private static @NotNull Map<String, Set<String>> processExports(
    @NotNull Collection<? extends DynamicExportsDeclaration> specs,
    @NotNull String thisModuleName)
  {
    // todo Support ALL-UNNAMED.
    // I want to keep order from specification when suitable. Therefore, I use LinkedHashMap and LinkedHashSet.
    Map<String, Set<String>> result = new LinkedHashMap<>();
    for (DynamicExportsDeclaration exports : specs) {
      for (String packageName : exports.getPackageNames()) {
        String      source  = resolveThis(exports.getSourceModule(), thisModuleName) + "/" + packageName;
        Set<String> targets = result.computeIfAbsent(source, __ -> new LinkedHashSet<>());
        for (String module : exports.getTargetModules()) {
          targets.add(resolveThis(module, thisModuleName));
        }
      }
    }
    return result;
  }

  private static @NotNull Map<String, Set<String>> processReads(
    @NotNull Collection<? extends DynamicReadsDeclaration> specs,
    @NotNull String thisModuleName)
  {
    // todo support for ALL-UNNAMED
    // I want to keep order from specification when suitable. Therefore, I use LinkedHashMap and LinkedHashSet.
    Map<String, Set<String>> result = new LinkedHashMap<>();
    for (DynamicReadsDeclaration reads : specs) {
      for (String affectedModule : reads.getAffectedModules()) {
        String      affectedModuleName = resolveThis(affectedModule, thisModuleName);
        Set<String> requiredModules    = result.computeIfAbsent(affectedModuleName, __ -> new LinkedHashSet<>());
        for (String requiredModule : reads.getRequiredModules()) {
          requiredModules.add(resolveThis(requiredModule, thisModuleName));
        }
      }
    }
    return result;
  }

  private static @NotNull String resolveThis(@NotNull String module, @NotNull String thisModuleName) {
    if (module.equals(THIS)) {
      return thisModuleName;
    }
    else {
      return module;
    }
  }

  private static class Options {
    @NotNull Map<String, Set<String>> exports;
    @NotNull Map<String, Set<String>> opens;
    @NotNull Map<String, Set<String>> reads;

    Options(@NotNull Map<String, Set<String>> exports,
            @NotNull Map<String, Set<String>> opens,
            @NotNull Map<String, Set<String>> reads)
    {
      this.exports = exports;
      this.opens = opens;
      this.reads = reads;
    }
  }
}
