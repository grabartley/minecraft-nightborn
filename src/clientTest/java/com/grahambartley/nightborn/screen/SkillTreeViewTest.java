package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class SkillTreeViewTest {

  static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  static SkillTreeView.Node node(final String branch, final int row) {
    return new SkillTreeView.Node(id(branch), 0, row);
  }

  static final Map<Identifier, SkillBranch> BRANCHES =
      Map.of(
          id("occult"), new SkillBranch(SkillTree.VAMPIRE, true, 3),
          id("core"), new SkillBranch(SkillTree.VAMPIRE, false, 0),
          id("noble"), new SkillBranch(SkillTree.VAMPIRE, true, 1),
          id("beast"), new SkillBranch(SkillTree.VAMPIRE, true, 1),
          id("tracking"), new SkillBranch(SkillTree.HUNTER, false, 0),
          id("lord"), new SkillBranch(SkillTree.VAMPIRE, false, 15, false));

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {"VAMPIRE|core,beast,noble,occult", "HUNTER|tracking"})
  @DisplayName("a tree shows its own learnable branches by order, then by id")
  void branches(final SkillTree tree, final String expected) {
    assertEquals(
        List.of(expected.split(",")).stream().map(SkillTreeViewTest::id).toList(),
        SkillTreeView.branches(BRANCHES, tree));
  }

  @Test
  @DisplayName("a branch lists only its own skills, in id order")
  void skillsIn() {
    final Map<Identifier, SkillTreeView.Node> skills =
        Map.of(
            id("core/b"), node("core", 0),
            id("core/a"), node("core", 1),
            id("noble/a"), node("noble", 0));
    assertEquals(List.of(id("core/a"), id("core/b")), SkillTreeView.skillsIn(id("core"), skills));
  }

  @ParameterizedTest(name = "deepest lane {0} -> {1} lanes")
  @CsvSource({"0, 1", "2, 3", "4, 5"})
  @DisplayName("a band is as tall as its lowest lane, whatever the other branches hold")
  void lanes(final int deepest, final int lanes) {
    final Map<Identifier, SkillTreeView.Node> skills =
        Map.of(
            id("core/a"), new SkillTreeView.Node(id("core"), deepest, 0),
            id("noble/a"), new SkillTreeView.Node(id("noble"), 4, 0));
    assertEquals(lanes, SkillTreeView.lanes(skills, id("core")));
  }

  @ParameterizedTest(name = "furthest column {0} -> {1} columns")
  @CsvSource({"0, 1", "3, 4", "6, 7"})
  @DisplayName("the trees are as wide as the furthest skill in the shown branches")
  void columns(final int furthest, final int columns) {
    final Map<Identifier, SkillTreeView.Node> skills =
        Map.of(id("core/a"), node("core", furthest), id("tracking/a"), node("tracking", 6));
    assertEquals(columns, SkillTreeView.columns(skills, List.of(id("core"))));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillPurchase.Verdict.class)
  @DisplayName("owned and buyable skills are drawn bright; the rest are locked and dim")
  void dimmed(final SkillPurchase.Verdict verdict) {
    final boolean open =
        verdict == SkillPurchase.Verdict.OWNED || verdict == SkillPurchase.Verdict.ALLOWED;
    assertEquals(!open, SkillTreeView.dimmed(verdict));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillPurchase.Verdict.class)
  @DisplayName("a skill with a rank held looks owned unless its next rank can be bought now")
  void look(final SkillPurchase.Verdict verdict) {
    assertEquals(verdict, SkillTreeView.look(verdict, false));
    assertEquals(
        verdict == SkillPurchase.Verdict.ALLOWED
            ? SkillPurchase.Verdict.ALLOWED
            : SkillPurchase.Verdict.OWNED,
        SkillTreeView.look(verdict, true));
  }

  @ParameterizedTest(name = "from {0},{1} to {3},{4} turning at {2}, {5} px")
  @CsvSource({
    "22, 11, 30, 38, 39, 3",
    "22, 39, 30, 38, 11, 3",
    "22, 11, 30, 38, 11, 3",
    "22, 11, 106, 114, 11, 1",
    "60, 95, 68, 76, 39, 1"
  })
  @DisplayName("a line runs across, turns in the gap before the skill, then runs across into it")
  void route(
      final int fromX,
      final int fromY,
      final int jointX,
      final int toX,
      final int toY,
      final int width) {
    final List<SkillTreeView.Segment> parts =
        SkillTreeView.route(fromX, fromY, jointX, toX, toY, width);
    final SkillTreeView.Segment out = parts.get(0);
    final SkillTreeView.Segment turn = parts.get(1);
    final SkillTreeView.Segment in = parts.get(2);
    final int half = width / 2;
    assertEquals(width, out.height(), "leaves as one even line");
    assertEquals(width, turn.width());
    assertEquals(width, in.height());
    assertEquals(fromX - half, out.x(), "starts at the parent's edge");
    assertEquals(fromY - half, out.y(), "on the parent's lane");
    assertEquals(toX + width - 1 - half, in.x() + in.width() - 1, "ends at the child");
    assertEquals(toY - half, in.y(), "on the child's lane");
    assertEquals(jointX - half, turn.x(), "turns in the gap");
    assertEquals(out.x() + out.width(), turn.x() + turn.width(), "the corner is square");
    assertEquals(Math.min(fromY, toY) - half, turn.y(), "the turn reaches the higher lane");
    assertEquals(Math.max(fromY, toY) + width - half, turn.y() + turn.height(), "and the lower");
  }

  @Test
  @DisplayName("a line's core sits exactly inside its edge, with one pixel of edge on each side")
  void coreInsideEdge() {
    final List<SkillTreeView.Segment> edge = SkillTreeView.route(22, 11, 30, 38, 67, 3);
    final List<SkillTreeView.Segment> core = SkillTreeView.route(22, 11, 30, 38, 67, 1);
    for (int part = 0; part < edge.size(); part++) {
      final SkillTreeView.Segment e = edge.get(part);
      final SkillTreeView.Segment c = core.get(part);
      assertTrue(c.x() >= e.x() && c.y() >= e.y());
      assertTrue(
          c.x() + c.width() <= e.x() + e.width() && c.y() + c.height() <= e.y() + e.height());
      assertEquals(e.width() * e.height() > 0, c.width() * c.height() > 0);
    }
    assertEquals(edge.get(1).x() + 1, core.get(1).x(), "one pixel of edge left of the turn");
    assertEquals(edge.get(0).y() + 1, core.get(0).y(), "one pixel of edge above the line");
  }
}
