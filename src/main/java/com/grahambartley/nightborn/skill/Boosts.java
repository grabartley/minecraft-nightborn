package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.slayer.DownBlow;
import com.grahambartley.nightborn.vampire.Modifiers;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class Boosts {

  static final String PREFIX = "boost/";

  public static final AttachmentType<Map<Identifier, Boost>> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "boosts"),
          builder -> builder.persistent(Codec.unboundedMap(Identifier.CODEC, Boost.CODEC)));

  private Boosts() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(
        (dead, source) -> {
          if (source.getAttacker() instanceof LivingEntity killer) {
            killed(killer);
          }
        });
  }

  public static void start(final LivingEntity entity, final Identifier source, final Boost boost) {
    final Map<Identifier, Boost> next = new HashMap<>(all(entity));
    next.put(source, boost);
    entity.setAttached(TYPE, Map.copyOf(next));
    apply(entity, source, boost);
  }

  public static boolean active(final LivingEntity entity, final Identifier source) {
    final Boost boost = all(entity).get(source);
    return boost != null && !boost.over(entity.getWorld().getTime());
  }

  public static List<DownBlow> downs(final LivingEntity entity) {
    final long now = entity.getWorld().getTime();
    return all(entity).values().stream()
        .filter(boost -> !boost.over(now))
        .map(Boost::downs)
        .flatMap(Optional::stream)
        .toList();
  }

  public static void end(final LivingEntity entity, final Identifier source) {
    final Boost boost = all(entity).get(source);
    if (boost == null) {
      return;
    }
    remove(entity, source, boost);
    final Map<Identifier, Boost> next = new HashMap<>(all(entity));
    next.remove(source);
    entity.setAttached(TYPE, next.isEmpty() ? null : Map.copyOf(next));
  }

  public static void tick(final LivingEntity entity) {
    final Map<Identifier, Boost> boosts = entity.getAttached(TYPE);
    if (boosts == null) {
      return;
    }
    final long now = entity.getWorld().getTime();
    boosts.forEach(
        (source, boost) -> {
          if (boost.over(now)) {
            end(entity, source);
          }
        });
  }

  static void killed(final LivingEntity killer) {
    final Map<Identifier, Boost> boosts = killer.getAttached(TYPE);
    if (boosts == null) {
      return;
    }
    final Map<Identifier, Boost> next = new HashMap<>();
    boosts.forEach((source, boost) -> next.put(source, boost.onKill()));
    killer.setAttached(TYPE, Map.copyOf(next));
  }

  static Map<Identifier, Boost> all(final LivingEntity entity) {
    return entity.getAttachedOrElse(TYPE, Map.of());
  }

  static Identifier modifierId(final Identifier source, final int index) {
    return Identifier.of(
        Nightborn.MOD_ID, PREFIX + source.getNamespace() + "/" + source.getPath() + "/" + index);
  }

  private static void apply(final LivingEntity entity, final Identifier source, final Boost boost) {
    for (int i = 0; i < boost.attributes().size(); i++) {
      final SkillAttribute attribute = boost.attributes().get(i);
      Modifiers.set(
          entity,
          attribute.attribute(),
          modifierId(source, i),
          attribute.amount(),
          attribute.operation());
    }
  }

  private static void remove(
      final LivingEntity entity, final Identifier source, final Boost boost) {
    for (int i = 0; i < boost.attributes().size(); i++) {
      Modifiers.remove(entity, boost.attributes().get(i).attribute(), modifierId(source, i));
    }
    Modifiers.clampHealth(entity);
  }
}
