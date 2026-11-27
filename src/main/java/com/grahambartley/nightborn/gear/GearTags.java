package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class GearTags {

  public static final TagKey<Item> REVERSE_GRIP =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "reverse_grip"));

  private GearTags() {}
}
