package com.grahambartley.nightborn.tools.structures.files;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;

public final class GeneratedSync {

  public record Result(int rewritten, int deleted) {}

  private GeneratedSync() {}

  public static Result apply(
      final Path generated,
      final Collection<String> written,
      final Path target,
      final List<String> ownedFolders) {
    int rewritten = 0;
    for (final String relative : written) {
      final Path ours = generated.resolve(relative);
      final Path theirs = target.resolve(relative);
      if (!Files.exists(theirs) || !SameContent.of(ours, theirs)) {
        copy(ours, theirs);
        rewritten++;
      }
    }
    final List<String> stale = OwnedFiles.stale(target, ownedFolders, written);
    OwnedFiles.delete(target, stale);
    return new Result(rewritten, stale.size());
  }

  private static void copy(final Path from, final Path to) {
    try {
      Files.createDirectories(to.getParent());
      Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
