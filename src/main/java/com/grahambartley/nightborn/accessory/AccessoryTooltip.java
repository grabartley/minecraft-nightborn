package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.faction.Faction;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class AccessoryTooltip {

  private AccessoryTooltip() {}

  public static String key(final Faction wearer) {
    return "tooltip." + Nightborn.MOD_ID + ".accessory.works_for." + wearer.asString();
  }

  public static Text worksFor(final Faction wearer) {
    return Text.translatable(key(wearer)).formatted(Formatting.GRAY);
  }
}
