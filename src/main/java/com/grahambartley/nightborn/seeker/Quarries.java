package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.raid.Trails;
import com.grahambartley.nightborn.sight.Sights;
import com.grahambartley.nightborn.skill.PowerReach;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class Quarries {

  private record Marked(QuarryMark mark, MarkPower power) {}

  public static final AttachmentType<Long> QUARRIED =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "quarried"),
          builder -> builder.syncWith(PacketCodecs.VAR_LONG.cast(), AttachmentSyncPredicate.all()));

  private static final Map<UUID, Marked> MARKS = new HashMap<>();

  private Quarries() {}

  static void register() {
    Sights.standing(Quarries::show);
    ServerLivingEntityEvents.AFTER_DEATH.register(
        (entity, source) -> MARKS.values().removeIf(marked -> marked.mark().on(entity.getUuid())));
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> MARKS.remove(handler.getPlayer().getUuid()));
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> MARKS.clear());
  }

  public static void mark(
      final LivingEntity caster, final LivingEntity target, final MarkPower power) {
    if (!(caster instanceof ServerPlayerEntity hunter)) {
      return;
    }
    final long now = hunter.getServerWorld().getTime();
    final long until = now + power.durationTicks();
    final Marked before =
        MARKS.put(
            hunter.getUuid(),
            new Marked(
                new QuarryMark(target.getUuid(), until, power.damageShare(), power.colour()),
                power));
    if (before != null
        && !before.mark().on(target.getUuid())
        && markers(before.mark().target(), now).isEmpty()
        && hunter.getServerWorld().getEntity(before.mark().target()) instanceof Entity old) {
      old.removeAttached(QUARRIED);
    }
    target.setAttached(QUARRIED, branded(target.getAttached(QUARRIED), until));
    Trails.follow(hunter, target, true);
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
    final Marked marked = MARKS.get(hunter.getUuid());
    if (marked == null) {
      return Optional.empty();
    }
    if (!marked.mark().lasts(hunter.getServerWorld().getTime())) {
      MARKS.remove(hunter.getUuid());
      return Optional.empty();
    }
    return Optional.of(marked.mark());
  }

  static Set<UUID> markers(final UUID target, final long now) {
    return MARKS.entrySet().stream()
        .filter(entry -> entry.getValue().mark().lasts(now) && entry.getValue().mark().on(target))
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  static double share(final UUID hunter, final UUID target, final long now) {
    final Marked marked = MARKS.get(hunter);
    return marked != null && marked.mark().lasts(now) && marked.mark().on(target)
        ? marked.mark().share()
        : 0.0;
  }

  static long branded(final Long before, final long until) {
    return before == null ? until : Math.max(before, until);
  }

  public static boolean outlined(final boolean veiled, final boolean boss, final boolean inSight) {
    return !veiled && (!boss || inSight);
  }

  private static void show(final ServerPlayerEntity hunter, final Map<Integer, Integer> shown) {
    final Marked marked = MARKS.get(hunter.getUuid());
    if (of(hunter).isEmpty()
        || !(hunter.getServerWorld().getEntity(marked.mark().target())
            instanceof LivingEntity target)
        || !target.isAlive()) {
      return;
    }
    if (outlined(
        Veil.any(marked.power().hiddenBy(), target),
        !PowerReach.marks(target),
        hunter.canSee(target))) {
      shown.putIfAbsent(target.getId(), marked.mark().colour());
    }
  }
}
