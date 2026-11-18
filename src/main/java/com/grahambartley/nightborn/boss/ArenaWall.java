package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.HexFormat;
import java.util.List;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record ArenaWall(
    Identifier fog, Identifier shapes, List<Integer> palette, int beatTicks, boolean breathes) {

  static final Codec<Integer> COLOUR =
      Codec.STRING.comapFlatMap(ArenaWall::colour, value -> String.format("#%06X", value));

  public static final Codec<ArenaWall> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("fog").forGetter(ArenaWall::fog),
                      Identifier.CODEC.fieldOf("shapes").forGetter(ArenaWall::shapes),
                      COLOUR
                          .listOf()
                          .validate(
                              colours ->
                                  colours.isEmpty()
                                      ? DataResult.error(() -> "An arena wall needs a colour")
                                      : DataResult.success(colours))
                          .fieldOf("palette")
                          .forGetter(ArenaWall::palette),
                      Codec.intRange(4, 1200).fieldOf("beat_ticks").forGetter(ArenaWall::beatTicks),
                      Codec.BOOL.optionalFieldOf("breathes", false).forGetter(ArenaWall::breathes))
                  .apply(instance, ArenaWall::new));

  public static final PacketCodec<ByteBuf, ArenaWall> PACKET_CODEC = PacketCodecs.codec(CODEC);

  public ArenaWall {
    palette = List.copyOf(palette);
  }

  static DataResult<Integer> colour(final String text) {
    final String digits = text.startsWith("#") ? text.substring(1) : text;
    if (digits.length() != 6) {
      return DataResult.error(() -> "A colour is six hex digits: " + text);
    }
    try {
      return DataResult.success(HexFormat.fromHexDigits(digits));
    } catch (final IllegalArgumentException wrong) {
      return DataResult.error(() -> "A colour is six hex digits: " + text);
    }
  }
}
