package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.clothing.Dressed;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import software.bernie.geckolib.animatable.GeoItem;

enum ArmorDraw {
  NONE,
  VANILLA,
  GECKO;

  static ArmorDraw of(final Item item) {
    return of(
        item instanceof ArmorItem,
        item instanceof GeoItem && (item instanceof ArmorItem || item instanceof Dressed));
  }

  static ArmorDraw of(final boolean armour, final boolean geckoModel) {
    if (geckoModel) {
      return GECKO;
    }
    return armour ? VANILLA : NONE;
  }

  boolean flattensBust() {
    return this == VANILLA;
  }
}
