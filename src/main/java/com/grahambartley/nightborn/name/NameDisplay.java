package com.grahambartley.nightborn.name;

import java.util.Optional;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public final class NameDisplay {

  public static final String WITH_EPITHET = "nightborn.name.with_epithet";

  private NameDisplay() {}

  public static Text shown(
      final Text name,
      final String epithet,
      final boolean female,
      final Optional<TextColor> color) {
    final MutableText shown =
        Epithets.given(name.getString(), epithet)
            .<MutableText>map(
                given ->
                    Text.translatableWithFallback(
                        WITH_EPITHET,
                        given + " " + epithet,
                        Text.literal(given),
                        Text.translatableWithFallback(Epithets.key(epithet, female), epithet)))
            .orElseGet(name::copy);
    return color.map(c -> shown.styled(style -> style.withColor(c))).orElse(shown);
  }

  public static Text shown(
      final Text name, final NameBearer bearer, final Optional<TextColor> styleColor) {
    return shown(
        name, bearer.epithet(), bearer.bearsWomansName(), bearer.nameColor().or(() -> styleColor));
  }
}
