package com.grahambartley.nightborn.clothing;

import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;

public interface Dressed {

  String artId();

  int undyedColor(RegistryWrapper.WrapperLookup registries);

  default int color(final ItemStack stack, final RegistryWrapper.WrapperLookup registries) {
    return DyedColorComponent.getColor(stack, undyedColor(registries));
  }
}
