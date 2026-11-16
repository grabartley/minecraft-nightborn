package com.grahambartley.nightborn.mob.look;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;

public final class LookDataCheck {

  private LookDataCheck() {}

  public static List<String> problems(
      final Map<Identifier, VampireLook> looks,
      final Map<Identifier, VampireOutfit> outfits,
      final Map<Identifier, VampireBuild> builds,
      final Function<Identifier, Optional<EquipmentSlot>> slotOf) {
    final List<String> problems = new ArrayList<>();
    if (looks.isEmpty()) {
      problems.add("there are no vampire looks");
    }
    if (builds.isEmpty()) {
      problems.add("there are no vampire builds");
    }
    new TreeMap<>(looks)
        .forEach(
            (id, look) -> {
              if (look.eras().isEmpty() && VampireRig.COMMON.contains(look.rig())) {
                problems.add(id + " has no era");
              }
              for (final Identifier era : look.eras()) {
                if (outfits.values().stream()
                    .noneMatch(o -> o.rig() == look.rig() && o.era().equals(era))) {
                  problems.add(id + " has no " + look.rig().asString() + " outfit for " + era);
                }
              }
            });
    new TreeMap<>(outfits)
        .forEach(
            (id, outfit) -> {
              final Set<EquipmentSlot> worn = new HashSet<>();
              for (final VampireOutfit.Piece piece : outfit.pieces()) {
                final Optional<EquipmentSlot> slot = slotOf.apply(piece.item());
                if (slot.isEmpty()) {
                  problems.add(id + " uses " + piece.item() + ", which is not wearable");
                } else if (!worn.add(slot.get())) {
                  problems.add(id + " puts two pieces in the " + slot.get().getName() + " slot");
                }
              }
            });
    new TreeMap<>(builds)
        .forEach(
            (id, build) -> {
              if (build.minHeight() > build.maxHeight()) {
                problems.add(id + " has a lowest height above its highest");
              }
            });
    return problems;
  }
}
