package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ParasolItems {

  public static final ComponentType<Boolean> OPEN =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(Nightborn.MOD_ID, "parasol_open"),
          ComponentType.<Boolean>builder()
              .codec(Codec.BOOL)
              .packetCodec(PacketCodecs.BOOL)
              .build());

  public static final Item PARASOL =
      Registry.register(
          Registries.ITEM,
          Identifier.of(Nightborn.MOD_ID, "parasol"),
          new ParasolItem(new Item.Settings().maxDamage(ParasolWear.DURABILITY)));

  private ParasolItems() {}

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(entries -> entries.addAfter(Items.SHEARS, PARASOL));
  }
}
