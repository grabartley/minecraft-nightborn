package com.grahambartley.nightborn.guild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;

public final class HunterDataCheck {

  private HunterDataCheck() {}

  public static List<String> problems(
      final Map<Identifier, HunterLook> looks,
      final Map<Identifier, HunterKit> kits,
      final Predicate<Identifier> isItem,
      final Predicate<Identifier> isCrossbow) {
    final List<String> problems = new ArrayList<>();
    for (final HunterRig rig : HunterRig.values()) {
      if (looks.values().stream().noneMatch(look -> look.rig() == rig)) {
        problems.add("there is no " + rig.asString() + " hunter look");
      }
    }
    if (kits.isEmpty()) {
      problems.add("there are no hunter kits");
    }
    new TreeMap<>(kits)
        .forEach(
            (id, kit) -> {
              Stream.of(Stream.of(kit.weapon()), kit.bolt().stream(), kit.crucifix().stream())
                  .flatMap(items -> items)
                  .filter(isItem.negate())
                  .forEach(item -> problems.add(id + " uses " + item + ", which is not an item"));
              if (kit.ranged() && isItem.test(kit.weapon()) && !isCrossbow.test(kit.weapon())) {
                problems.add(id + " has a bolt but " + kit.weapon() + " is not a crossbow");
              }
            });
    return problems;
  }
}
