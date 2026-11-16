package com.grahambartley.nightborn.batform;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.config.CoreSettings;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.BatFormPower;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.smoke.SmokeParticles;
import com.grahambartley.nightborn.sun.DirectSun;
import com.grahambartley.nightborn.weakened.Weakened;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

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

  static final int SMOKE_COUNT = 30;
  static final double SMOKE_SPEED = 0.02;

  private BatForm() {}

  public static void register() {
    BatFormLimits.register();
    ServerLivingEntityEvents.AFTER_DAMAGE.register(BatForm::afterDamage);
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
      BatFlight.lift(player);
      MilestoneCriterion.reach(player, Milestone.BAT_FORM);
    } else {
      vampire.setNoGravity(true);
    }
    poof(vampire, ENTER_SOUND);
  }

  public static boolean leave(final LivingEntity vampire) {
    if (!isBat(vampire)) {
      return true;
    }
    if (!roomToStand(vampire)) {
      return false;
    }
    vampire.removeAttached(TYPE);
    vampire.calculateDimensions();
    if (vampire instanceof ServerPlayerEntity player) {
      BatFlight.ground(player);
    } else {
      vampire.setNoGravity(false);
    }
    poof(vampire, LEAVE_SOUND);
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
      BatFlight.keep(player);
    }
  }

  static boolean roomToStand(final LivingEntity vampire) {
    final EntityDimensions standing =
        vampire instanceof BatShifter shifter
            ? shifter.standingDimensions()
            : PlayerEntity.STANDING_DIMENSIONS.scaled(vampire.getScale());
    return vampire.getWorld().isSpaceEmpty(vampire, standing.getBoxAt(vampire.getPos()));
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

  private static void poof(final LivingEntity vampire, final SoundEvent sound) {
    if (!(vampire.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final EntityDimensions shape = vampire.getDimensions(vampire.getPose());
    world.spawnParticles(
        SmokeParticles.VAMPIRE_SMOKE,
        vampire.getX(),
        vampire.getBodyY(0.5),
        vampire.getZ(),
        SMOKE_COUNT,
        shape.width() / 2.0,
        shape.height() / 2.0,
        shape.width() / 2.0,
        SMOKE_SPEED);
    world.playSound(
        null,
        vampire.getX(),
        vampire.getY(),
        vampire.getZ(),
        sound,
        vampire.getSoundCategory(),
        1.0F,
        1.0F);
  }
}
