package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.client.state.GoreLevel;
import com.grahambartley.nightborn.dismember.BodyPart;
import com.grahambartley.nightborn.dismember.BodyState;
import com.grahambartley.nightborn.dismember.BodyZoneData;
import com.grahambartley.nightborn.dismember.Dismemberment;
import com.grahambartley.nightborn.dismember.WoundSite;
import com.grahambartley.nightborn.sound.NightbornSounds;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class WoundBleeds {

  static final double DRAW_RANGE = 48.0;
  static final double DRIP_RANGE = 16.0;
  static final float DRIP_VOLUME = 0.5F;
  static final float DRIP_PITCH = 0.7F;
  static final float DRIP_PITCH_SPREAD = 0.1F;
  static final double PULSE_SPEED = 0.12;
  static final double PULSE_JITTER = 0.03;
  static final double PULSE_LIFT = 0.05;

  private static final WoundTrail TRAIL = new WoundTrail();

  private WoundBleeds() {}

  static void register() {
    ClientTickEvents.END_WORLD_TICK.register(WoundBleeds::tick);
  }

  static Vec3d site(final ClientWorld world, final LivingEntity entity, final BodyPart part) {
    return entity
        .getPos()
        .add(
            WoundSite.offset(
                BodyZoneData.zones(world.getRegistryManager()),
                part,
                entity.getWidth(),
                entity.getHeight(),
                entity.bodyYaw,
                entity.getPose() == EntityPose.STANDING
                    || entity.getPose() == EntityPose.CROUCHING));
  }

  private static void tick(final ClientWorld world) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client.isPaused() || client.player == null) {
      return;
    }
    final GoreLevel gore = ClientStateService.current().gore();
    final ParticlesMode particles = client.options.getParticles().getValue();
    final Vec3d eye = client.gameRenderer.getCamera().getPos();
    final long now = world.getTime();
    int spent = 0;
    TRAIL.begin(world);
    for (final Entity entity : world.getEntities()) {
      if (!(entity instanceof LivingEntity living)
          || !living.isAlive()
          || living.squaredDistanceTo(eye) > DRAW_RANGE * DRAW_RANGE) {
        continue;
      }
      final BodyState body = living.getAttached(Dismemberment.TYPE);
      if (body == null || !body.bleeding(now)) {
        continue;
      }
      final long sinceCut = now - body.lastCutAt();
      if (WoundPulse.drips(sinceCut) && living.squaredDistanceTo(eye) <= DRIP_RANGE * DRIP_RANGE) {
        world.playSound(
            living.getX(),
            living.getY(),
            living.getZ(),
            NightbornSounds.BLEEDING_DRIP,
            living.getSoundCategory(),
            DRIP_VOLUME,
            DRIP_PITCH + world.random.nextFloat() * DRIP_PITCH_SPREAD,
            false);
      }
      if (WoundPulse.shown(
          living.isInvisible(),
          living == client.player && client.options.getPerspective().isFirstPerson())) {
        spent +=
            pulse(client, world, living, body.openWounds(now), gore, particles, sinceCut, spent);
      }
      if (!living.isInvisible()) {
        TRAIL.follow(client, living, gore, particles);
      }
    }
    TRAIL.end();
  }

  private static int pulse(
      final MinecraftClient client,
      final ClientWorld world,
      final LivingEntity living,
      final List<BodyPart> wounds,
      final GoreLevel gore,
      final ParticlesMode particles,
      final long sinceCut,
      final int spent) {
    final WoundPulse beat = WoundPulse.of(gore, particles, sinceCut);
    if (beat.drops() == 0) {
      return 0;
    }
    final Random random = world.random;
    int used = 0;
    for (final BodyPart part : wounds) {
      final int drops = WoundPulse.allowed(spent + used, beat.drops());
      final Vec3d at = site(world, living, part);
      final Vec3d out = WoundSite.outward(part, living.bodyYaw).multiply(PULSE_SPEED);
      for (int i = 0; i < drops; i++) {
        BloodParticleFactories.spawnDrop(
            client,
            at,
            out.add(living.getVelocity())
                .add(
                    random.nextGaussian() * PULSE_JITTER,
                    random.nextDouble() * PULSE_LIFT,
                    random.nextGaussian() * PULSE_JITTER),
            beat.look());
      }
      used += drops;
    }
    return used;
  }
}
