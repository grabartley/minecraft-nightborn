package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.Gestures;
import com.grahambartley.nightborn.mob.VampirePose;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
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

  private static final Set<LivingEntity> FORCED =
      Collections.newSetFromMap(new IdentityHashMap<>());

  private CoffinRise() {}

  public static void register() {}

  public static boolean rising(final LivingEntity sleeper) {
    return sleeper.hasAttached(TYPE);
  }

  public static Optional<VampirePose> pose(final LivingEntity sleeper) {
    if (!Vampires.isVampire(sleeper) || !CoffinSleepers.inCoffin(sleeper)) {
      return Optional.empty();
    }
    return Optional.of(
        VampirePose.coffin(rising(sleeper), CoffinSleepers.inStandingCoffin(sleeper)));
  }

  public static void wakeAtOnce(final ServerPlayerEntity sleeper) {
    if (!sleeper.isSleeping()) {
      return;
    }
    FORCED.add(sleeper);
    try {
      sleeper.wakeUp(true, true);
    } finally {
      FORCED.remove(sleeper);
    }
    sleeper.removeAttached(TYPE);
  }

  public static boolean deferWakeIntoRise(final LivingEntity sleeper) {
    if (sleeper.getWorld().isClient) {
      return false;
    }
    final RiseRules.Wake wake =
        RiseRules.wake(
            staysInside(sleeper),
            FORCED.contains(sleeper),
            sleeper.isOnFire(),
            until(sleeper),
            sleeper.getWorld().getTime());
    if (wake == RiseRules.Wake.BEGIN) {
      DayRest.woke(sleeper);
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
    if (!deferWakeIntoRise(sleeper)) {
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
    Gestures.show(sleeper, VampirePose.WAKE_STRETCH);
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
