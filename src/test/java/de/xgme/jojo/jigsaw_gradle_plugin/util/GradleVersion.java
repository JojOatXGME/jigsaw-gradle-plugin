package de.xgme.jojo.jigsaw_gradle_plugin.util;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(TestProjectExtension.class)
@Inherited
public @interface GradleVersion {
  String value();
}
