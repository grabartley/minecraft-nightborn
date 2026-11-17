package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;

public record PowerLayout(List<Optional<Identifier>> slots) {

  public static final int SLOTS = 5;

  public static final PowerLayout EMPTY = new PowerLayout(List.of());

  private static final Codec<Optional<Identifier>> SLOT_CODEC =
      Codec.STRING.xmap(
          saved ->
              saved.isEmpty() ? Optional.empty() : Optional.ofNullable(Identifier.tryParse(saved)),
          slot -> slot.map(Identifier::toString).orElse(""));

  public static final Codec<PowerLayout> CODEC =
      SLOT_CODEC.listOf(0, SLOTS).xmap(PowerLayout::new, PowerLayout::slots);

  public PowerLayout {
    final List<Optional<Identifier>> padded = new ArrayList<>(SLOTS);
    for (int i = 0; i < SLOTS; i++) {
      final Optional<Identifier> slot = i < slots.size() ? slots.get(i) : Optional.empty();
      padded.add(slot.filter(skill -> !padded.contains(Optional.of(skill))));
    }
    slots = Collections.unmodifiableList(padded);
  }

  public static PowerLayout inOrder(final List<Identifier> skills) {
    return new PowerLayout(skills.stream().limit(SLOTS).map(Optional::of).toList());
  }

  public static boolean has(final int slot) {
    return slot >= 0 && slot < SLOTS;
  }

  public Optional<Identifier> at(final int slot) {
    return has(slot) ? slots.get(slot) : Optional.empty();
  }

  public int slotOf(final Identifier skill) {
    return slots.indexOf(Optional.of(skill));
  }

  public boolean isEmpty() {
    return slots.stream().allMatch(Optional::isEmpty);
  }

  public int used() {
    for (int slot = SLOTS - 1; slot >= 0; slot--) {
      if (slots.get(slot).isPresent()) {
        return slot + 1;
      }
    }
    return 0;
  }

  public PowerLayout place(final int slot, final Identifier skill) {
    if (!has(slot)) {
      return this;
    }
    final List<Optional<Identifier>> next = new ArrayList<>(slots);
    final int from = slotOf(skill);
    if (from >= 0) {
      next.set(from, slots.get(slot));
    }
    next.set(slot, Optional.of(skill));
    return new PowerLayout(next);
  }

  public PowerLayout clear(final int slot) {
    if (!has(slot)) {
      return this;
    }
    final List<Optional<Identifier>> next = new ArrayList<>(slots);
    next.set(slot, Optional.empty());
    return new PowerLayout(next);
  }

  public PowerLayout add(final Identifier skill) {
    final int free = slots.indexOf(Optional.<Identifier>empty());
    return slotOf(skill) >= 0 || free < 0 ? this : place(free, skill);
  }

  public PowerLayout keeping(final Predicate<Identifier> kept) {
    return new PowerLayout(slots.stream().map(slot -> slot.filter(kept)).toList());
  }
}
