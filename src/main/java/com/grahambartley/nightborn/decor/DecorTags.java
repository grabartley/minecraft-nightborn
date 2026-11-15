package com.grahambartley.nightborn.decor;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class DecorTags {

  public static final TagKey<Item> WEAPON_RACK_HOLDS =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "weapon_rack_holds"));

  private DecorTags() {}
}
