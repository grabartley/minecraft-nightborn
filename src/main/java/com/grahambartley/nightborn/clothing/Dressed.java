package com.grahambartley.nightborn.clothing;

import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;

public interface Dressed {

  Garment garment();

  default int color(final ItemStack stack) {
    return DyedColorComponent.getColor(stack, garment().undyedColor());
  }
}
