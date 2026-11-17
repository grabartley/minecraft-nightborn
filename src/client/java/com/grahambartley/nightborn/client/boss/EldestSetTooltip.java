package com.grahambartley.nightborn.client.boss;

import com.grahambartley.nightborn.eldest.EldestSpoils;
import java.util.Optional;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class EldestSetTooltip {

  static final String BLADE = "nightborn.eldest_blade.tooltip";
  static final String SET = "nightborn.eldest_set.tooltip";

  private EldestSetTooltip() {}

  public static void register() {
    ItemTooltipCallback.EVENT.register(
        (stack, context, type, lines) -> {
          final boolean blade = stack.isIn(EldestSpoils.SET_BLADES);
          final boolean armor = stack.isIn(EldestSpoils.SET_ARMOR);
          own(blade).ifPresent(key -> lines.add(line(key, Formatting.DARK_RED)));
          shared(blade, armor).ifPresent(key -> lines.add(line(key, Formatting.GRAY)));
        });
  }

  static Optional<String> own(final boolean blade) {
    return blade ? Optional.of(BLADE) : Optional.empty();
  }

  static Optional<String> shared(final boolean blade, final boolean armor) {
    return blade || armor ? Optional.of(SET) : Optional.empty();
  }

  private static Text line(final String key, final Formatting colour) {
    return Text.translatable(key).formatted(colour);
  }
}
