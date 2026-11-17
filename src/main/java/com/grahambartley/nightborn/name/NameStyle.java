package com.grahambartley.nightborn.name;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

public record NameStyle(
    Identifier men,
    Identifier women,
    Optional<Identifier> epithets,
    Optional<TextColor> color,
    boolean gendered) {

  public static final Codec<NameStyle> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("men").forGetter(NameStyle::men),
                      Identifier.CODEC.fieldOf("women").forGetter(NameStyle::women),
                      Identifier.CODEC.optionalFieldOf("epithets").forGetter(NameStyle::epithets),
                      TextColor.CODEC.optionalFieldOf("color").forGetter(NameStyle::color),
                      Codec.BOOL.optionalFieldOf("gendered", true).forGetter(NameStyle::gendered))
                  .apply(instance, NameStyle::new));

  public List<Identifier> poolsFor(final boolean female) {
    return gendered ? List.of(female ? women : men) : List.of(men, women);
  }

  public boolean kin(final NameStyle other) {
    return men.equals(other.men) && women.equals(other.women);
  }
}
