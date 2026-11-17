package com.grahambartley.nightborn.mob.stage;

import com.grahambartley.nightborn.mob.look.Weighted;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.DoubleSupplier;
import net.minecraft.util.Identifier;

public final class SkillPicks {

  private SkillPicks() {}

  public static Map<Identifier, Skill> ofTree(
      final Map<Identifier, Skill> skills,
      final Map<Identifier, SkillBranch> branches,
      final SkillTree tree) {
    final Map<Identifier, Skill> inTree = new HashMap<>();
    skills.forEach(
        (id, skill) -> {
          final SkillBranch branch = branches.get(skill.branch());
          if (branch != null && branch.tree() == tree) {
            inTree.put(id, skill);
          }
        });
    return inTree;
  }

  public static Map<Identifier, Skill> forStage(
      final Map<Identifier, Skill> tree,
      final Map<Identifier, SkillBranch> branches,
      final StageSkills stage) {
    final Map<Identifier, Skill> open = new HashMap<>();
    tree.forEach(
        (id, skill) -> {
          final SkillBranch branch = branches.get(skill.branch());
          if (branch != null && (branch.learnable() || stage.weights().containsKey(id))) {
            open.put(id, skill);
          }
        });
    return open;
  }

  public static List<Identifier> pick(
      final Map<Identifier, Skill> tree,
      final StageSkills weights,
      final int wanted,
      final DoubleSupplier rolls) {
    final List<Identifier> owned = new ArrayList<>();
    final int count = weights.wanted(tree.size(), wanted);
    final Map<Identifier, Skill> sorted = new TreeMap<>(tree);
    while (owned.size() < count) {
      final List<Identifier> open =
          sorted.entrySet().stream()
              .filter(e -> !owned.contains(e.getKey()))
              .filter(e -> owned.containsAll(e.getValue().requires()))
              .map(Map.Entry::getKey)
              .toList();
      final var next = Weighted.pick(open, weights::weight, rolls.getAsDouble());
      if (next.isEmpty()) {
        break;
      }
      owned.add(next.get());
    }
    return owned;
  }
}
