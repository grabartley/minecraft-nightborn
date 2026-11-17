package com.grahambartley.nightborn.name;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public record NameStyle(
    Identifier men, Identifier women, Optional<Identifier> epithets, Optional<TextColor> colour) {

  public static final NameStyle VAMPIRE =
      new NameStyle(
          pool("vampire_men"),
          pool("vampire_women"),
          Optional.of(pool("vampire_epithets")),
          Optional.empty());
  public static final NameStyle ELDEST =
      new NameStyle(
          pool("vampire_men"),
          pool("vampire_women"),
          Optional.of(pool("eldest_epithets")),
          Optional.empty());
  public static final NameStyle HUNTER =
      new NameStyle(
          pool("hunter_men"),
          pool("hunter_women"),
          Optional.empty(),
          Optional.of(TextColor.fromFormatting(Formatting.BLUE)));
  public static final NameStyle FAMILIAR =
      new NameStyle(
          pool("familiar_men"), pool("familiar_women"), Optional.empty(), Optional.empty());

  public boolean kin(final NameStyle other) {
    return men.equals(other.men) && women.equals(other.women);
  }

  private static Identifier pool(final String path) {
    return Identifier.of(Nightborn.MOD_ID, path);
  }
}
