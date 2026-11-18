package com.grahambartley.nightborn.moment;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.FactionSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;

public final class Moments {

  static final Identifier STILL = Identifier.of(Nightborn.MOD_ID, "moment_still");
  static final Identifier GROUNDED = Identifier.of(Nightborn.MOD_ID, "moment_grounded");
  static final int TICKS_PER_SECOND = 20;
  static final int PARTICLES = 24;
  static final double PARTICLE_SPREAD = 0.8;

  private Moments() {}

  public static void register() {
    MomentAttachment.register();
    ServerTickEvents.END_SERVER_TICK.register(Moments::tick);
    ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !locked(entity));
    AttackEntityCallback.EVENT.register(
        (player, world, hand, entity, hit) ->
            locked(player) ? ActionResult.FAIL : ActionResult.PASS);
    AttackBlockCallback.EVENT.register(
        (player, world, hand, pos, direction) ->
            locked(player) ? ActionResult.FAIL : ActionResult.PASS);
    UseBlockCallback.EVENT.register(
        (player, world, hand, hit) -> locked(player) ? ActionResult.FAIL : ActionResult.PASS);
    UseEntityCallback.EVENT.register(
        (player, world, hand, entity, hit) ->
            locked(player) ? ActionResult.FAIL : ActionResult.PASS);
    UseItemCallback.EVENT.register(
        (player, world, hand) ->
            locked(player)
                ? TypedActionResult.fail(player.getStackInHand(hand))
                : TypedActionResult.pass(player.getStackInHand(hand)));
  }

  public static boolean locked(final LivingEntity entity) {
    return MomentAttachment.get(entity) != null;
  }

  public static MomentState begin(final ServerPlayerEntity player, final Moment moment) {
    final FactionSettings settings = ServerConfigService.current().factions();
    player.stopRiding();
    final MomentState state =
        new MomentState(
            moment,
            player.getServerWorld().getTime(),
            player.getYaw(),
            player.getPos(),
            settings.ceremonySeconds() * TICKS_PER_SECOND,
            settings.titleCardSeconds() * TICKS_PER_SECOND);
    MomentAttachment.set(player, state);
    hold(player, true);
    pin(player, state);
    return state;
  }

  private static void tick(final MinecraftServer server) {
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      final MomentState state = MomentAttachment.get(player);
      if (state != null) {
        tick(player, state);
      }
    }
  }

  private static void tick(final ServerPlayerEntity player, final MomentState state) {
    final int elapsed = (int) (player.getServerWorld().getTime() - state.startedAt());
    if (state.phase(elapsed) == MomentState.Phase.OVER) {
      MomentAttachment.set(player, null);
      hold(player, false);
      return;
    }
    hold(player, true);
    pin(player, state);
    for (final MomentCue cue :
        MomentSchedule.sounding(state.moment(), elapsed, state.cameraTicks())) {
      show(player, state.moment(), cue);
    }
  }

  private static void show(
      final ServerPlayerEntity subject, final Moment moment, final MomentCue cue) {
    final int range = ServerConfigService.current().factions().ceremonyRange();
    for (final ServerPlayerEntity viewer : subject.getServerWorld().getPlayers()) {
      if (viewer != subject
          && !moment.seenBy(Vampires.isVampire(viewer), viewer.squaredDistanceTo(subject), range)) {
        continue;
      }
      viewer.networkHandler.sendPacket(
          new PlaySoundS2CPacket(
              cue.sound(),
              SoundCategory.PLAYERS,
              subject.getX(),
              subject.getY(),
              subject.getZ(),
              cue.volume(),
              cue.pitch(),
              subject.getRandom().nextLong()));
      subject
          .getServerWorld()
          .spawnParticles(
              viewer,
              cue.particle(),
              false,
              subject.getX(),
              subject.getBodyY(0.5),
              subject.getZ(),
              PARTICLES,
              PARTICLE_SPREAD,
              PARTICLE_SPREAD,
              PARTICLE_SPREAD,
              0.0);
    }
  }

  private static void pin(final ServerPlayerEntity player, final MomentState state) {
    if (player.hasVehicle()) {
      player.stopRiding();
    }
    if (player.isFallFlying()) {
      player.stopFallFlying();
    }
    if (player.getAbilities().flying) {
      player.getAbilities().flying = false;
      player.sendAbilitiesUpdate();
    }
    player.setVelocity(Vec3d.ZERO);
    if (state.drifted(player.getPos())) {
      player.requestTeleport(state.at().getX(), state.at().getY(), state.at().getZ());
    }
  }

  private static void hold(final LivingEntity entity, final boolean held) {
    hold(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, STILL, held);
    hold(entity, EntityAttributes.GENERIC_JUMP_STRENGTH, GROUNDED, held);
  }

  private static void hold(
      final LivingEntity entity,
      final RegistryEntry<EntityAttribute> attribute,
      final Identifier id,
      final boolean held) {
    final EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
    if (instance == null) {
      return;
    }
    if (!held) {
      instance.removeModifier(id);
    } else if (!instance.hasModifier(id)) {
      instance.addTemporaryModifier(
          new EntityAttributeModifier(
              id, -1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
  }
}
