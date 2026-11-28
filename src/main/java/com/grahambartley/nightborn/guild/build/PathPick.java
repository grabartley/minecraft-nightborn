package com.grahambartley.nightborn.guild.build;

import com.grahambartley.nightborn.mob.look.Weighted;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.DoubleSupplier;
import java.util.function.ToIntFunction;
import net.minecraft.util.Identifier;

public final class PathPick {

  private PathPick() {}

  public static List<Identifier> paths(
      final Map<Identifier, SkillBranch> branches, final SkillTree tree) {
    return new TreeMap<>(branches)
        .entrySet().stream()
            .filter(e -> e.getValue().tree() == tree && e.getValue().line())
            .filter(e -> e.getValue().learnable())
            .map(Map.Entry::getKey)
            .toList();
  }

  public static List<Identifier> pick(
      final Map<Identifier, SkillBranch> branches,
      final SkillTree tree,
      final List<Map<Identifier, Integer>> leanings,
      final DoubleSupplier rolls) {
    final List<Identifier> open = new ArrayList<>(paths(branches, tree));
    final List<Identifier> picked = new ArrayList<>();
    for (int slot = 0; slot < SkillPurchase.LINE_LIMIT && !open.isEmpty(); slot++) {
      final ToIntFunction<Identifier> weight = weights(leanings, slot);
      Weighted.pick(open, weight, rolls.getAsDouble())
          .ifPresent(
              path -> {
                picked.add(path);
                open.remove(path);
              });
    }
    return List.copyOf(picked);
  }

  private static ToIntFunction<Identifier> weights(
      final List<Map<Identifier, Integer>> leanings, final int slot) {
    if (slot >= leanings.size()) {
      return path -> 1;
    }
    final Map<Identifier, Integer> slotLeanings = leanings.get(slot);
    return path -> slotLeanings.getOrDefault(path, 0);
  }
}
