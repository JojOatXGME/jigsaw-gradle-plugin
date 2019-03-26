package de.xgme.jojo.jigsaw_gradle_plugin.util;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TestProjectExtension implements BeforeEachCallback, ParameterResolver {
  private static final Namespace NAMESPACE          = Namespace.create(TestProjectExtension.class);
  private static final String    TEMP_DIR_PREFIX    = "jigsaw-gradle-plugin-test";
  private static final String    GRADLE_VERSION_KEY = "gradle-version";

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    AnnotationSupport.findAnnotation(context.getTestClass(), GradleVersion.class).ifPresent(
      annotation -> context.getStore(NAMESPACE).put(GRADLE_VERSION_KEY, annotation.value()));
    AnnotationSupport.findAnnotation(context.getTestMethod(), GradleVersion.class).ifPresent(
      annotation -> context.getStore(NAMESPACE).put(GRADLE_VERSION_KEY, annotation.value()));

    List<Field> candidates = ReflectionSupport.findFields(context.getRequiredTestClass(),
                                                          field -> TestProject.class.equals(field.getType()),
                                                          HierarchyTraversalMode.BOTTOM_UP);
    for (Field field : candidates) {
      if (Modifier.isPrivate(field.getModifiers())) {
        throw new ExtensionConfigurationException("TestProject field [" + field + "] must not be private.");
      }
      if (Modifier.isStatic(field.getModifiers())) {
        throw new ExtensionConfigurationException("TestProject filed [" + field + "] must not be static.");
      }
      field.trySetAccessible();
      field.set(context.getRequiredTestInstance(), createTestProject(context));
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
    throws ParameterResolutionException
  {
    return TestProject.class.equals(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
    throws ParameterResolutionException
  {
    return createTestProject(extensionContext);
  }

  private static @NotNull TestProject createTestProject(@NotNull ExtensionContext context) {
    TestProject testProject = new TestProject(createTempDirectory(context));
    getFromStore(context, GRADLE_VERSION_KEY, String.class).ifPresent(testProject::useGradleVersion);
    return testProject;
  }

  private static <V> @NotNull Optional<V> getFromStore(@NotNull ExtensionContext context,
                                                       @NotNull Object key, @NotNull Class<V> requiredType)
  {
    return Optional.ofNullable(context.getStore(NAMESPACE).get(key, requiredType));
  }

  private static @NotNull Path createTempDirectory(@NotNull ExtensionContext context) {
    try {
      Path projectDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
      // The TempDirDeletionGuard is put into the store to trigger deletion after the test.
      context.getStore(NAMESPACE).put(projectDir, new TempDirDeletionGuard(projectDir));
      return projectDir;
    }
    catch (IOException e) {
      throw new ExtensionConfigurationException("Failed to create temp directory", e);
    }
  }

  private static class TempDirDeletionGuard implements CloseableResource {
    private final Path projectDir;

    TempDirDeletionGuard(Path projectDir) {
      this.projectDir = projectDir;
    }

    @Override
    public void close() throws Throwable {
      List<IOException> failures = new LinkedList<>();
      Files.walkFileTree(projectDir, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try {
            Files.delete(file);
          }
          catch (IOException e) {
            failures.add(e);
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          try {
            Files.delete(dir);
          }
          catch (IOException e) {
            failures.add(e);
          }
          return FileVisitResult.CONTINUE;
        }
      });
      if (!failures.isEmpty()) {
        IOException exception = new IOException("Failed to delete temp directory " + projectDir.toAbsolutePath());
        failures.forEach(exception::addSuppressed);
        throw exception;
      }
    }
  }
}
