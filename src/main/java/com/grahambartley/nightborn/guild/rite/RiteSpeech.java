package com.grahambartley.nightborn.guild.rite;

import java.util.Optional;
import net.minecraft.text.Text;

public final class RiteSpeech {

  static final String KEY = "nightborn.guild.rite.";

  public enum Line {
    BEGUN,
    NONE,
    UNSAFE,
    BROKEN,
    BUSY;

    public Text by(final Text teller) {
      return Text.translatable(KEY + name().toLowerCase(java.util.Locale.ROOT), teller);
    }
  }

  private RiteSpeech() {}

  public static Optional<Text> refused(
      final RiteAccess.Refusal refusal, final Text teller, final int price, final long wait) {
    return switch (refusal) {
      case NONE -> Optional.empty();
      case WAIT -> Optional.of(Text.translatable(KEY + "wait", teller, RiteAccess.clock(wait)));
      case PRICE -> Optional.of(Text.translatable(KEY + "price", teller, price));
      case RANK -> Optional.of(Text.translatable(KEY + "rank", teller));
    };
  }
}
