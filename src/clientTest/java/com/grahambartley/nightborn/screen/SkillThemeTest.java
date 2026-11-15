package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class SkillThemeTest {

  static List<Identifier> sprites(final SkillTheme theme) {
    return Stream.concat(
            Stream.of(
                theme.panel(),
                theme.header(),
                theme.tooltip(),
                theme.point(),
                theme.powerSlot(),
                theme.line(false),
                theme.line(true)),
            Stream.of(SkillPurchase.Verdict.values()).map(theme::frame))
        .distinct()
        .toList();
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillTree.class)
  @DisplayName("every sprite a theme names ships in the mod, under its own tree's folder")
  void spritesExist(final SkillTree tree) {
    for (final Identifier sprite : sprites(SkillTheme.of(tree))) {
      assertTrue(sprite.getPath().startsWith("skills/" + tree.asString() + "/"), sprite.toString());
      assertNotNull(
          SkillThemeTest.class.getResource(
              "/assets/nightborn/textures/gui/sprites/" + sprite.getPath() + ".png"),
          sprite.toString());
    }
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillTree.class)
  @DisplayName(
      "each tree is themed by its own tree, so the screen never checks which tree it shows")
  void themeFollowsTree(final SkillTree tree) {
    assertEquals(tree, SkillTheme.of(tree).tree());
  }

  @Test
  @DisplayName("the vampire and hunter screens share no sprite and no title colour")
  void treesLookDifferent() {
    final SkillTheme vampire = SkillTheme.of(SkillTree.VAMPIRE);
    final SkillTheme hunter = SkillTheme.of(SkillTree.HUNTER);
    assertEquals(List.of(), sprites(vampire).stream().filter(sprites(hunter)::contains).toList());
    assertNotEquals(vampire.title(), hunter.title());
  }

  static Stream<Arguments> verdicts() {
    return Stream.of(
        Arguments.of(SkillPurchase.Verdict.OWNED, "frame_owned"),
        Arguments.of(SkillPurchase.Verdict.ALLOWED, "frame_available"),
        Arguments.of(SkillPurchase.Verdict.NEEDS_SKILL, "frame_locked"),
        Arguments.of(SkillPurchase.Verdict.TOO_FEW_POINTS, "frame_locked"),
        Arguments.of(SkillPurchase.Verdict.BLOODLINE_LOCKED, "frame_locked"));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("verdicts")
  @DisplayName("owned, buyable and locked skills each get their own frame shape")
  void frames(final SkillPurchase.Verdict verdict, final String frame) {
    for (final SkillTree tree : SkillTree.values()) {
      assertEquals(
          Identifier.of("nightborn", "skills/" + tree.asString() + "/" + frame),
          SkillTheme.of(tree).frame(verdict));
    }
  }
}
