package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.household.Temperaments;
import com.grahambartley.nightborn.mob.look.VampireLooks;
import com.grahambartley.nightborn.mob.stage.VampireStages;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class NightbornMobs {

  static final float VAMPIRE_WIDTH = 0.6F;
  static final float VAMPIRE_HEIGHT = 1.95F;
  static final float VAMPIRE_EYES = 1.74F;

  public static final EntityType<VampireEntity> VAMPIRE =
      register(
          "vampire",
          EntityType.Builder.create(VampireEntity::new, SpawnGroup.MONSTER)
              .dimensions(VAMPIRE_WIDTH, VAMPIRE_HEIGHT)
              .eyeHeight(VAMPIRE_EYES)
              .maxTrackingRange(8));

  public static final EntityType<VampireLordEntity> VAMPIRE_LORD =
      register(
          "vampire_lord",
          EntityType.Builder.create(VampireLordEntity::new, SpawnGroup.MONSTER)
              .dimensions(VAMPIRE_WIDTH, VAMPIRE_HEIGHT)
              .eyeHeight(VAMPIRE_EYES)
              .maxTrackingRange(10));

  public static final EntityType<EldestEntity> ELDEST =
      register(
          "eldest",
          EntityType.Builder.create(EldestEntity::new, SpawnGroup.MONSTER)
              .dimensions(VAMPIRE_WIDTH, VAMPIRE_HEIGHT)
              .eyeHeight(VAMPIRE_EYES)
              .maxTrackingRange(10));

  public static final EntityType<VampireBatEntity> VAMPIRE_BAT =
      register(
          "vampire_bat",
          EntityType.Builder.create(VampireBatEntity::new, SpawnGroup.AMBIENT)
              .dimensions(0.6F, 0.9F)
              .eyeHeight(0.45F)
              .maxTrackingRange(5));

  public static final Item VAMPIRE_SPAWN_EGG =
      registerEgg(
          "vampire_spawn_egg", new SpawnEggItem(VAMPIRE, 0x2A0A10, 0xD8CEC2, new Item.Settings()));

  public static final Item VAMPIRE_LORD_SPAWN_EGG =
      registerEgg(
          "vampire_lord_spawn_egg",
          new SpawnEggItem(VAMPIRE_LORD, 0x3A0610, 0xB8902E, new Item.Settings()));

  public static final Item ELDEST_SPAWN_EGG =
      registerEgg(
          "eldest_spawn_egg", new SpawnEggItem(ELDEST, 0x120608, 0x8C1420, new Item.Settings()));

  public static final Item VAMPIRE_BAT_SPAWN_EGG =
      registerEgg(
          "vampire_bat_spawn_egg",
          new SpawnEggItem(VAMPIRE_BAT, 0x161012, 0x7A1018, new Item.Settings()));

  private NightbornMobs() {}

  public static void initialize() {
    MobSounds.initialize();
    VampireLooks.register();
    VampireStages.register();
    Temperaments.register();
    FabricDefaultAttributeRegistry.register(VAMPIRE, VampireEntity.createVampireAttributes());
    FabricDefaultAttributeRegistry.register(VAMPIRE_LORD, VampireEntity.createVampireAttributes());
    FabricDefaultAttributeRegistry.register(ELDEST, VampireEntity.createVampireAttributes());
    FabricDefaultAttributeRegistry.register(
        VAMPIRE_BAT, VampireBatEntity.createVampireBatAttributes());
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(
            entries -> {
              entries.add(VAMPIRE_SPAWN_EGG);
              entries.add(VAMPIRE_LORD_SPAWN_EGG);
              entries.add(ELDEST_SPAWN_EGG);
              entries.add(VAMPIRE_BAT_SPAWN_EGG);
            });
    MobSpawns.register();
  }

  private static <T extends Entity> EntityType<T> register(
      final String path, final EntityType.Builder<T> builder) {
    return Registry.register(
        Registries.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, path), builder.build(path));
  }

  private static Item registerEgg(final String path, final Item egg) {
    return Registry.register(Registries.ITEM, Identifier.of(Nightborn.MOD_ID, path), egg);
  }
}
