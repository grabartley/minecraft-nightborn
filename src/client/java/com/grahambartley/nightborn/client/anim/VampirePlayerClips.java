package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.client.vomit.VomitFx;
import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.VampirePose;
import com.grahambartley.nightborn.network.PlayerCue;
import com.grahambartley.nightborn.network.PlayerCuePayload;
import com.grahambartley.nightborn.vampire.VisibleState;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

public final class VampirePlayerClips {

  private static final Map<PlayerEntity, PlayerClipState> STATES = new WeakHashMap<>();

  private VampirePlayerClips() {}

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
                                VisibleStateAttachment.get(player).downed())));
    ClientPlayNetworking.registerGlobalReceiver(
        PlayerCuePayload.ID,
        (payload, context) -> {
          final Entity entity =
              context.client().world == null
                  ? null
                  : context.client().world.getEntityById(payload.entityId());
          if (entity instanceof PlayerEntity player) {
            cued(player, payload.cue());
          }
        });
  }

  public static Optional<VampirePose> clip(final PlayerEntity player) {
    final VisibleState visible = VisibleStateAttachment.get(player);
    return state(player)
        .clip(
            player.age,
            new PlayerClipState.Situation(
                Levitation.isLevitating(player),
                player.isSprinting(),
                Math.hypot(player.getX() - player.prevX, player.getZ() - player.prevZ),
                visible.downed(),
                visible.feeding(),
                visible.isWeakened() && staggersOnFoot(player),
                CoffinRise.pose(player)),
            new PlayerClipState.Lengths(
                VampireClips.seconds(VampirePose.LAND),
                VampireClips.seconds(VampirePose.VOMIT),
                VampireClips.seconds(VampirePose.DRAIN),
                VampireClips.seconds(VampirePose.RISE),
                VampireClips.seconds(VampirePose.FLINCH)));
  }

  public static Optional<VampirePose> action(final PlayerEntity player) {
    return clip(player)
        .filter(VampirePose::allowsActions)
        .flatMap(
            pose ->
                VampirePose.action(
                    player.handSwinging,
                    player.isUsingItem(),
                    (player.getActiveHand() == Hand.MAIN_HAND)
                        == (player.getMainArm() == Arm.RIGHT)));
  }

  public static boolean showsHeldItems(final PlayerEntity player) {
    return clip(player).map(VampirePose::showsHeldItems).orElse(true);
  }

  public static double vomitingSeconds(final PlayerEntity player, final float partialTick) {
    return state(player).vomitingSeconds(player.age + partialTick);
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
      case FLINCH -> state(player).flinch(player.age);
    }
  }

  private static PlayerClipState state(final PlayerEntity player) {
    return STATES.computeIfAbsent(player, ignored -> new PlayerClipState());
  }
}
