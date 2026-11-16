package com.grahambartley.nightborn.tools.structures.files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SameContentTest {

  @TempDir Path folder;

  private static byte[] gzip(final byte[] payload, final int level) throws IOException {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (GZIPOutputStream out =
        new GZIPOutputStream(bytes) {
          {
            def.setLevel(level);
          }
        }) {
      out.write(payload);
    }
    return bytes.toByteArray();
  }

  @ParameterizedTest(name = "x={0} at level {1} against x={2} at level {3}: same {4}")
  @CsvSource({"1, 1, 1, 9, true", "1, 6, 1, 6, true", "1, 9, 2, 9, false", "3, 0, 4, 9, false"})
  void templatesMatchByPayloadWhateverTheirCompression(
      final int ourValue,
      final int ourLevel,
      final int theirValue,
      final int theirLevel,
      final boolean same)
      throws IOException {
    final Path ours = folder.resolve("a.nbt");
    final Path theirs = folder.resolve("b.nbt");
    Files.write(ours, gzip(NbtWriter.bytes(Nbt.compound("x", ourValue)), ourLevel));
    Files.write(theirs, gzip(NbtWriter.bytes(Nbt.compound("x", theirValue)), theirLevel));
    assertEquals(same, SameContent.of(ours, theirs));
  }
}
