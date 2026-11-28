package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.client.state.GoreLevel;
import com.grahambartley.nightborn.dismember.BodyPart;
import com.grahambartley.nightborn.dismember.BodyState;
import com.grahambartley.nightborn.dismember.BodyZoneData;
import com.grahambartley.nightborn.dismember.Dismemberment;
import com.grahambartley.nightborn.dismember.WoundSite;
import com.grahambartley.nightborn.particle.DecalSurface;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class WoundBleeds {

  static final double DRAW_RANGE = 48.0;
  static final double DRIP_RANGE = 16.0;
  static final int TRAIL_REACH = 1;
  static final float DRIP_VOLUME = 0.5F;
  static final float DRIP_PITCH = 0.7F;

  private static final Int2ObjectOpenHashMap<Vec3d> LAST_SPLAT = new Int2ObjectOpenHashMap<>();

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
                entity.bodyYaw));
  }

  private static void tick(final ClientWorld world) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client.isPaused() || client.player == null) {
      LAST_SPLAT.clear();
      return;
    }
    final GoreLevel gore = ClientStateService.current().gore();
    final ParticlesMode particles = client.options.getParticles().getValue();
    final Vec3d eye = client.gameRenderer.getCamera().getPos();
    final long now = world.getTime();
    int spent = 0;
    final Int2ObjectOpenHashMap<Vec3d> seen = new Int2ObjectOpenHashMap<>();
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
            SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER,
            living.getSoundCategory(),
            DRIP_VOLUME,
            DRIP_PITCH + world.random.nextFloat() * 0.1F,
            false);
      }
      spent += pulse(client, world, living, body.openWounds(now), gore, particles, sinceCut, spent);
      seen.put(living.getId(), trail(client, world, living, gore, particles));
    }
    LAST_SPLAT.clear();
    LAST_SPLAT.putAll(seen);
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
    final Random random = world.random;
    int used = 0;
    for (final BodyPart part : wounds) {
      final int drops = WoundPulse.allowed(spent + used, beat.drops());
      final Vec3d at = site(world, living, part);
      final Vec3d out = WoundSite.outward(part, living.bodyYaw).multiply(0.12);
      for (int i = 0; i < drops; i++) {
        BloodParticleFactories.spawnDrop(
            client,
            at,
            out.add(living.getVelocity())
                .add(
                    random.nextGaussian() * 0.03,
                    random.nextDouble() * 0.05,
                    random.nextGaussian() * 0.03),
            beat.look());
      }
      used += drops;
    }
    return used;
  }

  private static Vec3d trail(
      final MinecraftClient client,
      final ClientWorld world,
      final LivingEntity living,
      final GoreLevel gore,
      final ParticlesMode particles) {
    final Vec3d feet = living.getPos();
    final Vec3d last = LAST_SPLAT.get(living.getId());
    if (last != null && !BleedTrail.due(gore, particles, last.squaredDistanceTo(feet))) {
      return last;
    }
    if (BleedTrail.drawn(gore, particles)) {
      final double spread = living.getWidth() / 2.0;
      final Vec3d spot =
          feet.add(
              (world.random.nextDouble() - 0.5) * spread,
              0.0,
              (world.random.nextDouble() - 0.5) * spread);
      DecalSurface.floorBelow(world, spot, TRAIL_REACH)
          .ifPresent(
              floor ->
                  BloodParticleFactories.spawnSplat(
                      client, floor, Direction.UP, SplatterLook.of(gore), false));
    }
    return feet;
  }
}
