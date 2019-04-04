package de.xgme.jojo.jigsaw_gradle_plugin;

import de.xgme.jojo.jigsaw_gradle_plugin.action.*;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.BaseProjectExtension;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.BasicTaskExtension;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.CompileTaskExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.jetbrains.annotations.NotNull;

public class JigsawBasePlugin implements Plugin<Project> {
  @Override
  public void apply(@NotNull Project target) {
    // todo Add option to set --illegal-access=deny at runtime.
    // todo Add support for jlink? (https://openjdk.java.net/projects/jigsaw/quick-start)
    // todo Set --patch-module by inspecting source sets.
    final Logger logger = Logging.getLogger(JigsawBasePlugin.class);

    BaseProjectExtension projectExtension = target.getExtensions().create("jigsaw", BaseProjectExtension.class);

    target.getTasks().withType(JavaCompile.class, task -> {
      CompileTaskExtension extension = task.getExtensions().create("jigsaw", CompileTaskExtension.class,
                                                                   projectExtension);
      JavaCompileMod.apply(task, extension);
    });

    // todo Add support for JMOD archives? (https://openjdk.java.net/jeps/261)
    // todo Check compilerTestJava (probably also JavaCompile.class).

    target.getTasks().withType(Test.class, task -> {
      BasicTaskExtension extension = task.getExtensions().create("jigsaw", BasicTaskExtension.class,
                                                                 projectExtension);
      TestMod.apply(task, extension);
    });

    target.getTasks().withType(Javadoc.class, task -> {
      BasicTaskExtension extension = task.getExtensions().create("jigsaw", BasicTaskExtension.class,
                                                                 projectExtension);
      JavadocMod.apply(task, extension);
    });

    target.getTasks().withType(Jar.class, task -> {
      BasicTaskExtension extension = task.getExtensions().create("jigsaw", BasicTaskExtension.class,
                                                                 projectExtension);
      // todo Add option for --module-version.
      // todo Add option for --hash-modules.
      // todo Add option --main-class=<class>.
    });

    target.getTasks().withType(JavaExec.class, task -> {
      BasicTaskExtension extension = task.getExtensions().create("jigsaw", BasicTaskExtension.class,
                                                                 projectExtension);
      JavaExecMod.apply(task, extension);
    });

    target.getTasks().withType(CreateStartScripts.class, task -> {
      BasicTaskExtension extension = task.getExtensions().create("jigsaw", BasicTaskExtension.class,
                                                                 projectExtension);
      CreateStartScriptsMod.apply(task, extension);
    });
  }
}
