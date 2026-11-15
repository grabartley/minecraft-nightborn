package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.client.vomit.VomitFx;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.VampirePose;
import com.grahambartley.nightborn.network.VomitPayload;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public final class VampirePlayerClips {

  private static final Map<PlayerEntity, PlayerClipState> STATES = new WeakHashMap<>();

  private VampirePlayerClips() {}

  public static void register() {
    ClientTickEvents.END_WORLD_TICK.register(
        world ->
            world
                .getPlayers()
                .forEach(
                    player -> state(player).tick(player.age, Levitation.isLevitating(player))));
    ClientPlayNetworking.registerGlobalReceiver(
        VomitPayload.ID,
        (payload, context) -> {
          final Entity entity =
              context.client().world == null
                  ? null
                  : context.client().world.getEntityById(payload.entityId());
          if (entity instanceof PlayerEntity player) {
            state(player).vomit(player.age);
            VomitFx.started(player);
          }
        });
  }

  public static Optional<VampirePose> clip(final PlayerEntity player) {
    if (!Vampires.isVampire(player)) {
      return Optional.empty();
    }
    return state(player)
        .clip(
            player.age,
            Levitation.isLevitating(player),
            player.isSprinting(),
            Math.hypot(player.getX() - player.prevX, player.getZ() - player.prevZ),
            VampireClips.seconds(VampirePose.LAND),
            VampireClips.seconds(VampirePose.VOMIT));
  }

  public static double vomitingSeconds(final PlayerEntity player, final float partialTick) {
    return state(player).vomitingSeconds(player.age + partialTick);
  }

  private static PlayerClipState state(final PlayerEntity player) {
    return STATES.computeIfAbsent(player, ignored -> new PlayerClipState());
  }
}
