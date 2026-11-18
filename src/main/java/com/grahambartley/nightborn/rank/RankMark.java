package com.grahambartley.nightborn.rank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public record RankMark(String glyph, TextColor color) {

  public static final int LONGEST_GLYPH = 4;

  public static final Codec<RankMark> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.string(1, LONGEST_GLYPH).fieldOf("glyph").forGetter(RankMark::glyph),
                      TextColor.CODEC.fieldOf("color").forGetter(RankMark::color))
                  .apply(instance, RankMark::new));

  public MutableText text() {
    return Text.literal(glyph).styled(style -> style.withColor(color));
  }
}
