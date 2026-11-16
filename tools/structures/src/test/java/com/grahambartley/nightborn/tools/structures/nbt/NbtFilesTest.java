package com.grahambartley.nightborn.tools.structures.nbt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NbtFilesTest {

  @TempDir Path folder;

  @ParameterizedTest(name = "payload with {0} entries")
  @ValueSource(ints = {0, 1, 50, 4000})
  void readsBackTheCompressedPayload(final int entries) throws IOException {
    final java.util.Map<String, Object> root = Nbt.compound();
    for (int i = 0; i < entries; i++) {
      root.put("k" + i, i);
    }
    final byte[] payload = NbtWriter.bytes(root);
    final Path file = folder.resolve("t.nbt");
    Files.write(file, NbtFiles.compressed(payload));
    assertArrayEquals(payload, NbtFiles.payload(file));
  }
}
