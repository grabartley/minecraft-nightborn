package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.feeding.LancetItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
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

  public static final ComponentType<Identifier> BLOOD_GIFT =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(Nightborn.MOD_ID, "blood_gift"),
          ComponentType.<Identifier>builder()
              .codec(Identifier.CODEC)
              .packetCodec(Identifier.PACKET_CODEC)
              .build());

  public static final Item BLOOD_BOTTLE =
      Registry.register(
          Registries.ITEM,
          Identifier.of(Nightborn.MOD_ID, "blood_bottle"),
          new BloodBottleItem(
              new Item.Settings()
                  .maxCount(16)
                  .recipeRemainder(Items.GLASS_BOTTLE)
                  .component(BLOOD_QUALITY, BloodQuality.ANIMAL)));

  public static final Item LANCET =
      Registry.register(
          Registries.ITEM,
          Identifier.of(Nightborn.MOD_ID, "lancet"),
          new LancetItem(new Item.Settings().maxDamage(LancetItem.DURABILITY)));

  private BloodItems() {}

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
        .register(
            entries -> {
              for (final BloodQuality quality : BloodQuality.values()) {
                entries.add(BloodBottleItem.of(quality));
              }
            });
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(LANCET));
  }
}
