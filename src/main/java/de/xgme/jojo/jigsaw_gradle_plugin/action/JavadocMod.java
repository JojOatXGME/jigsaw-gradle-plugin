package de.xgme.jojo.jigsaw_gradle_plugin.action;

import de.xgme.jojo.jigsaw_gradle_plugin.action.util.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin.action.util.SourceUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.JavadocExtension;
import de.xgme.jojo.jigsaw_gradle_plugin.action.util.JavaDocOption;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.external.javadoc.MinimalJavadocOptions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Callable;

public final class JavadocMod {

  private JavadocMod() {
    // This class cannot be instantiated.
  }

  public static void apply(@NotNull Javadoc task, @NotNull JavadocExtension extension) {
    task.doFirst(new ReconfigurationAction(extension));
    task.getInputs().property("jigsaw.enabled", callable(extension::isEnabled));
    task.getInputs().property("jigsaw.moduleName", callable(extension::getModuleName)).optional(true);
  }

  private static <T> @NotNull Callable<T> callable(@NotNull Callable<T> callable) {
    return callable;
  }

  private static class ReconfigurationAction implements Action<Task> {
    private @NotNull JavadocExtension extension;

    private ReconfigurationAction(@NotNull JavadocExtension extension) {
      this.extension = extension;
    }

    @Override
    public void execute(@NotNull Task looselyTypedTask) {
      if (!(looselyTypedTask instanceof Javadoc)) {
        throw new IllegalStateException("This action must only be used on tasks of type Javadoc");
      }
      if (!extension.isEnabled()) {
        return;
      }

      Javadoc               task    = (Javadoc) looselyTypedTask;
      MinimalJavadocOptions options = task.getOptions();
      if (options instanceof CoreJavadocOptions) {
        String moduleName = extension.getModuleName();
        if (moduleName == null) {
          moduleName = SourceUtil.findModuleNameFromSource(task.getSource());
        }
        List<JavaDocOption> dynamicModuleOptions = OptionGenerator.generateJavaDocOptions(moduleName,
                                                                                          extension.getExports(),
                                                                                          extension.getReads());
        // todo Is --patch-module option required for JavaDoc generation in some cases?
        ((CoreJavadocOptions) options).addStringOption("-module-path", task.getClasspath().getAsPath());
        for (JavaDocOption option : dynamicModuleOptions) {
          ((CoreJavadocOptions) options).addStringOption(option.getName(), option.getValue());
        }
        task.setClasspath(task.getProject().files());
      }
      else {
        task.getLogger().warn("jigsaw: Unknown type of Javadoc options. Cannot set module path.");
      }
    }
  }
}
