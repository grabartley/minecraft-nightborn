package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.faith.Flame;
import com.grahambartley.nightborn.faith.Kindling;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.SharedConstants;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;

public final class FlameTooltip {

  static final int COLOUR = 0xC0392B;

  private FlameTooltip() {}

  public static void register() {
    ItemTooltipCallback.EVENT.register(
        (stack, context, type, lines) ->
            Kindling.burning(stack, LiveCoating.now())
                .ifPresent(
                    flame ->
                        lines.addAll(Math.min(1, lines.size()), lines(flame, LiveCoating.now()))));
  }

  static List<Text> lines(final Flame flame, final long now) {
    return List.of(
        Text.translatable("nightborn.blood_fire.alight")
            .styled(style -> style.withColor(TextColor.fromRgb(COLOUR))),
        Text.translatable(
                "nightborn.coating.left.time",
                StringHelper.formatTicks(
                    (int) flame.ticksLeft(now), SharedConstants.TICKS_PER_SECOND))
            .formatted(Formatting.GRAY));
  }
}
