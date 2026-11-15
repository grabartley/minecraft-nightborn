package com.grahambartley.nightborn.ward;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class WardTags {

  public static final TagKey<Block> CRUCIFIX_MATERIALS =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, "crucifix_materials"));
  public static final TagKey<Item> CRUCIFIXES =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "crucifixes"));
  public static final TagKey<Item> GARLIC =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "garlic"));

  private WardTags() {}
}
