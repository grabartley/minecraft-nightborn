package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.client.vomit.VomitFx;
import com.grahambartley.nightborn.levitation.Levitation;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;

public final class PlayerAnimator {

  private static final Map<PlayerEntity, PlayerMotion> MOTIONS = new WeakHashMap<>();
  private static boolean firstPersonArm;

  private PlayerAnimator() {}

  public static void register() {
    ClipLibrary.register();
    ClientTickEvents.END_WORLD_TICK.register(PlayerAnimator::tick);
  }

  public static void startVomiting(final PlayerEntity player) {
    MOTIONS.computeIfAbsent(player, ignored -> new PlayerMotion()).startVomiting(player.age);
  }

  public static RigPose pose(final PlayerEntity player, final float animationProgress) {
    final PlayerMotion motion = MOTIONS.get(player);
    if (motion == null || firstPersonArm) {
      return RigPose.REST;
    }
    return motion.pose(ClipLibrary.current(), animationProgress);
  }

  public static void drawingFirstPersonArm(final boolean drawing) {
    firstPersonArm = drawing;
  }

  private static void tick(final ClientWorld world) {
    final ClipSet clips = ClipLibrary.current();
    MOTIONS.keySet().removeIf(player -> player.isRemoved() || player.getWorld() != world);
    for (final PlayerEntity player : world.getPlayers()) {
      final boolean levitating = Levitation.isLevitating(player);
      PlayerMotion motion = MOTIONS.get(player);
      if (motion == null && !levitating) {
        continue;
      }
      if (motion == null) {
        motion = new PlayerMotion();
        MOTIONS.put(player, motion);
      }
      final double before = motion.vomitingSeconds(player.age - 1.0);
      motion.tick(
          player.age,
          levitating,
          Math.hypot(player.getX() - player.prevX, player.getZ() - player.prevZ),
          player.isSprinting());
      VomitFx.tick(player, motion, clips, before, motion.vomitingSeconds(player.age));
      if (motion.idle(clips, player.age)) {
        MOTIONS.remove(player);
      }
    }
  }
}
