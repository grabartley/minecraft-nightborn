package com.grahambartley.nightborn.tools.structures.files;

import com.grahambartley.nightborn.tools.structures.nbt.NbtFiles;
import java.nio.file.Path;
import java.util.Arrays;

public final class SameContent {

  private SameContent() {}

  public static boolean of(final Path ours, final Path theirs) {
    return Arrays.equals(NbtFiles.payload(ours), NbtFiles.payload(theirs));
  }
}
