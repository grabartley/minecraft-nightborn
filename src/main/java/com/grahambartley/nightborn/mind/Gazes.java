package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.skill.MesmerisePower;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class Gazes {

  static final int PARTICLE_EVERY_TICKS = 3;

  private static final Map<UUID, Gaze> GAZES = new HashMap<>();

  private Gazes() {}

  static void register() {
    ServerTickEvents.END_SERVER_TICK.register(Gazes::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
  }

  public static void start(
      final LivingEntity caster, final LivingEntity target, final MesmerisePower power) {
    GAZES.put(
        caster.getUuid(),
        new Gaze(
            caster.getWorld().getRegistryKey(),
            target.getUuid(),
            caster.getWorld().getTime() + power.stareTicks(),
            power));
  }

  public static boolean gazing(final LivingEntity caster) {
    return GAZES.containsKey(caster.getUuid());
  }

  private static void tick(final MinecraftServer server) {
    if (GAZES.isEmpty()) {
      return;
    }
    final Iterator<Map.Entry<UUID, Gaze>> gazes = GAZES.entrySet().iterator();
    while (gazes.hasNext()) {
      final Map.Entry<UUID, Gaze> entry = gazes.next();
      final Gaze gaze = entry.getValue();
      final ServerWorld world = server.getWorld(gaze.world());
      final LivingEntity caster =
          world != null && world.getEntity(entry.getKey()) instanceof LivingEntity living
              ? living
              : null;
      final LivingEntity target =
          world != null && world.getEntity(gaze.target()) instanceof LivingEntity living
              ? living
              : null;
      final boolean holding =
          caster != null
              && target != null
              && caster.isAlive()
              && Mesmerise.holds(caster, target, gaze.power().range());
      final Gaze.Step step = gaze.step(holding, world == null ? 0L : world.getTime());
      if (step == Gaze.Step.HOLD) {
        if (world.getTime() % PARTICLE_EVERY_TICKS == 0) {
          Mesmerise.staring(world, target);
        }
        continue;
      }
      gazes.remove();
      if (step == Gaze.Step.LAND) {
        Mesmerise.land(caster, target, gaze.power());
      } else if (caster instanceof PlayerEntity player) {
        player.sendMessage(Text.translatable("nightborn.mesmerise.broken"), true);
      }
    }
  }

  public static void clearAll() {
    GAZES.clear();
  }
}
