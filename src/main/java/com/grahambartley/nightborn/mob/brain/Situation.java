package com.grahambartley.nightborn.mob.brain;

import java.util.Collection;
import java.util.List;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

public record Situation(Intent intent, Self self, @Nullable Target target) {

  public record Self(
      float health,
      int blood,
      boolean inForm,
      boolean sunlit,
      boolean downed,
      Collection<RegistryEntry<StatusEffect>> effects) {

    public static Self of(final float health, final int blood) {
      return new Self(health, blood, false, false, false, List.of());
    }
  }

  public record Target(double distance, boolean inSight, float health) {}
}
