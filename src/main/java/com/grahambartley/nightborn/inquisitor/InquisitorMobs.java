package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.VampireBatEntity;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class InquisitorMobs {

  static final String ID = "grand_inquisitor";

  public static final EntityType<GrandInquisitorEntity> GRAND_INQUISITOR =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, ID),
          EntityType.Builder.create(GrandInquisitorEntity::new, SpawnGroup.MISC)
              .dimensions(0.6F, 1.95F)
              .eyeHeight(1.62F)
              .maxTrackingRange(10)
              .makeFireImmune()
              .build(ID));

  static final String HAWK_ID = "tame_hawk";

  public static final EntityType<TameHawkEntity> TAME_HAWK =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, HAWK_ID),
          EntityType.Builder.<TameHawkEntity>create(TameHawkEntity::new, SpawnGroup.MISC)
              .dimensions(0.6F, 0.7F)
              .eyeHeight(0.4F)
              .maxTrackingRange(8)
              .build(HAWK_ID));

  public static final Item GRAND_INQUISITOR_SPAWN_EGG =
      Registry.register(
          Registries.ITEM,
          Identifier.of(Nightborn.MOD_ID, ID + "_spawn_egg"),
          new SpawnEggItem(GRAND_INQUISITOR, 0x1A2238, 0xD8C690, new Item.Settings()));

  private InquisitorMobs() {}

  static void register() {
    FabricDefaultAttributeRegistry.register(
        GRAND_INQUISITOR, GrandInquisitorEntity.createInquisitorAttributes());
    FabricDefaultAttributeRegistry.register(
        TAME_HAWK, VampireBatEntity.createVampireBatAttributes());
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(entries -> entries.add(GRAND_INQUISITOR_SPAWN_EGG));
  }
}
