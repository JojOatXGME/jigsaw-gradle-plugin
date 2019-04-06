package de.xgme.jojo.jigsaw_gradle_plugin.extension._impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class DynamicExportsTest {
  @Test
  @DisplayName("equals and hashCode must be implemented")
  void testEqualsAndHashCode() {
    // Methods equals and hashCode must be implemented to make proper up-to-date checks possible.
    DynamicExports obj1 = new DynamicExports(List.of("pkg1", "pkg2"));
    DynamicExports obj2 = new DynamicExports(List.of("pkg1", "pkg2"));
    Assertions.assertAll(
      () -> Assertions.assertEquals(obj1, obj2, "equals"),
      () -> Assertions.assertEquals(obj1.hashCode(), obj2.hashCode(), "hashCode"));
  }
}
