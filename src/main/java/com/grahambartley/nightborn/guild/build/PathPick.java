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
      final DoubleSupplier rolls) {
    final List<Identifier> open = new ArrayList<>(paths(branches, tree));
    final List<Identifier> picked = new ArrayList<>();
    while (picked.size() < SkillPurchase.LINE_LIMIT && !open.isEmpty()) {
      Weighted.pick(open, path -> 1, rolls.getAsDouble())
          .ifPresent(
              path -> {
                picked.add(path);
                open.remove(path);
              });
    }
    return List.copyOf(picked);
  }
}
