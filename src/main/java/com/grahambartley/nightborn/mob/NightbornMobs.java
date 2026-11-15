package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
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

  public static final EntityType<VampireEntity> VAMPIRE =
      register(
          "vampire",
          EntityType.Builder.create(VampireEntity::new, SpawnGroup.MONSTER)
              .dimensions(0.6F, 1.95F)
              .eyeHeight(1.74F)
              .maxTrackingRange(8));

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

  public static final Item VAMPIRE_BAT_SPAWN_EGG =
      registerEgg(
          "vampire_bat_spawn_egg",
          new SpawnEggItem(VAMPIRE_BAT, 0x161012, 0x7A1018, new Item.Settings()));

  private NightbornMobs() {}

  public static void initialize() {
    MobSounds.initialize();
    VampireLooks.register();
    VampireStages.register();
    FabricDefaultAttributeRegistry.register(VAMPIRE, VampireEntity.createVampireAttributes());
    FabricDefaultAttributeRegistry.register(
        VAMPIRE_BAT, VampireBatEntity.createVampireBatAttributes());
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(
            entries -> {
              entries.add(VAMPIRE_SPAWN_EGG);
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
