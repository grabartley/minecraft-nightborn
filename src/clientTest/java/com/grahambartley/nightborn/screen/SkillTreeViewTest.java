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

  @ParameterizedTest(name = "deepest row {0} -> {1} rows")
  @CsvSource({"0, 1", "1, 2", "5, 6"})
  @DisplayName("the screen is as tall as the deepest skill in the shown branches")
  void rows(final int deepest, final int rows) {
    final Map<Identifier, SkillTreeView.Node> skills =
        Map.of(id("core/a"), node("core", deepest), id("tracking/a"), node("tracking", 5));
    assertEquals(rows, SkillTreeView.rows(skills, List.of(id("core"))));
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

  @ParameterizedTest(name = "from {0},{1} to {2},{3}")
  @CsvSource({"50, 40, 20, 70", "50, 40, 80, 70", "50, 40, 50, 70"})
  @DisplayName("a requirement line runs down, across and down, joined with no gaps")
  void elbow(final int fromX, final int fromY, final int toX, final int toY) {
    final List<SkillTreeView.Segment> parts = SkillTreeView.elbow(fromX, fromY, toX, toY, 2);
    final SkillTreeView.Segment down = parts.get(0);
    final SkillTreeView.Segment across = parts.get(1);
    final SkillTreeView.Segment last = parts.get(2);
    assertEquals(fromY, down.y(), "starts under the parent");
    assertEquals(toY, last.y() + last.height(), "ends on top of the child");
    assertTrue(down.y() + down.height() >= across.y(), "down meets across");
    assertTrue(across.y() + across.height() >= last.y(), "across meets the last leg");
    assertTrue(
        across.x() <= Math.min(fromX, toX) - 1
            && across.x() + across.width() >= Math.max(fromX, toX) + 1);
    assertEquals(fromX - 1, down.x());
    assertEquals(toX - 1, last.x());
  }
}
