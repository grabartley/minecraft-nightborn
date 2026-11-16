package com.grahambartley.nightborn.tools.structures.files;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ShippedDiff {

  private ShippedDiff() {}

  public static List<String> differences(
      final Path generated,
      final Set<String> written,
      final Path shipped,
      final List<String> ownedFolders) {
    final List<String> found = new ArrayList<>();
    for (final String relative : written) {
      final Path theirs = shipped.resolve(relative);
      if (!Files.exists(theirs)) {
        found.add("not shipped: " + relative);
      } else if (!SameContent.of(generated.resolve(relative), theirs)) {
        found.add("differs: " + relative);
      }
    }
    for (final String relative : OwnedFiles.stale(shipped, ownedFolders, written)) {
      found.add("not generated: " + relative);
    }
    return found;
  }
}
