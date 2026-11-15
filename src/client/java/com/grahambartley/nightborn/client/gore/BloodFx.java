package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.network.GorePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class BloodFx {

  private BloodFx() {}

  public static void register() {
    BloodParticleFactories.register();
    ClientPlayNetworking.registerGlobalReceiver(
        GorePayload.ID,
        (payload, context) -> {
          final MinecraftClient client = context.client();
          final Entity entity =
              client.world == null ? null : client.world.getEntityById(payload.entityId());
          if (entity != null) {
            spray(client, entity, payload);
          }
        });
  }

  private static void spray(
      final MinecraftClient client, final Entity entity, final GorePayload payload) {
    final BloodBurst burst =
        BloodBurst.of(
            ClientStateService.current().gore(),
            client.options.getParticles().getValue(),
            payload.gore());
    if (burst.drops() == 0) {
      return;
    }
    final Random random = entity.getRandom();
    final Vec3d origin = entity.getPos().add(0.0, entity.getHeight() * burst.height(), 0.0);
    final float yaw =
        Float.isNaN(payload.sprayYaw()) ? random.nextFloat() * 360.0F : payload.sprayYaw();
    for (int i = 0; i < burst.drops(); i++) {
      final float angle =
          (burst.allAround() ? random.nextFloat() * 360.0F : yaw) * MathHelper.RADIANS_PER_DEGREE;
      final double speed = burst.speed() * (0.5 + random.nextDouble());
      final Vec3d velocity =
          new Vec3d(
                  MathHelper.cos(angle) * speed,
                  0.04 + random.nextDouble() * 0.08,
                  MathHelper.sin(angle) * speed)
              .add(
                  random.nextGaussian() * burst.spread(),
                  random.nextGaussian() * burst.spread(),
                  random.nextGaussian() * burst.spread());
      BloodParticleFactories.spawnDrop(client, origin, velocity, burst.look());
    }
    if (burst.pools() > 0 && entity.isOnGround() && client.world != null) {
      BloodParticleFactories.spawnSplat(
          client.world,
          entity.getX(),
          entity.getY(),
          entity.getZ(),
          Direction.UP,
          burst.look(),
          true);
    }
    for (int i = 0; i < burst.smoke(); i++) {
      entity
          .getWorld()
          .addParticle(
              ParticleTypes.LARGE_SMOKE,
              origin.x + random.nextGaussian() * 0.2,
              origin.y + random.nextGaussian() * 0.3,
              origin.z + random.nextGaussian() * 0.2,
              0.0,
              0.03,
              0.0);
    }
  }
}
