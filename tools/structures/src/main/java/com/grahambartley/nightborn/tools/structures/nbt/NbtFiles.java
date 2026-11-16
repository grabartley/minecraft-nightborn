package com.grahambartley.nightborn.tools.structures.nbt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class NbtFiles {

  private NbtFiles() {}

  public static byte[] payload(final Path file) {
    try (InputStream in = new GZIPInputStream(Files.newInputStream(file))) {
      return in.readAllBytes();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static byte[] compressed(final byte[] payload) {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try (OutputStream out = new GZIPOutputStream(buffer)) {
      out.write(payload);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
    return buffer.toByteArray();
  }
}
