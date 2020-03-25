package de.xgme.jojo.jigsaw_gradle_plugin._action;

import de.xgme.jojo.jigsaw_gradle_plugin._util.JavaDocOption;
import de.xgme.jojo.jigsaw_gradle_plugin._util.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin._util.ModuleUtil;
import de.xgme.jojo.jigsaw_gradle_plugin._util.SourceSetUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.JavadocExtension;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class JavadocReconfigurationAction implements Action<Task> {
  private final @NotNull JavadocExtension extension;

  public JavadocReconfigurationAction(@NotNull JavadocExtension extension) {
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

    Javadoc task = (Javadoc) looselyTypedTask;

    if (!(task.getOptions() instanceof CoreJavadocOptions)) {
      throw new GradleException("jigsaw: Unknown type of Javadoc options. Cannot set module path.");
    }

    CoreJavadocOptions options           = (CoreJavadocOptions) task.getOptions();
    String             moduleName        = extension.getModuleName();
    FileCollection     localClasspath    = SourceSetUtil.getLocalClasspath(task.getProject(), task.getClasspath());
    FileCollection     modulePath        = task.getClasspath().minus(localClasspath);
    List<String>       dependencyModules = ModuleUtil.findModuleNames(modulePath);

    if (moduleName == null) {
      moduleName = ModuleUtil.findModuleNameFromSource(task.getSource());
    }

    List<JavaDocOption> dynamicModuleOptions = OptionGenerator.generateJavaDocOptions(moduleName,
                                                                                      extension.getExports(),
                                                                                      extension.getReads());
    options.addStringOption("-module-path", modulePath.getAsPath());
    if (!dependencyModules.isEmpty()) {
      options.addStringOption("-add-modules", String.join(",", dependencyModules));
    }
    for (JavaDocOption option : dynamicModuleOptions) {
      options.addStringOption(option.getName(), option.getValue());
    }
    task.setClasspath(localClasspath);
  }
}
