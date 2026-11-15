package com.grahambartley.nightborn.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.util.Identifier;

public final class SkillDataCheck {

  private SkillDataCheck() {}

  public static List<String> problems(
      final Map<Identifier, Skill> skills, final Map<Identifier, SkillBranch> branches) {
    final List<String> problems = new ArrayList<>();
    final Map<String, Identifier> places = new HashMap<>();
    for (final Map.Entry<Identifier, Skill> entry : new TreeMap<>(skills).entrySet()) {
      final Identifier id = entry.getKey();
      final Skill skill = entry.getValue();
      final SkillBranch branch = branches.get(skill.branch());
      if (branch == null) {
        problems.add(id + " is in unknown branch " + skill.branch());
        continue;
      }
      for (final Identifier need : skill.requires()) {
        final Skill required = skills.get(need);
        if (need.equals(id)) {
          problems.add(id + " needs itself");
        } else if (required == null) {
          problems.add(id + " needs unknown skill " + need);
        } else if (branches.containsKey(required.branch())
            && branches.get(required.branch()).tree() != branch.tree()) {
          problems.add(id + " needs " + need + " from another tree");
        }
      }
      final String place = skill.branch() + " " + skill.column() + "," + skill.row();
      final Identifier taken = places.putIfAbsent(place, id);
      if (taken != null) {
        problems.add(id + " shares its place in " + place + " with " + taken);
      }
    }
    return problems;
  }
}
