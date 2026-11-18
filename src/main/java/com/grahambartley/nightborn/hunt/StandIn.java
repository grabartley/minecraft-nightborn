package com.grahambartley.nightborn.hunt;

import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class StandIn {

  public static final String TAG = "nightborn.inquisitor_stand_in";
  static final double WALK_SPEED = 1.0;
  static final float ARRIVAL_VOLUME = 3.0F;
  static final float ARRIVAL_PITCH = 0.6F;

  private StandIn() {}

  static void register() {
    ServerEntityEvents.ENTITY_LOAD.register(
        (entity, world) -> {
          if (is(entity)) {
            entity.discard();
          }
        });
  }

  public static boolean is(final Entity entity) {
    return entity.getCommandTags().contains(TAG);
  }

  public static Optional<UUID> arrive(final ServerPlayerEntity quarry) {
    final ServerWorld world = quarry.getServerWorld();
    final Optional<BlockPos> spot =
        ArrivalSpot.find(world, quarry.getBlockPos(), world.getRandom());
    if (spot.isEmpty()) {
      return Optional.empty();
    }
    final HunterEntity man = GuildMobs.HUNTER.create(world);
    if (man == null) {
      return Optional.empty();
    }
    final BlockPos at = spot.get();
    man.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
    man.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
    man.setCustomName(Text.translatable("nightborn.hunt.stand_in"));
    man.setCustomNameVisible(true);
    man.setPersistent();
    world.spawnEntity(man);
    man.addCommandTag(TAG);
    man.lookAtEntity(quarry, 360.0F, 360.0F);
    if (man.canTarget(quarry)) {
      man.setTarget(quarry);
    }
    man.getNavigation().startMovingTo(quarry, WALK_SPEED);
    world.playSound(
        null, at, HuntSounds.WARNING_TOLL, SoundCategory.HOSTILE, ARRIVAL_VOLUME, ARRIVAL_PITCH);
    quarry.sendMessage(Text.translatable("nightborn.hunt.arrived"), false);
    return Optional.of(man.getUuid());
  }

  static void dismiss(final ServerWorld world, final UUID man) {
    final Entity entity = world.getEntity(man);
    if (entity != null && is(entity)) {
      entity.discard();
    }
  }
}
