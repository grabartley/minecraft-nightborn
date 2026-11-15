package com.grahambartley.nightborn.client.vomit;

import com.grahambartley.nightborn.client.anim.Heaves;
import com.grahambartley.nightborn.client.anim.VampireClips;
import com.grahambartley.nightborn.client.anim.VampirePlayerClips;
import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.vomit.VomitEffects;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class VomitFx {

  static final double HEAVE_DEGREES = 30.0;
  static final double FORWARD_SPEED = 0.11;
  static final double SPREAD = 0.045;
  static final double FALLBACK_MOUTH_DROP = 0.3;

  private static final Map<PlayerEntity, Double> LAST_SECONDS = new WeakHashMap<>();
  private static final Map<PlayerEntity, Vec3d> MOUTHS = new WeakHashMap<>();

  private VomitFx() {}

  public static void register() {
    VomitParticleFactories.register();
    ClientTickEvents.END_WORLD_TICK.register(world -> world.getPlayers().forEach(VomitFx::tick));
  }

  public static void started(final PlayerEntity player) {
    LAST_SECONDS.put(player, Double.NaN);
  }

  public static void mouth(final PlayerEntity player, final Vec3d mouth) {
    MOUTHS.put(player, mouth);
  }

  private static void tick(final PlayerEntity player) {
    if (!LAST_SECONDS.containsKey(player)) {
      return;
    }
    final double now = VampirePlayerClips.vomitingSeconds(player, 0.0F);
    final double before = LAST_SECONDS.get(player);
    for (final double ignored : Heaves.crossed(VampireClips.heaves(HEAVE_DEGREES), before, now)) {
      heave(player);
    }
    LAST_SECONDS.put(player, now);
  }

  private static void heave(final PlayerEntity player) {
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
    final float facing = (180.0F - player.bodyYaw) * MathHelper.RADIANS_PER_DEGREE;
    final Vec3d forward = new Vec3d(0.0, 0.0, -1.0).rotateY(facing);
    final Vec3d mouth =
        MOUTHS.getOrDefault(
            player,
            player.getEyePos().add(forward.multiply(0.3)).subtract(0.0, FALLBACK_MOUTH_DROP, 0.0));
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
