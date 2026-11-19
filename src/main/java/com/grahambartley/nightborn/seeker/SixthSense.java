package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.mob.home.WatchedStranger;
import com.grahambartley.nightborn.skill.TrackingDirection;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.wild.Wanderer;
import com.grahambartley.nightborn.wild.WandererAttachment;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class SixthSense {

  static final int WARN_EVERY_TICKS = 40;

  private SixthSense() {}

  static void register() {
    ServerTickEvents.END_SERVER_TICK.register(SixthSense::tick);
  }

  public static boolean warns(
      final boolean vampireMob,
      final boolean targets,
      final boolean watches,
      final boolean stalks) {
    return vampireMob && (targets || watches || stalks);
  }

  private static void tick(final MinecraftServer server) {
    if (server.getTicks() % WARN_EVERY_TICKS != 0) {
      return;
    }
    for (final ServerPlayerEntity hunter : server.getPlayerManager().getPlayerList()) {
      if (!hunter.isAlive() || hunter.isSpectator() || Vampires.isVampire(hunter)) {
        continue;
      }
      final float range = SeekGifts.senseRange(HeldSeekGifts.of(hunter));
      if (range > 0.0F) {
        watcher(hunter, range).ifPresent(watcher -> warn(hunter, watcher));
      }
    }
  }

  static Optional<MobEntity> watcher(final ServerPlayerEntity hunter, final float range) {
    final UUID id = hunter.getUuid();
    return hunter
        .getServerWorld()
        .getEntitiesByClass(
            MobEntity.class,
            hunter.getBoundingBox().expand(range),
            mob ->
                mob.isAlive()
                    && mob.squaredDistanceTo(hunter) <= (double) range * range
                    && warns(
                        Vampires.isVampire(mob),
                        mob.getTarget() == hunter,
                        WandererAttachment.get(mob)
                            .flatMap(Wanderer::watches)
                            .map(id::equals)
                            .orElse(false),
                        WatchedStranger.of(mob).map(id::equals).orElse(false)))
        .stream()
        .min(Comparator.comparingDouble(mob -> mob.squaredDistanceTo(hunter)));
  }

  private static void warn(final ServerPlayerEntity hunter, final MobEntity watcher) {
    hunter.sendMessage(
        Text.translatable(
                "nightborn.seeker.sixth_sense",
                TrackingDirection.arrow(
                    watcher.getX() - hunter.getX(),
                    watcher.getZ() - hunter.getZ(),
                    hunter.getYaw()))
            .formatted(Formatting.GOLD),
        true);
    hunter.playSoundToPlayer(
        SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, SoundCategory.PLAYERS, 0.6F, 0.6F);
  }
}
