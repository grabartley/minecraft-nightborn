package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.coating.Coat;
import com.grahambartley.nightborn.coating.CoatingTerms;
import com.grahambartley.nightborn.coating.Coatings;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.SharedConstants;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;

public final class CoatingTooltip {

  static final int COLOUR = 0xB8C4CC;
  static final String BLESSED = "nightborn.coating.blessed";

  private CoatingTooltip() {}

  public static void register() {
    ItemTooltipCallback.EVENT.register(
        (stack, context, type, lines) ->
            LiveCoating.of(stack)
                .ifPresent(
                    coat ->
                        lines.addAll(
                            Math.min(1, lines.size()),
                            lines(
                                coat,
                                CoatingTerms.of(coat.coating(), SyncedServerConfig.get(), stack),
                                LiveCoating.now(),
                                stack.isIn(Coatings.AMMO)))));
  }

  static List<Text> lines(
      final Coat coat, final CoatingTerms terms, final long now, final boolean ammo) {
    final Text name =
        Text.translatable(coat.blessed() ? BLESSED : coat.coating().translationKey())
            .styled(style -> style.withColor(TextColor.fromRgb(COLOUR)));
    if (ammo) {
      return List.of(
          name, Text.translatable("nightborn.coating.until_fired").formatted(Formatting.GRAY));
    }
    final int hits = coat.hitsLeft(terms);
    final String time =
        StringHelper.formatTicks(
            (int) coat.ticksLeft(terms, now), SharedConstants.TICKS_PER_SECOND);
    final Text left =
        coat.blessed()
            ? Text.translatable("nightborn.coating.left.time", time)
            : hits == 1
                ? Text.translatable("nightborn.coating.left.one", time)
                : Text.translatable("nightborn.coating.left", hits, time);
    return List.of(name, left.copy().formatted(Formatting.GRAY));
  }
}
