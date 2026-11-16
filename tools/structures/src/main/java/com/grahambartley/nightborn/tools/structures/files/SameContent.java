package com.grahambartley.nightborn.tools.structures.files;

import com.grahambartley.nightborn.tools.structures.nbt.NbtFiles;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class SameContent {

  private SameContent() {}

  public static boolean of(final Path ours, final Path theirs) {
    if (ours.toString().endsWith(".nbt")) {
      return Arrays.equals(NbtFiles.payload(ours), NbtFiles.payload(theirs));
    }
    try {
      return Arrays.equals(Files.readAllBytes(ours), Files.readAllBytes(theirs));
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
