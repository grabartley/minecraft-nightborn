package com.grahambartley.nightborn.skill;

import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class Rescues {

  private Rescues() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, base, taken, blocked) -> {
          if (entity.isAlive() && taken > 0.0F) {
            rescue(entity);
          }
        });
  }

  public static boolean due(final Optional<Float> below, final float health, final float most) {
    return below.isPresent() && most > 0.0F && health / most < below.get();
  }

  static void rescue(final LivingEntity entity) {
    for (final Identifier id : ready(entity)) {
      final Optional<SkillPower> power = Powers.power(entity, id);
      if (power.flatMap(SkillPower::boost).isEmpty() || Boosts.active(entity, id)) {
        continue;
      }
      if (due(power.get().boost().get().rescueBelow(), entity.getHealth(), entity.getMaxHealth())) {
        Powers.cast(entity, id);
      }
    }
  }

  private static List<Identifier> ready(final LivingEntity entity) {
    if (!(entity instanceof PlayerEntity)) {
      return SkillAttachment.owned(entity).ownedInOrder();
    }
    return Skills.skills(entity.getWorld().getRegistryManager())
        .map(
            registry ->
                PowerBar.layout(registry, SkillAttachment.owned(entity)).slots().stream()
                    .flatMap(Optional::stream)
                    .toList())
        .orElse(List.of());
  }
}
