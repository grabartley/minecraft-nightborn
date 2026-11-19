package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.raid.Trails;
import com.grahambartley.nightborn.sight.Sights;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class Quarries {

  private static final Map<UUID, QuarryMark> MARKS = new HashMap<>();

  private Quarries() {}

  static void register() {
    Sights.standing(Quarries::show);
    ServerLivingEntityEvents.AFTER_DEATH.register(
        (entity, source) -> MARKS.values().removeIf(mark -> mark.on(entity.getUuid())));
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> MARKS.clear());
  }

  public static void mark(
      final LivingEntity caster, final LivingEntity target, final MarkPower power) {
    if (!(caster instanceof ServerPlayerEntity hunter)) {
      return;
    }
    MARKS.put(
        hunter.getUuid(),
        new QuarryMark(
            target.getUuid(),
            hunter.getServerWorld().getTime() + power.durationTicks(),
            power.damageShare(),
            power.colour()));
    Trails.follow(hunter, target);
    hunter.sendMessage(
        Text.translatable("nightborn.seeker.marked", target.getDisplayName())
            .formatted(Formatting.GOLD),
        true);
    if (target instanceof ServerPlayerEntity marked) {
      marked.sendMessage(
          Text.translatable("nightborn.seeker.marked_you").formatted(Formatting.DARK_RED), true);
    }
  }

  public static Optional<QuarryMark> of(final ServerPlayerEntity hunter) {
    final QuarryMark mark = MARKS.get(hunter.getUuid());
    if (mark == null) {
      return Optional.empty();
    }
    if (!mark.lasts(hunter.getServerWorld().getTime())) {
      MARKS.remove(hunter.getUuid());
      return Optional.empty();
    }
    return Optional.of(mark);
  }

  static Map<UUID, QuarryMark> all() {
    return MARKS;
  }

  private static void show(final ServerPlayerEntity hunter, final Map<Integer, Integer> shown) {
    of(hunter)
        .map(mark -> hunter.getServerWorld().getEntity(mark.target()))
        .filter(LivingEntity.class::isInstance)
        .map(LivingEntity.class::cast)
        .filter(target -> target.isAlive() && !Veils.veiled(target))
        .ifPresent(
            target -> shown.putIfAbsent(target.getId(), MARKS.get(hunter.getUuid()).colour()));
  }
}
