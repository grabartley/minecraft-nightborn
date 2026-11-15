package com.grahambartley.nightborn.client.vomit;

import com.grahambartley.nightborn.client.anim.ClipSet;
import com.grahambartley.nightborn.client.anim.PlayerAnimator;
import com.grahambartley.nightborn.client.anim.PlayerMotion;
import com.grahambartley.nightborn.client.anim.RigSpace;
import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.network.VomitPayload;
import com.grahambartley.nightborn.vomit.VomitEffects;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class VomitFx {

  static final double HEAVE_DEGREES = 30.0;
  static final String MOUTH = "mouth";
  static final float PLAYER_MODEL_SCALE = 0.9375F;
  static final double FORWARD_SPEED = 0.11;
  static final double SPREAD = 0.045;

  private VomitFx() {}

  public static void register() {
    VomitParticleFactories.register();
    ClientPlayNetworking.registerGlobalReceiver(
        VomitPayload.ID,
        (payload, context) -> {
          final Entity entity =
              context.client().world == null
                  ? null
                  : context.client().world.getEntityById(payload.entityId());
          if (entity instanceof PlayerEntity player) {
            PlayerAnimator.startVomiting(player);
          }
        });
  }

  public static void tick(
      final PlayerEntity player,
      final PlayerMotion motion,
      final ClipSet clips,
      final double secondsBefore,
      final double secondsNow) {
    if (Double.isNaN(secondsNow)) {
      return;
    }
    for (final double heave : motion.heaves(clips, HEAVE_DEGREES)) {
      if ((Double.isNaN(secondsBefore) || secondsBefore < heave) && secondsNow >= heave) {
        heave(player, motion, clips);
      }
    }
  }

  private static void heave(
      final PlayerEntity player, final PlayerMotion motion, final ClipSet clips) {
    player
        .getWorld()
        .playSound(
            player.getX(),
            player.getY(),
            player.getZ(),
            VomitEffects.RETCH,
            SoundCategory.PLAYERS,
            1.0F,
            0.9F + player.getRandom().nextFloat() * 0.2F,
            false);
    final VomitBurst burst =
        VomitBurst.of(
            ClientStateService.current().gore(),
            MinecraftClient.getInstance().options.getParticles().getValue());
    if (burst.drops() == 0) {
      return;
    }
    final Vec3d frame =
        RigSpace.locate(clips.rig(), motion.pose(clips, player.age), MOUTH, PLAYER_MODEL_SCALE)
            .orElse(new Vec3d(0.0, player.getStandingEyeHeight() - 0.3, -0.3));
    final float facing = (180.0F - player.bodyYaw) * MathHelper.RADIANS_PER_DEGREE;
    final Vec3d mouth = player.getPos().add(frame.rotateY(facing));
    final Vec3d forward = new Vec3d(0.0, 0.0, -1.0).rotateY(facing);
    final Random random = player.getRandom();
    for (int i = 0; i < burst.drops(); i++) {
      final double speed = FORWARD_SPEED * (0.5 + random.nextDouble());
      VomitParticleFactories.spawnDrop(
          MinecraftClient.getInstance(),
          mouth,
          forward
              .multiply(speed)
              .add(
                  random.nextGaussian() * SPREAD,
                  -0.02 - random.nextDouble() * 0.05,
                  random.nextGaussian() * SPREAD),
          burst.splashes());
    }
  }
}
