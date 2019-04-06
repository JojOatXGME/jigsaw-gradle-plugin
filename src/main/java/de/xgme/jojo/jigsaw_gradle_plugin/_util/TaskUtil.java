package de.xgme.jojo.jigsaw_gradle_plugin._util;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.task._property_marker.Input;
import org.gradle.api.Task;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

public final class TaskUtil {
  private TaskUtil() {
    // This class cannot be instantiated.
  }

  public static <T> @NotNull T createExtension(@NotNull Task task,
                                               @NotNull Class<T> publicType,
                                               @NotNull String name,
                                               @NotNull Class<? extends T> instanceType,
                                               Object... constructionArguments)
  {
    List<Method> inputGetters = new ArrayList<>();
    for (Method method : publicType.getMethods()) {
      if (!method.isAnnotationPresent(Input.class)) {
        continue;
      }
      if (method.getParameterCount() != 0) {
        throw new IllegalStateException("Property getters must not have parameters: " +
                                        publicType.getName() + "#" + method.getName());
      }
      if (Modifier.isStatic(method.getModifiers())) {
        throw new IllegalStateException("Property getters must not be static: " +
                                        publicType.getName() + "#" + method.getName());
      }
      inputGetters.add(method);
    }

    T extension = task.getExtensions().create(publicType, name, instanceType, constructionArguments);

    for (Method method : inputGetters) {
      Input annotation = method.getAnnotation(Input.class);
      task.getInputs().property(name + "." + getPropertyName(method),
                                callable(() -> method.invoke(extension)))
          .optional(annotation.optional());
    }
    return extension;
  }

  private static @NotNull String getPropertyName(@NotNull Method getter) {
    String name = getter.getName();
    if (name.startsWith("get")) {
      return decapitalize(name.substring(3));
    }
    else if (name.startsWith("is")) {
      return decapitalize(name.substring(2));
    }
    else {
      return name;
    }
  }

  private static @NotNull String decapitalize(@NotNull String str) {
    return str.substring(0, 1).toLowerCase(Locale.ROOT) + str.substring(1);
  }

  private static <T> @NotNull Callable<T> callable(@NotNull Callable<T> callable) {
    return callable;
  }
}
