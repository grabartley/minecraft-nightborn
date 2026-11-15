package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.OptionalLong;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class CoffinRise {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "coffin_rise");

  public static final AttachmentType<Long> TYPE =
      AttachmentRegistry.create(
          ID,
          builder -> builder.syncWith(PacketCodecs.VAR_LONG.cast(), AttachmentSyncPredicate.all()));

  private CoffinRise() {}

  public static void register() {}

  public static boolean rising(final LivingEntity sleeper) {
    return sleeper.hasAttached(TYPE);
  }

  public static boolean holds(final LivingEntity sleeper) {
    if (sleeper.getWorld().isClient) {
      return false;
    }
    final RiseRules.Wake wake =
        RiseRules.wake(staysInside(sleeper), until(sleeper), sleeper.getWorld().getTime());
    if (wake == RiseRules.Wake.BEGIN) {
      begin(sleeper);
    }
    return wake != RiseRules.Wake.WAKE;
  }

  public static void tick(final LivingEntity sleeper) {
    if (sleeper.getWorld().isClient || !rising(sleeper)) {
      return;
    }
    if (!sleeper.isSleeping()) {
      sleeper.removeAttached(TYPE);
      return;
    }
    if (!holds(sleeper)) {
      finish(sleeper);
    }
  }

  private static void begin(final LivingEntity sleeper) {
    final BlockPos head = sleeper.getSleepingPosition().orElseThrow();
    final BlockState state = sleeper.getWorld().getBlockState(head);
    sleeper.setAttached(
        TYPE, sleeper.getWorld().getTime() + RiseRules.ticks(state.get(CoffinBlock.STANDING)));
    sleeper.setPose(EntityPose.STANDING);
    CoffinBlock.openLid(sleeper.getWorld(), head);
    CoffinSleeperHold.hold(sleeper);
  }

  private static void finish(final LivingEntity sleeper) {
    final float yaw = sleeper.getYaw();
    if (sleeper instanceof PlayerEntity player) {
      player.wakeUp(false, true);
    } else {
      sleeper.wakeUp();
    }
    sleeper.removeAttached(TYPE);
    if (sleeper instanceof ServerPlayerEntity player) {
      player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), yaw, 0.0F);
    } else {
      sleeper.setYaw(yaw);
      sleeper.setHeadYaw(yaw);
      sleeper.setBodyYaw(yaw);
    }
  }

  private static boolean staysInside(final LivingEntity sleeper) {
    return Vampires.isVampire(sleeper)
        && CoffinSleepers.inCoffin(sleeper)
        && sleeper.isAlive()
        && !sleeper.isRemoved()
        && !(sleeper instanceof ServerPlayerEntity player && player.isDisconnected());
  }

  private static OptionalLong until(final LivingEntity sleeper) {
    final Long until = sleeper.getAttached(TYPE);
    return until == null ? OptionalLong.empty() : OptionalLong.of(until);
  }
}
