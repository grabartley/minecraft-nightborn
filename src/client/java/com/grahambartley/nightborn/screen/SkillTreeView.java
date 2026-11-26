package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Identifier;

public final class SkillTreeView {

  public record Segment(int x, int y, int width, int height) {}

  public record Node(Identifier branch, int column, int row) {}

  private SkillTreeView() {}

  public static List<Identifier> branches(
      final Map<Identifier, SkillBranch> branches, final SkillTree tree) {
    return branches.entrySet().stream()
        .filter(entry -> entry.getValue().tree() == tree && entry.getValue().learnable())
        .sorted(
            Comparator.comparingInt(
                    (Map.Entry<Identifier, SkillBranch> entry) -> entry.getValue().order())
                .thenComparing(Map.Entry::getKey))
        .map(Map.Entry::getKey)
        .toList();
  }

  public static List<Identifier> skillsIn(
      final Identifier branch, final Map<Identifier, Node> nodes) {
    return nodes.entrySet().stream()
        .filter(entry -> entry.getValue().branch().equals(branch))
        .map(Map.Entry::getKey)
        .sorted()
        .toList();
  }

  public static int lanes(final Map<Identifier, Node> nodes, final Identifier branch) {
    return nodes.values().stream()
            .filter(node -> node.branch().equals(branch))
            .mapToInt(Node::column)
            .max()
            .orElse(0)
        + 1;
  }

  public static int columns(final Map<Identifier, Node> nodes, final List<Identifier> branches) {
    return nodes.values().stream()
            .filter(node -> branches.contains(node.branch()))
            .mapToInt(Node::row)
            .max()
            .orElse(0)
        + 1;
  }

  public static SkillPurchase.Verdict look(
      final SkillPurchase.Verdict verdict, final boolean owned) {
    return owned && verdict != SkillPurchase.Verdict.ALLOWED
        ? SkillPurchase.Verdict.OWNED
        : verdict;
  }

  public static boolean dimmed(final SkillPurchase.Verdict verdict) {
    return verdict != SkillPurchase.Verdict.OWNED && verdict != SkillPurchase.Verdict.ALLOWED;
  }

  public static List<Segment> route(
      final int fromX,
      final int fromY,
      final int jointX,
      final int toX,
      final int toY,
      final int width) {
    return List.of(
        across(fromX, jointX, fromY, width),
        new Segment(
            jointX - width / 2,
            Math.min(fromY, toY) - width / 2,
            width,
            Math.abs(toY - fromY) + width),
        across(jointX, toX, toY, width));
  }

  private static Segment across(final int fromX, final int toX, final int y, final int width) {
    return new Segment(
        Math.min(fromX, toX) - width / 2, y - width / 2, Math.abs(toX - fromX) + width, width);
  }
}
