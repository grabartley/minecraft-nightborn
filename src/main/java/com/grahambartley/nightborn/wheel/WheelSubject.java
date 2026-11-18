package com.grahambartley.nightborn.wheel;

import com.grahambartley.nightborn.skill.SkillTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class WheelSubject<T extends Entity> {

  private final Class<T> type;
  private final SkillTree theme;
  private final List<WheelAction<? super T>> actions = new CopyOnWriteArrayList<>();

  public WheelSubject(final Class<T> type, final SkillTree theme) {
    this.type = type;
    this.theme = theme;
  }

  public WheelSubject<T> add(final WheelAction<? super T> action) {
    if (actions.stream().anyMatch(known -> known.id().equals(action.id()))) {
      throw new IllegalArgumentException("Wheel action " + action.id() + " is already added");
    }
    actions.add(action);
    return this;
  }

  public SkillTree theme() {
    return theme;
  }

  public Optional<T> cast(final Entity entity) {
    return type.isInstance(entity) ? Optional.of(type.cast(entity)) : Optional.empty();
  }

  public List<WheelEntry> entries(final ServerPlayerEntity player, final T target) {
    final List<WheelEntry> entries = new ArrayList<>();
    for (final WheelAction<? super T> action : actions) {
      if (action.allows(player, target)) {
        entries.addAll(action.entries(player, target));
      }
    }
    return entries;
  }

  public boolean perform(final ServerPlayerEntity player, final T target, final WheelEntry chosen) {
    for (final WheelAction<? super T> action : actions) {
      if (action.id().equals(chosen.action()) && action.allows(player, target)) {
        action.perform(player, target, chosen);
        return true;
      }
    }
    return false;
  }

  public boolean has(final Identifier action) {
    return actions.stream().anyMatch(known -> known.id().equals(action));
  }
}
