package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.dismember.BodyPart;
import com.grahambartley.nightborn.dismember.Dismemberment;
import com.grahambartley.nightborn.gore.Gore;
import com.grahambartley.nightborn.network.GorePayload;
import com.grahambartley.nightborn.particle.DecalSurface;
import com.grahambartley.nightborn.smoke.SmokeParticles;
import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class BloodFx {

  static final int POOL_REACH = 2;

  private BloodFx() {}

  public static void register() {
    BloodParticleFactories.register();
    WoundBleeds.register();
    ClientPlayNetworking.registerGlobalReceiver(
        GorePayload.ID,
        (payload, context) -> {
          final MinecraftClient client = context.client();
          final ClientWorld world = client.world;
          final Entity entity = world == null ? null : world.getEntityById(payload.entityId());
          if (entity != null) {
            spray(client, world, entity, payload);
          }
        });
  }

  private static Vec3d origin(
      final ClientWorld world, final Entity entity, final Gore gore, final BloodBurst burst) {
    if (gore == Gore.SEVER && entity instanceof LivingEntity living) {
      final Optional<BodyPart> cut = Dismemberment.state(living).newestCut();
      if (cut.isPresent()) {
        return WoundBleeds.site(world, living, cut.get());
      }
    }
    return entity.getPos().add(0.0, entity.getHeight() * burst.height(), 0.0);
  }

  private static void spray(
      final MinecraftClient client,
      final ClientWorld world,
      final Entity entity,
      final GorePayload payload) {
    final BloodBurst burst =
        BloodBurst.of(
            ClientStateService.current().gore(),
            client.options.getParticles().getValue(),
            payload.gore());
    if (burst.drops() == 0) {
      return;
    }
    final Random random = world.random;
    final Vec3d origin = origin(world, entity, payload.gore(), burst);
    final float yaw =
        Float.isNaN(payload.sprayYaw()) ? random.nextFloat() * 360.0F : payload.sprayYaw();
    for (int i = 0; i < burst.drops(); i++) {
      final Vec3d velocity =
          BloodSpray.velocity(
                  burst.allAround() ? random.nextFloat() * 360.0F : yaw,
                  burst.speed() * (0.5 + random.nextDouble()),
                  0.04 + random.nextDouble() * 0.08)
              .add(
                  random.nextGaussian() * burst.spread(),
                  random.nextGaussian() * burst.spread(),
                  random.nextGaussian() * burst.spread());
      BloodParticleFactories.spawnDrop(client, origin, velocity, burst.look());
    }
    if (burst.pools() > 0) {
      DecalSurface.floorBelow(world, entity.getPos(), POOL_REACH)
          .ifPresent(
              floor ->
                  BloodParticleFactories.spawnSplat(
                      client, floor, Direction.UP, burst.look(), true));
    }
    for (int i = 0; i < burst.smoke(); i++) {
      world.addParticle(
          SmokeParticles.VAMPIRE_SMOKE,
          origin.x + random.nextGaussian() * 0.2,
          origin.y + random.nextGaussian() * 0.3,
          origin.z + random.nextGaussian() * 0.2,
          0.0,
          0.03,
          0.0);
    }
  }
}
