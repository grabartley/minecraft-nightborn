package com.grahambartley.nightborn.tools.structures.nbt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NbtFilesTest {

  @TempDir Path folder;

  @Test
  void readsBackTheCompressedPayload() throws IOException {
    final byte[] payload = NbtWriter.bytes(Nbt.compound("x", 1));
    final Path file = folder.resolve("t.nbt");
    Files.write(file, NbtFiles.compressed(payload));
    assertArrayEquals(payload, NbtFiles.payload(file));
  }
}
