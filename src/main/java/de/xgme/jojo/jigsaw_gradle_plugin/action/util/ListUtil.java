package de.xgme.jojo.jigsaw_gradle_plugin.action.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ListUtil {
  private ListUtil() {
    // This class cannot be instantiated.
  }

  @SafeVarargs
  public static <T> @NotNull List<T> concat(@Nullable Iterable<? extends T>... iterables) {
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
}
