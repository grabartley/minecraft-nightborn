package com.grahambartley.nightborn.tools.structures.nbt;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class NbtWriter {

  private NbtWriter() {}

  public static byte[] bytes(final Map<String, Object> root) {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try (DataOutputStream out = new DataOutputStream(buffer)) {
      out.writeByte(Nbt.COMPOUND);
      name(out, "");
      payload(out, root);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
    return buffer.toByteArray();
  }

  private static void name(final DataOutputStream out, final String name) throws IOException {
    final byte[] utf8 = name.getBytes(StandardCharsets.UTF_8);
    out.writeShort(utf8.length);
    out.write(utf8);
  }

  private static void payload(final DataOutputStream out, final Object value) throws IOException {
    switch (value) {
      case Map<?, ?> compound -> {
        for (final Map.Entry<?, ?> entry : compound.entrySet()) {
          out.writeByte(Nbt.tagId(entry.getValue()));
          name(out, (String) entry.getKey());
          payload(out, entry.getValue());
        }
        out.writeByte(Nbt.END);
      }
      case NbtList list -> {
        out.writeByte(list.kind());
        out.writeInt(list.items().size());
        for (final Object item : list.items()) {
          payload(out, item);
        }
      }
      case String text -> name(out, text);
      case Double number -> out.writeDouble(number);
      case Float number -> out.writeFloat(number);
      case Long number -> out.writeLong(number);
      case Short number -> out.writeShort(number);
      case Byte number -> out.writeByte(number);
      case Boolean flag -> out.writeByte(flag ? 1 : 0);
      case Integer number -> out.writeInt(number);
      case byte[] array -> {
        out.writeInt(array.length);
        out.write(array);
      }
      case int[] array -> {
        out.writeInt(array.length);
        for (final int item : array) {
          out.writeInt(item);
        }
      }
      case long[] array -> {
        out.writeInt(array.length);
        for (final long item : array) {
          out.writeLong(item);
        }
      }
      default -> throw new IllegalArgumentException("Not an NBT value: " + value);
    }
  }
}
