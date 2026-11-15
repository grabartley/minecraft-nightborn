package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Identifier;

public final class SkillTreeView {

  static final Identifier FRAME_OWNED = Identifier.of(Nightborn.MOD_ID, "skills/frame_owned");
  static final Identifier FRAME_AVAILABLE =
      Identifier.of(Nightborn.MOD_ID, "skills/frame_available");
  static final Identifier FRAME_LOCKED = Identifier.of(Nightborn.MOD_ID, "skills/frame_locked");

  public record Segment(int x, int y, int width, int height) {}

  public record Node(Identifier branch, int column, int row) {}

  private SkillTreeView() {}

  public static List<Identifier> branches(
      final Map<Identifier, SkillBranch> branches, final SkillTree tree) {
    return branches.entrySet().stream()
        .filter(entry -> entry.getValue().tree() == tree)
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

  public static int rows(final Map<Identifier, Node> nodes, final List<Identifier> branches) {
    return nodes.values().stream()
            .filter(node -> branches.contains(node.branch()))
            .mapToInt(Node::row)
            .max()
            .orElse(0)
        + 1;
  }

  public static Identifier frame(final SkillPurchase.Verdict verdict) {
    return switch (verdict) {
      case OWNED -> FRAME_OWNED;
      case ALLOWED -> FRAME_AVAILABLE;
      default -> FRAME_LOCKED;
    };
  }

  public static boolean dimmed(final SkillPurchase.Verdict verdict) {
    return verdict != SkillPurchase.Verdict.OWNED && verdict != SkillPurchase.Verdict.ALLOWED;
  }

  public static List<Segment> elbow(
      final int fromX, final int fromY, final int toX, final int toY, final int width) {
    final int half = width / 2;
    final int midY = (fromY + toY) / 2;
    return List.of(
        new Segment(fromX - half, fromY, width, midY - fromY + half),
        new Segment(Math.min(fromX, toX) - half, midY - half, Math.abs(toX - fromX) + width, width),
        new Segment(toX - half, midY - half, width, toY - midY + half));
  }
}
