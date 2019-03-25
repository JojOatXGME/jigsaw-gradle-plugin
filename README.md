Jigsaw Gradle Plugin (WIP)
==========================

Introduction
------------

This project is intended to provide tools for simply building Java
modules with Gradle. The module system of Java, called Jigsaw, has been
introduced with Java 9. A major motivation behind this project is to
gain experience with Gradle and the module system of Java. The project
shall provide three plugins for Gradle.

 *  **de.xgme.jojo.jigsaw-base**
 *  **de.xgme.jojo.jigsaw**
 *  **de.xgme.jojo.jigsaw-generator**

Base Plugin
-----------

The base plugin **de.xgme.jojo.jigsaw-base** does not affect the build
by default. However, the plugin adds various options which can affect
the build.

```groovy
plugins {
    id 'de.xgme.jojo.jigsaw-base' version '0.1-SNAPSHOT'
}
```

Convention Plugin
-----------------

The convention plugin **de.xgme.jojo.jigsaw** applies the base plugin
and the [Java plugin][java-plugin]. It sets some options by convention.
For most projects, this plugin shall enable the module system of Java
with minimal configuration.

```groovy
plugins {
    id 'de.xgme.jojo.jigsaw' version '0.1-SNAPSHOT'
}
```

Generator Plugin
----------------

The generator plugin **de.xgme.jojo.jigsaw-generator** is intended to
generate the *module-info* from the Gradle configuration. This avoids
duplication of dependency declaration. The generator plugin also applies
the convention plugin.

```groovy
plugins {
    id 'de.xgme.jojo.jigsaw-generator' version '0.1-SNAPSHOT'
}
```


[java-plugin]:
<https://docs.gradle.org/current/userguide/java_plugin.html>
"Gradle Userguide: The Java Plugi"
