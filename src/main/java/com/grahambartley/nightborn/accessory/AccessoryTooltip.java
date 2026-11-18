package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.faction.Faction;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class AccessoryTooltip {

  public static final String HOLDS = "tooltip." + Nightborn.MOD_ID + ".accessory.holds";

  private AccessoryTooltip() {}

  public static String key(final Faction wearer) {
    return "tooltip." + Nightborn.MOD_ID + ".accessory.works_for." + wearer.asString();
  }

  public static String giftKey(final Item item) {
    return item.getTranslationKey() + ".gift";
  }

  public static Text worksFor(final Faction wearer) {
    return Text.translatable(key(wearer)).formatted(Formatting.GRAY);
  }

  public static List<Text> lines(
      final Item item, final Accessory accessory, final ItemStack stack) {
    final List<Text> lines = new ArrayList<>();
    if (!accessory.plain()) {
      lines.add(Text.translatable(giftKey(item)).formatted(Formatting.DARK_PURPLE));
    }
    accessory
        .holds()
        .ifPresent(
            hold ->
                lines.add(
                    Text.translatable(HOLDS, AccessoryStore.held(stack), hold.count())
                        .formatted(Formatting.GRAY)));
    accessory.worksFor().map(AccessoryTooltip::worksFor).ifPresent(lines::add);
    return lines;
  }
}
