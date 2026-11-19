package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.clothing.Lining;
import java.util.Optional;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class LiningTooltip {

  static final String LINED = "nightborn.lining.tooltip";

  private LiningTooltip() {}

  public static void register() {
    ItemTooltipCallback.EVENT.register(
        (stack, context, type, lines) ->
            line(Lining.lined(stack))
                .ifPresent(
                    key ->
                        lines.add(
                            Math.min(1, lines.size()),
                            Text.translatable(key).formatted(Formatting.GRAY))));
  }

  static Optional<String> line(final boolean lined) {
    return lined ? Optional.of(LINED) : Optional.empty();
  }
}
