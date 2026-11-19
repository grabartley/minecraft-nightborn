package com.grahambartley.nightborn.client.faith;

import com.grahambartley.nightborn.client.LiveCoating;
import com.grahambartley.nightborn.faith.BladeFlames;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.render.Camera;
import net.minecraft.particle.SimpleParticleType;

public final class BloodFlameFx {

  private BloodFlameFx() {}

  public static void register() {
    ParticleFactoryRegistry.getInstance()
        .register(
            BladeFlames.FLAME,
            sprites ->
                (ParticleFactory<SimpleParticleType>)
                    (type, world, x, y, z, velocityX, velocityY, velocityZ) -> {
                      final MinecraftClient client = MinecraftClient.getInstance();
                      final Camera camera = client.gameRenderer.getCamera();
                      if (FlameView.hides(
                          client.options.getPerspective().isFirstPerson(),
                          client.player != null
                              && LiveCoating.burning(client.player.getMainHandStack()),
                          camera.getPos().squaredDistanceTo(x, y, z))) {
                        return null;
                      }
                      return new BloodFlameParticle(world, x, y, z, sprites);
                    });
  }
}
