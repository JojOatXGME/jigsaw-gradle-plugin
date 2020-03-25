package de.xgme.jojo.jigsaw_gradle_plugin;

import de.xgme.jojo.jigsaw_gradle_plugin._action.*;
import de.xgme.jojo.jigsaw_gradle_plugin._util.TaskUtil;
import de.xgme.jojo.jigsaw_gradle_plugin.extension._impl.TaskExtensionImpl;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.project.BaseProjectExtension;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.jetbrains.annotations.NotNull;

public class JigsawBasePlugin implements Plugin<Project> {
  @Override
  public void apply(@NotNull Project target) {
    // todo Add option to set --illegal-access=deny at runtime.
    // todo Add support for jlink? (https://openjdk.java.net/projects/jigsaw/quick-start)
    // todo Add support for JMOD archives? (https://openjdk.java.net/jeps/261)
    // todo Generate legacy service files from module-info.java.
    // todo Add extension to source sets, allow multiple modules per project.
    // todo Sometimes, some transitive runtime dependencies should stay on the classpath instead of the module path.
    // todo Avoid usage of ALL-MODULE-PATH. Only use it when necessary.
    final Logger logger = Logging.getLogger(JigsawBasePlugin.class);

    BaseProjectExtension projectExtension = target.getExtensions().create("jigsaw", BaseProjectExtension.class);

    target.getTasks().withType(JavaCompile.class, task -> {
      JavaCompileExtension extension = TaskUtil.createExtension(task, JavaCompileExtension.class, "jigsaw",
                                                                TaskExtensionImpl.class, projectExtension);
      task.doFirst(new JavaCompileReconfigurationAction(extension));
    });

    target.getTasks().withType(Test.class, task -> {
      TestExtension extension = TaskUtil.createExtension(task, TestExtension.class, "jigsaw",
                                                         TaskExtensionImpl.class, projectExtension);
      task.doFirst(new TestReconfigurationAction(extension));
    });

    target.getTasks().withType(Javadoc.class, task -> {
      JavadocExtension extension = TaskUtil.createExtension(task, JavadocExtension.class, "jigsaw",
                                                            TaskExtensionImpl.class, projectExtension);
      task.doFirst(new JavadocReconfigurationAction(extension));
    });

    target.getTasks().withType(JavaExec.class, task -> {
      JavaExecExtension extension = TaskUtil.createExtension(task, JavaExecExtension.class, "jigsaw",
                                                             TaskExtensionImpl.class, projectExtension);
      task.doFirst(new JavaExecReconfigurationAction(extension));
    });

    target.getTasks().withType(CreateStartScripts.class, task -> {
      CreateStartScriptsExtension extension = TaskUtil.createExtension(task, CreateStartScriptsExtension.class,
                                                                       "jigsaw",
                                                                       TaskExtensionImpl.class, projectExtension);
      CreateStartScriptsReconfigurationAction doFirst = new CreateStartScriptsReconfigurationAction(extension);
      task.doFirst(doFirst);
      task.doLast(new CreateStartScriptsPostprocessingAction(extension, doFirst));
    });
  }
}
