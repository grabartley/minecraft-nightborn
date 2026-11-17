package com.grahambartley.nightborn.name;

import java.util.Optional;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public final class NameDisplay {

  private NameDisplay() {}

  public static Text shown(
      final Text name,
      final String epithet,
      final boolean female,
      final Optional<TextColor> colour) {
    final MutableText shown =
        Epithets.given(name.getString(), epithet)
            .<MutableText>map(
                given ->
                    Text.literal(given + " ")
                        .append(
                            Text.translatableWithFallback(Epithets.key(epithet, female), epithet)))
            .orElseGet(name::copy);
    return colour.map(c -> shown.styled(style -> style.withColor(c))).orElse(shown);
  }

  public static Text shown(
      final Text name, final NameBearer bearer, final Optional<TextColor> styleColour) {
    return shown(
        name,
        bearer.epithet(),
        bearer.bearsWomansName(),
        bearer.nameColour().or(() -> styleColour));
  }
}
