package de.xgme.jojo.jigsaw_gradle_plugin.util;

import org.gradle.testkit.runner.GradleRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestProject {
  private final @NotNull Path   projectDir;
  private @Nullable      String gradleVersion;

  public TestProject(@NotNull Path projectDir) {
    this.projectDir = projectDir;
  }

  public void useGradleVersion(@Nullable String gradleVersion) {
    this.gradleVersion = gradleVersion;
  }

  public @NotNull Path getRootDir() {
    return projectDir;
  }

  public @NotNull TestProject.ContentWriter createFile(@NotNull String localPath) throws IOException {
    Path path = projectDir.resolve(localPath).normalize();
    Files.createDirectories(path.getParent());
    Files.createFile(path);
    return new ContentWriter(path);
  }

  public @NotNull GradleRunner createRunner() {
    GradleRunner runner = GradleRunner.create()
                                      .withProjectDir(projectDir.toFile())
                                      .withPluginClasspath()
                                      .withDebug(true)
                                      .forwardOutput();
    if (gradleVersion != null) {
      runner = runner.withGradleVersion(gradleVersion);
    }
    return runner;
  }

  public static class ContentWriter {
    private final @NotNull Path path;

    ContentWriter(@NotNull Path path) {
      this.path = path;
    }

    public @NotNull Path getPath() {
      return path;
    }

    public void rawContent(@NotNull String content) throws IOException {
      Files.writeString(path, content, StandardCharsets.UTF_8);
    }
  }
}
