package com.grahambartley.nightborn.tools.structures.nbt;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NbtReader {

  private NbtReader() {}

  public static Map<String, Object> read(final byte[] uncompressed) {
    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(uncompressed))) {
      final int type = in.readUnsignedByte();
      if (type != Nbt.COMPOUND) {
        throw new IllegalArgumentException("The root tag is not a compound but tag " + type);
      }
      name(in);
      return compound(in);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String name(final DataInputStream in) throws IOException {
    final byte[] utf8 = new byte[in.readUnsignedShort()];
    in.readFully(utf8);
    return new String(utf8, StandardCharsets.UTF_8);
  }

  private static Map<String, Object> compound(final DataInputStream in) throws IOException {
    final Map<String, Object> tag = new LinkedHashMap<>();
    int type = in.readUnsignedByte();
    while (type != Nbt.END) {
      final String key = name(in);
      tag.put(key, payload(in, type));
      type = in.readUnsignedByte();
    }
    return tag;
  }

  private static Object payload(final DataInputStream in, final int type) throws IOException {
    return switch (type) {
      case Nbt.BYTE -> in.readByte();
      case Nbt.SHORT -> in.readShort();
      case Nbt.INT -> in.readInt();
      case Nbt.LONG -> in.readLong();
      case Nbt.FLOAT -> in.readFloat();
      case Nbt.DOUBLE -> in.readDouble();
      case Nbt.BYTE_ARRAY -> {
        final byte[] array = new byte[in.readInt()];
        in.readFully(array);
        yield array;
      }
      case Nbt.STRING -> name(in);
      case Nbt.LIST -> {
        final int kind = in.readUnsignedByte();
        final int size = in.readInt();
        final List<Object> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
          items.add(payload(in, kind));
        }
        yield new NbtList(kind, items);
      }
      case Nbt.COMPOUND -> compound(in);
      case Nbt.INT_ARRAY -> {
        final int[] array = new int[in.readInt()];
        for (int i = 0; i < array.length; i++) {
          array[i] = in.readInt();
        }
        yield array;
      }
      case Nbt.LONG_ARRAY -> {
        final long[] array = new long[in.readInt()];
        for (int i = 0; i < array.length; i++) {
          array[i] = in.readLong();
        }
        yield array;
      }
      default -> throw new IllegalArgumentException("Unknown NBT tag " + type);
    };
  }
}
