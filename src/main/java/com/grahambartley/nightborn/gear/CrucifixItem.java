package com.grahambartley.nightborn.gear;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class CrucifixItem extends BlockItem {

  public CrucifixItem(final Block wallBlock, final Settings settings) {
    super(wallBlock, settings);
  }

  @Override
  public String getTranslationKey() {
    return this.getOrCreateTranslationKey();
  }
}
