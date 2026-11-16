package com.grahambartley.nightborn.tools.structures.files;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public final class OwnedFiles {

  private OwnedFiles() {}

  public static List<String> list(final Path root, final Collection<String> folders) {
    final List<String> found = new ArrayList<>();
    for (final String folder : folders) {
      final Path dir = root.resolve(folder);
      if (!Files.isDirectory(dir)) {
        continue;
      }
      try (Stream<Path> walk = Files.walk(dir)) {
        walk.filter(Files::isRegularFile)
            .map(file -> root.relativize(file).toString().replace('\\', '/'))
            .sorted()
            .forEach(found::add);
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return found;
  }

  public static List<String> stale(
      final Path root, final Collection<String> folders, final Collection<String> written) {
    return list(root, folders).stream().filter(file -> !written.contains(file)).toList();
  }

  public static void delete(final Path root, final Collection<String> files) {
    for (final String file : files) {
      try {
        Files.delete(root.resolve(file));
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }
}
