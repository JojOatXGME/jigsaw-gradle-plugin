package de.xgme.jojo.jigsaw_gradle_plugin;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.BaseProjectExtension;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.BasicTaskExtension;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.CompileTaskExtension;
import de.xgme.jojo.jigsaw_gradle_plugin.extension.util.JavaDocOption;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.external.javadoc.MinimalJavadocOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;

public class JigsawBasePlugin implements Plugin<Project> {
  @Override
  public void apply(@NotNull Project target) {
    // todo How do extensions in tasks work together with caches?
    // todo Add option to set --illegal-access=deny at runtime.
    // todo Add support for jlink? (https://openjdk.java.net/projects/jigsaw/quick-start)
    // todo Set --patch-module by inspecting source sets.
    final Logger logger = Logging.getLogger(JigsawBasePlugin.class);

    BaseProjectExtension projectExtension = target.getExtensions().create("jigsaw", BaseProjectExtension.class);

    target.getTasks().withType(JavaCompile.class, task -> {
      // todo Add option --module to command line?
      CompileTaskExtension extension = task.getExtensions().create("jigsaw", CompileTaskExtension.class,
                                                                   projectExtension);
      task.doFirst(__ -> {
        if (extension.isEnabled()) {
          String moduleName    = extension.getModuleName();
          String moduleVersion = extension.getModuleVersion();
          if (moduleName == null) {
            moduleName = findModuleNameFromSource(task.getSource());
          }
          task.getOptions().setCompilerArgs(concat(
            task.getOptions().getCompilerArgs(),
            List.of("--module-path", task.getClasspath().getAsPath(),
                    "--patch-module", moduleName + "=" + task.getSource().getAsPath()), // todo Should not be necessary.
            moduleVersion == null ? Collections.emptyList() : List.of("--module-version", moduleVersion),
            extension.getModuleInfoAdditionsAsCommandLineArguments(moduleName)));
          task.setClasspath(target.files()); // todo "pre-compiled" files should be passed here.
        }
      });
    });

    // todo Add support for JMOD archives? (https://openjdk.java.net/jeps/261)

    // todo Check compilerTestJava (probably also JavaCompile.class).

    target.getTasks().withType(Test.class, task -> {
      BasicTaskExtension extension = task.getExtensions().create("jigsaw", BasicTaskExtension.class,
                                                                 projectExtension);
      task.doFirst(__ -> {
        if (extension.isEnabled()) {
          String moduleName = extension.getModuleName();
          if (moduleName == null) {
            // todo How to detect module name?
          }
          task.jvmArgs("--module-path", task.getClasspath().getAsPath(),
                       "--patch-module", moduleName + "=" + task.getTestClassesDirs().getAsPath(), // todo does it work?
                       "--add-modules", "ALL-MODULE-PATH");
          task.jvmArgs(extension.getModuleInfoAdditionsAsCommandLineArguments(moduleName));
          task.setClasspath(target.files());
        }
      });
    });

    target.getTasks().withType(Javadoc.class, task -> {
      BasicTaskExtension extension = task.getExtensions().create("jigsaw", BasicTaskExtension.class,
                                                                 projectExtension);
      task.doFirst(__ -> {
        if (extension.isEnabled()) {
          MinimalJavadocOptions options = task.getOptions();
          if (options instanceof CoreJavadocOptions) {
            String moduleName = extension.getModuleName();
            if (moduleName == null) {
              moduleName = findModuleNameFromSource(task.getSource());
            }
            // todo Is --patch-module option required for JavaDoc generation in some cases?
            ((CoreJavadocOptions) options).addStringOption("-module-path", task.getClasspath().getAsPath());
            for (JavaDocOption option : extension.getModuleInfoAdditionsAsJavaDocOptions(moduleName)) {
              ((CoreJavadocOptions) options).addStringOption(option.getName(), option.getValue());
            }
            task.setClasspath(target.files());
          }
          else {
            logger.warn("Unknown type of Javadoc options. Cannot set module path.");
          }
        }
      });
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
      task.doFirst(__ -> {
        if (extension.isEnabled()) {
          String moduleName = extension.getModuleName();
          String mainClass  = task.getMain();
          if (moduleName == null) {
            // todo How to detect module name? Might not always be required.
          }
          if (mainClass != null && !mainClass.contains("/")) {
            mainClass = moduleName + "/" + mainClass;
          }
          // todo A --patch-module option might be required in some cases.
          task.jvmArgs("--module-path", task.getClasspath().getAsPath());
          if (mainClass != null) {
            task.jvmArgs("--module", mainClass);
          }
          task.jvmArgs(extension.getModuleInfoAdditionsAsCommandLineArguments(moduleName));
          task.setClasspath(target.files());
          // todo Should task.setMain(null) be called?
        }
      });
    });

    target.getTasks().withType(CreateStartScripts.class, task -> {
      BasicTaskExtension extension = task.getExtensions().create("jigsaw", BasicTaskExtension.class,
                                                                 projectExtension);
      task.doFirst(__ -> {
        if (extension.isEnabled()) {
          String moduleName = extension.getModuleName();
          String mainClass  = task.getMainClassName();
          if (moduleName == null) {
            // todo How to detect module name? Might not always be required.
          }
          if (mainClass != null && !mainClass.contains("/")) {
            mainClass = moduleName + "/" + mainClass;
          }
          task.setDefaultJvmOpts(concat(task.getDefaultJvmOpts(),
                                        List.of("--module-path", "APP_HOME_LIBS_PLACEHOLDER"),
                                        mainClass == null ? Collections.emptyList() : List.of("--module", mainClass),
                                        extension.getModuleInfoAdditionsAsCommandLineArguments(moduleName)));
          task.setClasspath(target.files());
          // todo Should task.setMainClassName(null) be called?
        }
      });
      task.doLast(__ -> {
        if (extension.isEnabled()) {
          String applicationName = task.getApplicationName();
          if (applicationName == null) {
            throw new GradleException("Application name is null in task " + task.getPath());
          }
          try {
            File   bashFile    = new File(task.getOutputDir(), applicationName);
            String bashContent = read(bashFile);
            write(bashFile, bashContent.replaceFirst("APP_HOME_LIBS_PLACEHOLDER",
                                                     Matcher.quoteReplacement("$APP_HOME/lib")));
            File   batFile    = new File(task.getOutputDir(), applicationName + ".bat");
            String batContent = read(batFile);
            write(batFile, batContent.replaceFirst("APP_HOME_LIBS_PLACEHOLDER",
                                                   Matcher.quoteReplacement("%APP_HOME%\\lib")));
          }
          catch (IOException e) {
            throw new GradleException("Could not modify start scripts for " + task.getPath(), e);
          }
        }
      });
    });
  }

  @SafeVarargs
  private static <T> @NotNull List<T> concat(@Nullable Iterable<? extends T>... iterables) {
    List<T> result = new ArrayList<>();
    if (iterables == null) {
      return result;
    }
    for (Iterable<? extends T> iterable : iterables) {
      if (iterable == null) {
        continue;
      }
      for (T element : iterable) {
        result.add(element);
      }
    }
    return result;
  }

  private static @NotNull String read(@NotNull File file) throws IOException {
    return Files.readString(file.toPath(), StandardCharsets.UTF_8);
  }

  private static void write(@NotNull File file, @NotNull String content) throws IOException {
    Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
  }

  private static @NotNull String findModuleNameFromSource(@NotNull FileTree sources) {
    Set<File> moduleInfoSet = sources.matching(filter -> filter.include("module-info.java")).getFiles();
    if (moduleInfoSet.isEmpty()) {
      throw new GradleException("No module-info.java could be found in " + sources.getAsPath());
    }
    if (moduleInfoSet.size() > 1) {
      throw new GradleException("Multiple files named module-info.java found: " + moduleInfoSet);
    }
    // todo How to extract the information from module-info.java?
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
