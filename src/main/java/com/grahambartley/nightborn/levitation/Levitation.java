package com.grahambartley.nightborn.levitation;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.network.ToggleLevitationPayload;
import com.grahambartley.nightborn.trap.MarkedGrounds;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.village.SuspicionCause;
import com.grahambartley.nightborn.village.VampireSigns;
import com.grahambartley.nightborn.weakened.Weakened;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class Levitation {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "levitating");

  public static final AttachmentType<Boolean> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder
                  .persistent(Codec.BOOL)
                  .syncWith(PacketCodecs.BOOL.cast(), AttachmentSyncPredicate.all()));

  public static final AttachmentType<LevitationGuard> GUARD =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "levitation_guard"));

  static final AttachmentType<Long> TOGGLED_AT =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "levitation_toggled_at"));

  static final int SET_DOWN_FALL_TICKS = 100;
  static final double POSE_FIT_MARGIN = 1.0E-7;

  static final String REFUSED = "nightborn.levitation.refused";

  private Levitation() {}

  public static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        ToggleLevitationPayload.ID, (payload, context) -> request(context.player()));
  }

  public static void request(final ServerPlayerEntity player) {
    if (!Vampires.isVampire(player) || player.isSpectator() || !player.isAlive()) {
      return;
    }
    final long now = player.getWorld().getTime();
    if (!LevitationRules.toggleReady(player.getAttached(TOGGLED_AT), now)) {
      return;
    }
    player.setAttached(TOGGLED_AT, now);
    if (isLevitating(player)) {
      land(player);
    } else if (!start(player)) {
      player.sendMessage(Text.translatable(REFUSED), true);
    }
  }

  public static boolean isLevitating(final LivingEntity entity) {
    return entity.getAttachedOrElse(TYPE, false);
  }

  public static boolean sinksFast(final LivingEntity entity) {
    return LevitationRules.sinksFast(
        isLevitating(entity), entity instanceof PlayerEntity, entity.isSneaking());
  }

  public static boolean holdsFloatPose(final LivingEntity entity) {
    return isLevitating(entity) && LevitationRules.holdsFloatPose(true, hasRoomToStand(entity));
  }

  private static boolean hasRoomToStand(final LivingEntity entity) {
    return entity
        .getWorld()
        .isSpaceEmpty(
            entity,
            entity
                .getDimensions(EntityPose.STANDING)
                .getBoxAt(entity.getPos())
                .contract(POSE_FIT_MARGIN));
  }

  public static boolean start(final LivingEntity vampire) {
    if (MarkedGrounds.holdsDown(vampire)
        || !LevitationRules.mayLevitate(
            Vampires.isVampire(vampire),
            weakness(vampire),
            Downed.state(vampire) != null,
            occupied(vampire))) {
      return false;
    }
    vampire.setAttached(TYPE, true);
    VampireSigns.shown(vampire, SuspicionCause.SEEN_LEVITATING);
    return true;
  }

  public static void land(final LivingEntity entity) {
    if (entity.hasAttached(TYPE)) {
      entity.removeAttached(TYPE);
    }
    if (entity.hasAttached(GUARD)) {
      entity.removeAttached(GUARD);
    }
  }

  public static void touchDown(final ServerPlayerEntity player, final boolean onGround) {
    if (LevitationRules.landsOnSneak(sinksFast(player), onGround)) {
      land(player);
    }
  }

  public static void setDown(final LivingEntity entity) {
    final boolean wasLevitating = isLevitating(entity);
    land(entity);
    if (wasLevitating && !entity.isOnGround()) {
      entity.addStatusEffect(
          new StatusEffectInstance(
              StatusEffects.SLOW_FALLING, SET_DOWN_FALL_TICKS, 0, false, false));
    }
  }

  public static void sink(final LivingEntity entity) {
    if (LevitationRules.cushions(
        isLevitating(entity),
        entity.isOnGround(),
        entity.getY(),
        LevitationGround.surfaceBelow(entity))) {
      setDown(entity);
    } else {
      land(entity);
    }
  }

  public static void tick(final LivingEntity vampire, final int weakened, final boolean downed) {
    if (!isLevitating(vampire)) {
      return;
    }
    if (!LevitationRules.mayLevitate(true, weakened, downed, occupied(vampire))) {
      setDown(vampire);
      return;
    }
    vampire.fallDistance = 0.0F;
    if (vampire instanceof ServerPlayerEntity player) {
      guard(player);
    }
  }

  private static void guard(final ServerPlayerEntity player) {
    final LevitationGuard guard =
        player
            .getAttachedOrElse(GUARD, LevitationGuard.FRESH)
            .next(player.getY(), LevitationGround.surfaceBelow(player));
    if (guard.lands()) {
      setDown(player);
    } else {
      player.setAttached(GUARD, guard);
    }
  }

  private static boolean occupied(final LivingEntity entity) {
    return BatForm.isBat(entity)
        || entity.hasVehicle()
        || entity.isSleeping()
        || entity.isFallFlying()
        || entity instanceof PlayerEntity player && player.getAbilities().flying;
  }

  private static int weakness(final LivingEntity vampire) {
    return Weakened.sources(vampire).level(Downed.state(vampire) != null);
  }
}
