package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.biome.Biome;

public final class MobSpawns {

  public static final TagKey<Biome> SPAWNS_VAMPIRES =
      TagKey.of(RegistryKeys.BIOME, Identifier.of(Nightborn.MOD_ID, "spawns_vampires"));
  public static final TagKey<Biome> SPAWNS_VAMPIRE_BATS =
      TagKey.of(RegistryKeys.BIOME, Identifier.of(Nightborn.MOD_ID, "spawns_vampire_bats"));

  static final int VAMPIRE_WEIGHT = 4;
  static final int VAMPIRE_BAT_WEIGHT = 2;

  private MobSpawns() {}

  static void register() {
    BiomeModifications.addSpawn(
        BiomeSelectors.tag(SPAWNS_VAMPIRES),
        SpawnGroup.MONSTER,
        NightbornMobs.VAMPIRE,
        VAMPIRE_WEIGHT,
        1,
        1);
    BiomeModifications.addSpawn(
        BiomeSelectors.tag(SPAWNS_VAMPIRE_BATS),
        SpawnGroup.AMBIENT,
        NightbornMobs.VAMPIRE_BAT,
        VAMPIRE_BAT_WEIGHT,
        1,
        2);
    SpawnRestriction.register(
        NightbornMobs.VAMPIRE,
        SpawnLocationTypes.ON_GROUND,
        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
        MobSpawns::vampireCanSpawn);
    SpawnRestriction.register(
        NightbornMobs.VAMPIRE_BAT,
        SpawnLocationTypes.ON_GROUND,
        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
        MobSpawns::batCanSpawn);
  }

  static boolean vampireCanSpawn(
      final EntityType<? extends HostileEntity> type,
      final ServerWorldAccess world,
      final SpawnReason reason,
      final BlockPos pos,
      final Random random) {
    if (reason != SpawnReason.NATURAL) {
      return HostileEntity.canSpawnInDark(type, world, reason, pos, random);
    }
    return MobSpawnRules.vampireMaySpawn(
            world.getDimension().hasFixedTime(),
            world.getLevelProperties().getTimeOfDay(),
            world.isSkyVisible(pos),
            random.nextInt(100),
            ServerConfigService.current().mobs().vampireSpawnChance())
        && HostileEntity.canSpawnInDark(type, world, reason, pos, random);
  }

  static boolean batCanSpawn(
      final EntityType<? extends MobEntity> type,
      final ServerWorldAccess world,
      final SpawnReason reason,
      final BlockPos pos,
      final Random random) {
    if (reason == SpawnReason.NATURAL
        && !MobSpawnRules.batMaySpawn(
            world.getDimension().hasFixedTime(),
            world.getLevelProperties().getTimeOfDay(),
            world.isSkyVisible(pos),
            world.getLightLevel(pos),
            random.nextInt(100),
            ServerConfigService.current().mobs().vampireBatSpawnChance())) {
      return false;
    }
    if (reason == SpawnReason.NATURAL
        && MobSpawnRules.crowded(
            world
                .getEntitiesByClass(
                    VampireBatEntity.class,
                    Box.of(
                        Vec3d.ofCenter(pos),
                        MobSpawnRules.NEARBY_RANGE * 2,
                        MobSpawnRules.NEARBY_RANGE * 2,
                        MobSpawnRules.NEARBY_RANGE * 2),
                    bat -> true)
                .size())) {
      return false;
    }
    return MobEntity.canMobSpawn(type, world, reason, pos, random);
  }
}
