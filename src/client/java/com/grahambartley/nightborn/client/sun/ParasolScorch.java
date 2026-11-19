package com.grahambartley.nightborn.client.sun;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class ParasolScorch {

  static final List<String> STAGES = List.of("fresh", "singed", "scorched", "charred");
  static final List<Double> FROM = List.of(0.0, 0.35, 0.65, 0.9);

  private ParasolScorch() {}

  public static Identifier texture(final ItemStack stack) {
    return texture(stack.getDamage(), stack.getMaxDamage());
  }

  static Identifier texture(final int damage, final int maxDamage) {
    return Identifier.of(
        Nightborn.MOD_ID, "textures/item/parasol/" + STAGES.get(stage(damage, maxDamage)) + ".png");
  }

  static int stage(final int damage, final int maxDamage) {
    final double worn = maxDamage <= 0 ? 0.0 : (double) damage / maxDamage;
    int stage = 0;
    while (stage + 1 < FROM.size() && worn >= FROM.get(stage + 1)) {
      stage++;
    }
    return stage;
  }
}
