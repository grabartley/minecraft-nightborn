package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.VampireStructure;
import com.grahambartley.nightborn.structure.VampireStructureStarts;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
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

  public static final TagKey<Biome> SPAWNS_VAMPIRE_BATS =
      TagKey.of(RegistryKeys.BIOME, Identifier.of(Nightborn.MOD_ID, "spawns_vampire_bats"));

  static final int VAMPIRE_BAT_WEIGHT = 2;

  private MobSpawns() {}

  static void register() {
    BiomeModifications.addSpawn(
        BiomeSelectors.tag(SPAWNS_VAMPIRE_BATS),
        SpawnGroup.AMBIENT,
        NightbornMobs.VAMPIRE_BAT,
        VAMPIRE_BAT_WEIGHT,
        1,
        2);
    SpawnRestriction.register(
        NightbornMobs.VAMPIRE_BAT,
        SpawnLocationTypes.ON_GROUND,
        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
        MobSpawns::batCanSpawn);
  }

  public static boolean batCanSpawn(
      final EntityType<? extends MobEntity> type,
      final ServerWorldAccess world,
      final SpawnReason reason,
      final BlockPos pos,
      final Random random) {
    if (reason != SpawnReason.NATURAL) {
      return MobEntity.canMobSpawn(type, world, reason, pos, random);
    }
    return MobSpawnRules.batMaySpawn(
            random.nextInt(100),
            ServerConfigService.current().mobs().vampireBatSpawnChance(),
            () -> BatEntity.canSpawn(asBat(type), world, reason, pos, random),
            () ->
                VampireStructureStarts.anyContains(
                        world.toServerWorld(), pos, VampireStructure::spawnsVampireBats)
                    && MobEntity.canMobSpawn(type, world, reason, pos, random))
        && !MobSpawnRules.crowded(batsAround(world, pos));
  }

  @SuppressWarnings("unchecked")
  private static EntityType<BatEntity> asBat(final EntityType<? extends MobEntity> type) {
    return (EntityType<BatEntity>) (EntityType<?>) type;
  }

  private static int batsAround(final ServerWorldAccess world, final BlockPos pos) {
    final PlayerEntity player =
        world.getClosestPlayer(
            pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, MobSpawnRules.NEARBY_RANGE, false);
    final Vec3d centre = player == null ? Vec3d.ofCenter(pos) : player.getPos();
    return world
        .getEntitiesByClass(
            VampireBatEntity.class,
            Box.of(
                centre,
                MobSpawnRules.NEARBY_RANGE * 2,
                MobSpawnRules.NEARBY_HEIGHT * 2,
                MobSpawnRules.NEARBY_RANGE * 2),
            bat -> true)
        .size();
  }
}
