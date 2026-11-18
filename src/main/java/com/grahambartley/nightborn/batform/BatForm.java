package com.grahambartley.nightborn.batform;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.TravelLog;
import com.grahambartley.nightborn.config.CoreSettings;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.BatFormPower;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.smoke.SmokePoof;
import com.grahambartley.nightborn.sun.DirectSun;
import com.grahambartley.nightborn.village.SuspicionCause;
import com.grahambartley.nightborn.village.VampireSigns;
import com.grahambartley.nightborn.weakened.Weakened;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class BatForm {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "bat_form");

  public static final AttachmentType<Boolean> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder
                  .persistent(Codec.BOOL)
                  .syncWith(PacketCodecs.BOOL.cast(), AttachmentSyncPredicate.all()));

  public static final SoundEvent ENTER_SOUND = sound("power.bat_form.enter");
  public static final SoundEvent LEAVE_SOUND = sound("power.bat_form.leave");

  static final double FLOOR_PROBE = 0.1;

  private static final TravelLog FLIGHTS = new TravelLog(DeedKinds.BAT_FLOWN);

  private BatForm() {}

  public static void register() {
    BatFormLimits.register();
    BatHang.register();
    ServerLivingEntityEvents.AFTER_DAMAGE.register(BatForm::afterDamage);
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> FLIGHTS.forget(handler.getPlayer().getUuid()));
  }

  private static SoundEvent sound(final String path) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, path);
    return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
  }

  public static boolean isBat(final Entity entity) {
    return entity.getAttachedOrElse(TYPE, false);
  }

  public static EntityDimensions dimensions() {
    return NightbornMobs.VAMPIRE_BAT.getDimensions();
  }

  public static void enter(final LivingEntity vampire) {
    if (isBat(vampire)) {
      return;
    }
    vampire.stopRiding();
    FeedingSessions.end(vampire);
    Levitation.land(vampire);
    vampire.setAttached(TYPE, true);
    vampire.calculateDimensions();
    if (vampire instanceof ServerPlayerEntity player) {
      stopCreativeFlight(player);
      MilestoneCriterion.reach(player, Milestone.BAT_FORM);
    } else {
      vampire.setNoGravity(true);
    }
    SmokePoof.burst(vampire, ENTER_SOUND);
    VampireSigns.shown(vampire, SuspicionCause.SEEN_IN_BAT_FORM);
  }

  public static boolean leave(final LivingEntity vampire) {
    if (!isBat(vampire)) {
      return true;
    }
    final Vec3d feet = standingFeet(vampire);
    if (!vampire.getWorld().isSpaceEmpty(vampire, standingDimensions(vampire).getBoxAt(feet))) {
      return false;
    }
    if (BatHang.isHanging(vampire)) {
      BatHang.release(vampire);
      vampire.requestTeleport(feet.x, feet.y, feet.z);
    }
    vampire.removeAttached(TYPE);
    vampire.calculateDimensions();
    if (vampire instanceof ServerPlayerEntity player) {
      player.interactionManager.getGameMode().setAbilities(player.getAbilities());
      player.sendAbilitiesUpdate();
    } else {
      vampire.setNoGravity(false);
    }
    SmokePoof.burst(vampire, LEAVE_SOUND);
    return true;
  }

  public static void tick(final LivingEntity vampire, final boolean downed) {
    if (!isBat(vampire)) {
      return;
    }
    final Optional<BatFormPower> form = Powers.ownedBatForm(vampire);
    if (BatFormRules.forcedOut(
            DirectSun.on(vampire),
            downed,
            form.map(power -> BatFormRules.warded(Weakened.sources(vampire), power.forcedBy()))
                .orElse(false),
            BloodAttachment.get(vampire),
            form.isPresent())
        && leave(vampire)) {
      return;
    }
    vampire.fallDistance = 0.0F;
    if (vampire instanceof ServerPlayerEntity player) {
      BatHang.tick(player);
      FLIGHTS.step(player);
    }
  }

  public static BatEffort effort(final LivingEntity vampire) {
    return BatEffort.of(
        isBat(vampire), BatHang.isHanging(vampire) || perched(vampire), vampire.isSprinting());
  }

  private static boolean perched(final LivingEntity vampire) {
    return vampire instanceof PlayerEntity
        && BatFormRules.perched(
            vampire.isOnGround(),
            !vampire
                .getWorld()
                .isSpaceEmpty(vampire, vampire.getBoundingBox().offset(0.0, -FLOOR_PROBE, 0.0)));
  }

  private static void stopCreativeFlight(final ServerPlayerEntity player) {
    if (player.getAbilities().flying) {
      player.getAbilities().flying = false;
      player.sendAbilitiesUpdate();
    }
  }

  private static EntityDimensions standingDimensions(final LivingEntity vampire) {
    return vampire instanceof BatShifter shifter
        ? shifter.standingDimensions()
        : PlayerEntity.STANDING_DIMENSIONS.scaled(vampire.getScale());
  }

  private static Vec3d standingFeet(final LivingEntity vampire) {
    return BatHang.anchor(vampire)
        .map(anchor -> BatHangRules.standingFeet(anchor, standingDimensions(vampire).height()))
        .orElse(vampire.getPos());
  }

  private static void afterDamage(
      final LivingEntity entity,
      final DamageSource source,
      final float baseDamageTaken,
      final float damageTaken,
      final boolean blocked) {
    final CoreSettings settings = ServerConfigService.current().core();
    if (isBat(entity) && BatFormRules.heavyHit(damageTaken, settings.batHeavyDamage())) {
      leave(entity);
    }
  }
}
