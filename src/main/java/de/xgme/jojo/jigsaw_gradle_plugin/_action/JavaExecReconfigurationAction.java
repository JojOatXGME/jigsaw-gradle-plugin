package de.xgme.jojo.jigsaw_gradle_plugin._action;

import de.xgme.jojo.jigsaw_gradle_plugin._util.ListUtil;
import de.xgme.jojo.jigsaw_gradle_plugin._util.OptionGenerator;
import de.xgme.jojo.jigsaw_gradle_plugin._util.SourceSetUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.JavaExecExtension;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.JavaExec;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

public final class JavaExecReconfigurationAction implements Action<Task> {
  private @NotNull JavaExecExtension extension;

  public JavaExecReconfigurationAction(@NotNull JavaExecExtension extension) {
    this.extension = extension;
  }

  @Override
  public void execute(@NotNull Task looselyTypedTask) {
    if (!(looselyTypedTask instanceof JavaExec)) {
      throw new IllegalStateException("This action must only be used on tasks of type JavaExec");
    }
    if (!extension.isEnabled()) {
      return;
    }

    JavaExec       task           = (JavaExec) looselyTypedTask;
    FileCollection modulePath     = task.getClasspath();
    FileCollection localClasspath = SourceSetUtil.getLocalClasspath(task.getProject(), task.getClasspath());
    String         moduleName     = extension.getModuleName();
    String         mainClass      = task.getMain();

    if (moduleName == null) {
      // todo How to detect module name? Might not always be required.
      throw new UnsupportedOperationException("Not Implemented yet");
    }
    if (mainClass == null) {
      throw new GradleException("No main class specified");
    }
    if (!mainClass.contains("/")) {
      mainClass = moduleName + "/" + mainClass;
    }

    task.setClasspath(task.getProject().files());
    task.jvmArgs("--module-path", modulePath.getAsPath());
    task.jvmArgs(OptionGenerator.generateArguments(moduleName,
                                                   extension.getExports(), extension.getOpens(),
                                                   extension.getReads()));
    task.jvmArgs("--patch-module", moduleName + "=" + localClasspath.getAsPath());
    task.jvmArgs("--add-modules", "ALL-MODULE-PATH");

    // Since there are implicit arguments appended to the end of jvmArgs, "--module" cannot be specified at the end of
    // `jvmArgs`. Therefore, the old `mainClass` has to be replaced with "--module" and the actual `mainClass` is
    // injected at the beginning of `args`.
    // Old command line: java <jvmArgs...> <mainClass> <args...>
    // New command line: java <jvmArgs...> --module <mainClass> <args...>
    task.setMain("--module");
    task.setArgs(ListUtil.concat(
      List.of(mainClass),
      task.getArgs()
    ));

    // The field `__main__` represents the isExplicitValueField flag of the property "main". This field is injected by
    // the decorator for convention mappings of Gradle. See ClassBuilderImpl.applyConventionMappingToGetter,
    // ClassBuilderImpl.applyConventionMappingToSetter and ConventionAwareHelper.getConventionValue. When the value of
    // the field is false, Gradle will use the default value instead of the actual value. According to my knowledge,
    // task.setMain(...) should rather set the field to `true` but it sometimes doesn't. (It seems not to be
    // deterministic for some reason.) We will set the field manually as a workaround.
    try {
      Field mainIsExplicitValueField = task.getClass().getDeclaredField("__main__");
      mainIsExplicitValueField.setAccessible(true);
      mainIsExplicitValueField.setBoolean(task, true);
    }
    catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
    catch (NoSuchFieldException e) {
      // If the field does not exist, the related implementation in Gradle did probably change. Then, the bug does
      // hopefully not occur anymore.
    }
  }
}
