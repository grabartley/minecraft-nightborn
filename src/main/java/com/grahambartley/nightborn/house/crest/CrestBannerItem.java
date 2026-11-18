package com.grahambartley.nightborn.house.crest;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;

public class CrestBannerItem extends BlockItem {

  static final String DESCRIBED = "nightborn.crest.described";
  static final String BLANK = "nightborn.crest.blank";

  public CrestBannerItem(final Block block, final Settings settings) {
    super(block, settings);
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    super.appendTooltip(stack, context, tooltip, type);
    tooltip.add(
        describe(Optional.ofNullable(stack.get(CrestBanners.CREST)))
            .copy()
            .formatted(Formatting.GRAY));
  }

  public static Text describe(final Optional<Crest> crest) {
    return crest
        .<Text>map(
            shown ->
                Text.translatable(
                    DESCRIBED,
                    Text.translatable(shown.charge().translationKey()),
                    Text.translatable(colour(shown.tincture())),
                    Text.translatable(colour(shown.field()))))
        .orElseGet(() -> Text.translatable(BLANK));
  }

  public static String colour(final DyeColor dye) {
    return "color.minecraft." + dye.getName();
  }
}
