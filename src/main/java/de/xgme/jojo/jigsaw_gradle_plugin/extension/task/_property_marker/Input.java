package de.xgme.jojo.jigsaw_gradle_plugin.extension.task._property_marker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Input {
  boolean optional() default false;
}
