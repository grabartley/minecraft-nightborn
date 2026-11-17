package com.grahambartley.nightborn.skill;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PowerBar {

  public static final int SLOTS = PowerLayout.SLOTS;

  private PowerBar() {}

  public static PowerLayout layout(
      @Nullable final Registry<Skill> skills, final TreeProgress progress) {
    return skills == null ? PowerLayout.EMPTY : layout(progress, active(skills));
  }

  public static PowerLayout layout(
      final TreeProgress progress, final Predicate<Identifier> active) {
    final Predicate<Identifier> usable = id -> progress.owns(id) && active.test(id);
    return progress
        .bar()
        .map(bar -> bar.keeping(usable))
        .orElseGet(
            () -> PowerLayout.inOrder(progress.ownedInOrder().stream().filter(usable).toList()));
  }

  public static Optional<TreeProgress> set(
      final TreeProgress progress,
      final Predicate<Identifier> active,
      final int slot,
      final Optional<Identifier> skill) {
    if (!PowerLayout.has(slot)
        || skill.filter(id -> !progress.owns(id) || !active.test(id)).isPresent()) {
      return Optional.empty();
    }
    final PowerLayout bar = layout(progress, active);
    return Optional.of(
        progress.withBar(skill.map(id -> bar.place(slot, id)).orElseGet(() -> bar.clear(slot))));
  }

  public static TreeProgress learned(final TreeProgress progress, final Identifier power) {
    return progress.bar().map(bar -> progress.withBar(bar.add(power))).orElse(progress);
  }

  public static Predicate<Identifier> active(final Registry<Skill> skills) {
    return id -> {
      final Skill skill = skills.get(id);
      return skill != null && skill.active();
    };
  }
}
