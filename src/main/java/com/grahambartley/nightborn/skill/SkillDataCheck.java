package com.grahambartley.nightborn.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        } else if (required.branch().equals(skill.branch()) && required.row() >= skill.row()) {
          problems.add(id + " needs " + need + ", which is not on a row above it");
        }
      }
      final String place = skill.branch() + " " + skill.column() + "," + skill.row();
      final Identifier taken = places.putIfAbsent(place, id);
      if (taken != null) {
        problems.add(id + " shares its place in " + place + " with " + taken);
      }
    }
    for (final Identifier id : new TreeMap<>(skills).keySet()) {
      if (reaches(id, id, skills, new HashSet<>())) {
        problems.add(id + " needs itself through a loop of requirements");
      }
    }
    return problems;
  }

  private static boolean reaches(
      final Identifier from,
      final Identifier target,
      final Map<Identifier, Skill> skills,
      final Set<Identifier> seen) {
    final Skill skill = skills.get(from);
    if (skill == null || !seen.add(from)) {
      return false;
    }
    for (final Identifier need : skill.requires()) {
      if (need.equals(target) && !from.equals(target)) {
        return true;
      }
      if (!need.equals(from) && reaches(need, target, skills, seen)) {
        return true;
      }
    }
    return false;
  }
}
