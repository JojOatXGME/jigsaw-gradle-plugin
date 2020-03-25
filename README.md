Jigsaw Gradle Plugin (WIP)
==========================

*Note: The project is not finished and this documents does not represent
the current status but the intended functionality.*

Introduction
------------

This project is intended to extend Gradle with support for Java modules.
The module system, also known as Jigsaw, was introduced in Java 9. More
information can be found at [JEP 261][jep261]. The project targets to …

 *  **Integrate well into Gradle.** This basically means that
    functionality of Gradle shall keep working when possible. Avoid
    assumptions about the project to allow customised configurations to
    work. Existing options of Gradle shall be reused whenever possible.

 *  **Generate module-info based on Gradle configuration.** The
    *module-info.java* is partly redundant to the dependency
    configuration in Gradle. Therefore, the project shall provide an
    option to generate the module-info from the Gradle configuration.

 *  **No redundant configuration.** For simple projects, applying the
    right plugin should be enough.

Structure
---------

The functionality shall be provided by three plugins.

 *  **de.xgme.jojo.jigsaw-base**  
    Provides all the functionality but nothing is configured by default.

 *  **de.xgme.jojo.jigsaw**  
    Enables module system. The default configuration should work fine
    for many projects that use the following plugins:
    [*application*][application-plugin],
    [*java-library*][java-library-plugin], [*java*][java-plugin].

 *  **de.xgme.jojo.jigsaw-generator**  
    Extends *de.xgme.jojo.jigsaw* by enabling generation of
    *module-info* from dependency configuration.

Base Plugin
-----------

The base plugin **de.xgme.jojo.jigsaw-base** adds various options for
the module system. However, without further configuration, the plugin
will not affect the build. The plugin is the base for
**de.xgme.jojo.jigsaw** and **de.xgme.jojo.jigsaw-generator**. You can
use it directly for advanced configurations.

```groovy
plugins {
    id 'de.xgme.jojo.jigsaw-base' version '0.1-SNAPSHOT'
}
```

The project extends the following tasks.

 *  **JavaCompile** (eg. `javaCompile` and `testJavaCompile`)
 *  **Test** (eg. `test`)
 *  **Javadoc** (eg. `javadoc`)
 *  **JavaExec** (eg. `run`)
 *  **CreateStartScripts** (eg. `startScripts`)

Convention Plugin
-----------------

The convention plugin **de.xgme.jojo.jigsaw** applies the base plugin
and enables the module system for relevant tasks.

```groovy
plugins {
    id 'de.xgme.jojo.jigsaw' version '0.1-SNAPSHOT'
}
```

Generator Plugin
----------------

The generator plugin **de.xgme.jojo.jigsaw-generator** generates the
*module-info* from the Gradle configuration. This avoids duplication of
dependency declaration. The generator plugin also applies the convention
plugin.

```groovy
plugins {
    id 'de.xgme.jojo.jigsaw-generator' version '0.1-SNAPSHOT'
}
```


[application-plugin]:
  <https://docs.gradle.org/current/userguide/application_plugin.html>
  "Gradle Userguide: The Application Plugin"
[java-library-plugin]:
  <https://docs.gradle.org/current/userguide/java_library_plugin.html>
  "Gradle Userguide: The Java Library Plugin"
[java-plugin]:
  <https://docs.gradle.org/current/userguide/java_plugin.html>
  "Gradle Userguide: The Java Plugin"
[jep261]:
  <https://openjdk.java.net/jeps/261>
  "JEP 261: Module System"
