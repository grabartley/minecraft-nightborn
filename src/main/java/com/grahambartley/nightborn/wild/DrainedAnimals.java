package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.core.SurfaceSpots;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class DrainedAnimals {

  public static final TagKey<EntityType<?>> KINDS =
      TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "drained_animals"));

  static final int NEAR = 16;
  static final int FAR = 32;
  static final float SPREAD = MathHelper.HALF_PI;
  static final String NO_LOOT = "minecraft:empty";
  static final String LOOT_KEY = "DeathLootTable";
  static final long FALL_DELAY = 20L;

  static final AttachmentType<Long> FALLS_AT =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "drained_falls_at"),
          builder -> builder.persistent(Codec.LONG));

  private static final Map<RegistryKey<World>, Set<UUID>> FALLING = new HashMap<>();

  private DrainedAnimals() {}

  static void register() {
    ServerEntityEvents.ENTITY_LOAD.register(
        (entity, world) -> {
          if (entity.hasAttached(FALLS_AT)) {
            falling(world).add(entity.getUuid());
          }
        });
    ServerEntityEvents.ENTITY_UNLOAD.register(
        (entity, world) -> falling(world).remove(entity.getUuid()));
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> FALLING.clear());
  }

  static boolean fall(
      final ServerWorld world, final ServerPlayerEntity player, final Random random) {
    final List<RegistryEntry<EntityType<?>>> kinds =
        Registries.ENTITY_TYPE
            .getEntryList(KINDS)
            .map(list -> list.stream().toList())
            .orElse(List.of());
    if (kinds.isEmpty()) {
      return false;
    }
    final Optional<BlockPos> spot =
        SurfaceSpots.at(
            world,
            SurfaceSpots.offset(
                player.getBlockPos(),
                ahead(player.getYaw(), random.nextFloat()),
                random.nextBetween(NEAR, FAR)));
    if (spot.isEmpty()) {
      return false;
    }
    final Entity animal = kinds.get(random.nextInt(kinds.size())).value().create(world);
    if (!(animal instanceof MobEntity body)) {
      return false;
    }
    body.refreshPositionAndAngles(spot.get(), random.nextFloat() * 360.0F, 0.0F);
    final NbtCompound nbt = body.writeNbt(new NbtCompound());
    nbt.putString(LOOT_KEY, NO_LOOT);
    body.readNbt(nbt);
    body.setSilent(true);
    body.setAiDisabled(true);
    body.setAttached(FALLS_AT, world.getTime() + FALL_DELAY);
    world.spawnEntity(body);
    falling(world).add(body.getUuid());
    return true;
  }

  static void tick(final ServerWorld world) {
    final long now = world.getTime();
    falling(world)
        .removeIf(
            id -> {
              if (!(world.getEntity(id) instanceof MobEntity body) || !body.isAlive()) {
                return true;
              }
              if (!due(body.getAttachedOrElse(FALLS_AT, now), now)) {
                return false;
              }
              body.removeAttached(FALLS_AT);
              body.damage(world.getDamageSources().create(NightbornDamage.BITE), Float.MAX_VALUE);
              return true;
            });
  }

  static boolean due(final long at, final long now) {
    return now >= at;
  }

  private static Set<UUID> falling(final ServerWorld world) {
    return FALLING.computeIfAbsent(world.getRegistryKey(), key -> new HashSet<>());
  }

  static float ahead(final float yawDegrees, final float roll) {
    return yawDegrees * MathHelper.RADIANS_PER_DEGREE + MathHelper.HALF_PI + (roll - 0.5F) * SPREAD;
  }
}
