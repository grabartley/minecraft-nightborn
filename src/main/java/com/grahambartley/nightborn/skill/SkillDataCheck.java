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

  private record Link(
      Identifier branch, Identifier from, Identifier to, int gap, int low, int high) {

    boolean touches(final Link other) {
      return branch.equals(other.branch)
          && gap == other.gap
          && !from.equals(other.from)
          && !to.equals(other.to)
          && low <= other.high
          && other.low <= high;
    }
  }

  private SkillDataCheck() {}

  public static List<String> problems(
      final Map<Identifier, Skill> skills, final Map<Identifier, SkillBranch> branches) {
    final List<String> problems = new ArrayList<>();
    final Map<String, Identifier> places = new HashMap<>();
    final List<Link> links = new ArrayList<>();
    for (final Map.Entry<Identifier, Skill> entry : new TreeMap<>(skills).entrySet()) {
      final Identifier id = entry.getKey();
      final Skill skill = entry.getValue();
      final SkillBranch branch = branches.get(skill.branch());
      if (branch == null) {
        problems.add(id + " is in unknown branch " + skill.branch());
        continue;
      }
      int deepest = -1;
      for (final Identifier need : skill.requires()) {
        final Skill required = skills.get(need);
        if (need.equals(id)) {
          problems.add(id + " needs itself");
        } else if (required == null) {
          problems.add(id + " needs unknown skill " + need);
        } else if (branches.containsKey(required.branch())
            && branches.get(required.branch()).tree() != branch.tree()) {
          problems.add(id + " needs " + need + " from another tree");
        } else if (required.branch().equals(skill.branch())) {
          deepest = Math.max(deepest, required.row());
          links.addAll(linksBetween(need, required, id, skill));
          problems.addAll(blockedLane(need, required, id, skill, skills));
        }
      }
      if (skill.row() != deepest + 1) {
        problems.add(
            id
                + " is at row "
                + skill.row()
                + ", but the deepest skill it needs puts it at row "
                + (deepest + 1));
      }
      final String place = skill.branch() + " " + skill.column() + "," + skill.row();
      final Identifier taken = places.putIfAbsent(place, id);
      if (taken != null) {
        problems.add(id + " shares its place in " + place + " with " + taken);
      }
    }
    for (int a = 0; a < links.size(); a++) {
      for (int b = a + 1; b < links.size(); b++) {
        if (links.get(a).touches(links.get(b))) {
          problems.add(
              "the line from "
                  + links.get(a).from()
                  + " to "
                  + links.get(a).to()
                  + " touches the line from "
                  + links.get(b).from()
                  + " to "
                  + links.get(b).to());
        }
      }
    }
    for (final Identifier id : new TreeMap<>(skills).keySet()) {
      if (reaches(id, id, skills, new HashSet<>())) {
        problems.add(id + " needs itself through a loop of requirements");
      }
    }
    return problems;
  }

  private static List<Link> linksBetween(
      final Identifier fromId, final Skill from, final Identifier toId, final Skill to) {
    final List<Link> links = new ArrayList<>();
    for (int gap = from.row(); gap < to.row() - 1; gap++) {
      links.add(new Link(to.branch(), fromId, toId, gap, from.column(), from.column()));
    }
    links.add(
        new Link(
            to.branch(),
            fromId,
            toId,
            to.row() - 1,
            Math.min(from.column(), to.column()),
            Math.max(from.column(), to.column())));
    return links;
  }

  private static List<String> blockedLane(
      final Identifier fromId,
      final Skill from,
      final Identifier toId,
      final Skill to,
      final Map<Identifier, Skill> skills) {
    return new TreeMap<>(skills)
        .entrySet().stream()
            .filter(
                entry ->
                    entry.getValue().branch().equals(to.branch())
                        && entry.getValue().column() == from.column()
                        && entry.getValue().row() > from.row()
                        && entry.getValue().row() < to.row())
            .map(entry -> entry.getKey() + " sits on the line from " + fromId + " to " + toId)
            .toList();
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
