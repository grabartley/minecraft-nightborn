package com.grahambartley.nightborn.tools.structures.files;

import com.grahambartley.nightborn.tools.structures.nbt.NbtFiles;
import com.grahambartley.nightborn.tools.structures.nbt.NbtWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class OutputTree {

  private final Path root;
  private final Set<String> written = new TreeSet<>();

  public OutputTree(final Path root) {
    this.root = root;
  }

  public Set<String> written() {
    return Set.copyOf(written);
  }

  public void nbt(final String relative, final Map<String, Object> rootTag) {
    write(relative, NbtFiles.compressed(NbtWriter.bytes(rootTag)));
  }

  private void write(final String relative, final byte[] bytes) {
    if (!written.add(relative)) {
      throw new IllegalStateException("Two builders write " + relative);
    }
    final Path file = root.resolve(relative);
    try {
      Files.createDirectories(file.getParent());
      Files.write(file, bytes);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
