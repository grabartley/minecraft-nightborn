package com.grahambartley.nightborn.client.boss;

import com.grahambartley.nightborn.eldest.EldestSpoils;
import com.grahambartley.nightborn.inquisitor.InquisitorSpoils;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class BossSetTooltip {

  record BossSet(TagKey<Item> blades, TagKey<Item> armor, String bladeKey, String setKey) {

    Optional<String> own(final boolean blade) {
      return blade ? Optional.of(bladeKey) : Optional.empty();
    }

    Optional<String> shared(final boolean blade, final boolean armor) {
      return blade || armor ? Optional.of(setKey) : Optional.empty();
    }
  }

  static final List<BossSet> SETS =
      List.of(
          new BossSet(
              EldestSpoils.SET_BLADES,
              EldestSpoils.SET_ARMOR,
              "nightborn.eldest_blade.tooltip",
              "nightborn.eldest_set.tooltip"),
          new BossSet(
              InquisitorSpoils.SET_BLADES,
              InquisitorSpoils.SET_ARMOR,
              "nightborn.inquisitor_greatsword.tooltip",
              "nightborn.inquisitor_set.tooltip"));

  private BossSetTooltip() {}

  public static void register() {
    ItemTooltipCallback.EVENT.register(
        (stack, context, type, lines) -> {
          for (final BossSet set : SETS) {
            final boolean blade = stack.isIn(set.blades());
            final boolean armor = stack.isIn(set.armor());
            set.own(blade).ifPresent(key -> lines.add(line(key, Formatting.DARK_RED)));
            set.shared(blade, armor).ifPresent(key -> lines.add(line(key, Formatting.GRAY)));
          }
        });
  }

  private static Text line(final String key, final Formatting colour) {
    return Text.translatable(key).formatted(colour);
  }
}
