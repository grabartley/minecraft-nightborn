package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class BloodItems {

  public static final ComponentType<BloodQuality> BLOOD_QUALITY =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(Nightborn.MOD_ID, "blood_quality"),
          ComponentType.<BloodQuality>builder()
              .codec(BloodQuality.CODEC)
              .packetCodec(BloodQuality.PACKET_CODEC.cast())
              .build());

  public static final Item BLOOD_BOTTLE =
      Registry.register(
          Registries.ITEM,
          Identifier.of(Nightborn.MOD_ID, "blood_bottle"),
          new BloodBottleItem(
              new Item.Settings().maxCount(16).component(BLOOD_QUALITY, BloodQuality.ANIMAL)));

  private BloodItems() {}

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
        .register(
            entries -> {
              for (final BloodQuality quality : BloodQuality.values()) {
                entries.add(BloodBottleItem.of(quality));
              }
            });
  }
}
