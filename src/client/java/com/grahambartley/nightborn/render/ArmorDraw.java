package com.grahambartley.nightborn.render;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import software.bernie.geckolib.animatable.GeoItem;

enum ArmorDraw {
  NONE,
  VANILLA,
  GECKO;

  static ArmorDraw of(final Item item) {
    return of(item instanceof ArmorItem, item instanceof GeoItem);
  }

  static ArmorDraw of(final boolean armour, final boolean geckoModel) {
    if (!armour) {
      return NONE;
    }
    return geckoModel ? GECKO : VANILLA;
  }
}
