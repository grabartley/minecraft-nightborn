package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
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

public final class GuildMobs {

  public static final EntityType<HunterEntity> HUNTER =
      register(
          "hunter",
          EntityType.Builder.create(HunterEntity::new, SpawnGroup.MISC)
              .dimensions(0.6F, 1.95F)
              .eyeHeight(1.62F)
              .maxTrackingRange(10));

  public static final EntityType<GuildMasterEntity> GUILD_MASTER =
      register(
          "guild_master",
          EntityType.Builder.create(GuildMasterEntity::new, SpawnGroup.MISC)
              .dimensions(0.6F, 1.95F)
              .eyeHeight(1.62F)
              .maxTrackingRange(10));

  public static final Item HUNTER_SPAWN_EGG =
      egg("hunter_spawn_egg", new SpawnEggItem(HUNTER, 0x3B2A1E, 0xB8B4A8, new Item.Settings()));

  public static final Item GUILD_MASTER_SPAWN_EGG =
      egg(
          "guild_master_spawn_egg",
          new SpawnEggItem(GUILD_MASTER, 0x1F2A22, 0xB08D3C, new Item.Settings()));

  private GuildMobs() {}

  static void register() {
    FabricDefaultAttributeRegistry.register(HUNTER, HunterEntity.createHunterAttributes());
    FabricDefaultAttributeRegistry.register(
        GUILD_MASTER, GuildMasterEntity.createGuildMasterAttributes());
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(
            entries -> {
              entries.add(HUNTER_SPAWN_EGG);
              entries.add(GUILD_MASTER_SPAWN_EGG);
            });
  }

  private static <T extends Entity> EntityType<T> register(
      final String path, final EntityType.Builder<T> builder) {
    return Registry.register(
        Registries.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, path), builder.build(path));
  }

  private static Item egg(final String path, final Item egg) {
    return Registry.register(Registries.ITEM, Identifier.of(Nightborn.MOD_ID, path), egg);
  }
}
