package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.client.vomit.VomitFx;
import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.gear.GearTags;
import com.grahambartley.nightborn.gear.StakedFall;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.LevitationGait;
import com.grahambartley.nightborn.mob.VampirePose;
import com.grahambartley.nightborn.network.PlayerCue;
import com.grahambartley.nightborn.network.PlayerCuePayload;
import com.grahambartley.nightborn.network.PowerClipPayload;
import com.grahambartley.nightborn.sun.SunFire;
import com.grahambartley.nightborn.vampire.VisibleState;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

public final class PlayerClips {

  public static final int POWER_BLEND_TICKS = 2;

  private static final double POWER_BLEND_SECONDS = POWER_BLEND_TICKS / OneShot.TICKS_PER_SECOND;

  private static final Map<PlayerEntity, PlayerClipState> STATES = new WeakHashMap<>();
  private static final Map<PlayerEntity, ThrustState> THRUSTS = new WeakHashMap<>();

  private PlayerClips() {}

  public static void register() {
    ClientTickEvents.END_WORLD_TICK.register(
        world ->
            world
                .getPlayers()
                .forEach(
                    player ->
                        state(player)
                            .tick(
                                player.age,
                                Levitation.isLevitating(player),
                                VisibleStateAttachment.get(player).downed(),
                                player.isSprinting(),
                                LevitationGait.horizontalSpeed(player))));
    ClientTickEvents.END_WORLD_TICK.register(
        world -> world.getPlayers().forEach(PlayerClips::followSwing));
    ClientPlayNetworking.registerGlobalReceiver(
        PlayerCuePayload.ID,
        (payload, context) ->
            player(context.client(), payload.entityId())
                .ifPresent(player -> cued(player, payload.cue())));
    ClientPlayNetworking.registerGlobalReceiver(
        PowerClipPayload.ID,
        (payload, context) ->
            player(context.client(), payload.entityId())
                .filter(player -> VampireClips.seconds(payload.clip()) > 0.0)
                .ifPresent(
                    player ->
                        state(player)
                            .power(payload.clip(), player.age, PlayerClips::powerSeconds)));
  }

  public static boolean shown(final PlayerEntity player) {
    return clip(player).isPresent() || power(player).isPresent() || thrust(player).isPresent();
  }

  public static Optional<VampirePose> thrust(final PlayerEntity player) {
    return PlayerClipState.upright(player.getPose(), player.hasVehicle())
        ? ThrustState.shown(
            clip(player),
            power(player).isPresent(),
            thrustState(player).playing(player.age, VampireClips::seconds))
        : Optional.empty();
  }

  public static boolean freshThrust(final PlayerEntity player) {
    return thrustState(player).takeFresh();
  }

  public static Optional<VampirePose> clip(final PlayerEntity player) {
    final VisibleState visible = VisibleStateAttachment.get(player);
    return state(player)
        .clip(
            player.age,
            new PlayerClipState.Situation(
                Levitation.isLevitating(player),
                player.isSprinting(),
                LevitationGait.horizontalSpeed(player),
                visible.downed(),
                visible.feeding(),
                visible.isWeakened() && staggersOnFoot(player),
                CoffinRise.pose(player),
                StakedFall.staked(player) && !player.isAlive(),
                SunFire.burning(player)),
            VampireClips::seconds);
  }

  public static double levitationSpread(final PlayerEntity player) {
    return state(player).levitationSpread();
  }

  public static Optional<String> power(final PlayerEntity player) {
    return PlayerClipState.upright(player.getPose(), player.hasVehicle())
        ? state(player).powerClip(player.age, PlayerClips::powerSeconds)
        : Optional.empty();
  }

  public static boolean freshPower(final PlayerEntity player) {
    return state(player).takeFreshPower();
  }

  public static FirstPersonSway sway(
      final PlayerEntity player, final Arm arm, final float partialTick) {
    if (power(player).isEmpty()) {
      final double seconds = thrustState(player).seconds(player.age + partialTick);
      return thrust(player)
          .filter(pose -> ThrustState.sways(pose, arm))
          .map(
              pose ->
                  VampireClips.sway(pose.animation(), arm, Math.max(0.0, seconds))
                      .scaled(
                          ThrustState.swayWeight(seconds, VampireClips.seconds(pose.animation()))))
          .orElse(FirstPersonSway.NONE);
    }
    final double clipSeconds =
        state(player).powerSeconds(player.age + partialTick) - POWER_BLEND_SECONDS;
    return power(player)
        .map(
            clip ->
                VampireClips.sway(clip, arm, Math.max(0.0, clipSeconds))
                    .scaled(
                        FirstPersonSway.weight(
                            clipSeconds, VampireClips.seconds(clip), POWER_BLEND_SECONDS)))
        .orElse(FirstPersonSway.NONE);
  }

  public static Optional<VampirePose> action(final PlayerEntity player) {
    return PlayerClipState.action(
        clip(player),
        power(player).isPresent(),
        thrust(player),
        player.handSwinging,
        player.isUsingItem(),
        (player.getActiveHand() == Hand.MAIN_HAND) == (player.getMainArm() == Arm.RIGHT));
  }

  public static boolean showsHeldItems(final PlayerEntity player) {
    return clip(player).map(VampirePose::showsHeldItems).orElse(true);
  }

  public static double vomitingSeconds(final PlayerEntity player, final float partialTick) {
    return state(player).vomitingSeconds(player.age + partialTick);
  }

  private static double powerSeconds(final String clip) {
    return VampireClips.seconds(clip) + POWER_BLEND_SECONDS;
  }

  private static void followSwing(final PlayerEntity player) {
    final Hand hand = player.preferredHand == null ? Hand.MAIN_HAND : player.preferredHand;
    thrustState(player)
        .swing(
            player.handSwinging,
            player.handSwingTicks,
            player.getStackInHand(hand).isIn(GearTags.REVERSE_GRIP),
            ThrustState.swingsRightArm(hand, player.getMainArm()),
            player.age,
            VampireClips::seconds);
  }

  private static Optional<PlayerEntity> player(final MinecraftClient client, final int entityId) {
    final Entity entity = client.world == null ? null : client.world.getEntityById(entityId);
    return entity instanceof PlayerEntity player ? Optional.of(player) : Optional.empty();
  }

  private static boolean staggersOnFoot(final PlayerEntity player) {
    return WeakenedStoop.onFoot(
        player.getPose(), player.isOnGround(), player.hasVehicle(), player.isUsingItem());
  }

  private static void cued(final PlayerEntity player, final PlayerCue cue) {
    switch (cue) {
      case VOMIT -> {
        state(player).vomit(player.age);
        VomitFx.started(player);
      }
      case DRAIN -> state(player).drain(player.age);
      case FINISHER -> state(player).finisher(player.age);
      default -> cue.gesture().ifPresent(pose -> state(player).gesture(pose, player.age));
    }
  }

  private static ThrustState thrustState(final PlayerEntity player) {
    return THRUSTS.computeIfAbsent(player, ignored -> new ThrustState());
  }

  private static PlayerClipState state(final PlayerEntity player) {
    return STATES.computeIfAbsent(player, ignored -> new PlayerClipState());
  }
}
