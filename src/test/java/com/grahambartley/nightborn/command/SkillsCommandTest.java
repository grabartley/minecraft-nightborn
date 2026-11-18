package com.grahambartley.nightborn.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillsCommandTest {

  static String key(final Object text) {
    return ((TranslatableTextContent) ((Text) text).getContent()).getKey();
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "VAMPIRE, nightborn:noble, nightborn.skill_tree.vampire.lines, skill_branch.nightborn.noble",
    "HUNTER, nightborn:slayer, nightborn.skill_tree.hunter.lines, skill_branch.nightborn.slayer"
  })
  @DisplayName("a player's skills name their choices as bloodlines or paths, by tree")
  void summary(final SkillTree tree, final String line, final String label, final String chosen) {
    final TreeProgress progress =
        TreeProgress.EMPTY
            .withEarned(7)
            .buy(Identifier.of("nightborn", "x/y"), 3, Identifier.of(line));
    final TranslatableTextContent content =
        (TranslatableTextContent)
            SkillsCommand.summary(Text.literal("DevPlayer"), tree, progress).getContent();
    final Object[] args = content.getArgs();
    assertEquals("nightborn.command.player.skills", content.getKey());
    assertEquals(tree.translationKey(), key(args[1]));
    assertEquals(List.of(7, 3, 4, 1), List.of(args[2], args[3], args[4], args[5]));
    assertEquals(label, key(args[6]));
    assertEquals(chosen, key(((Text) args[7]).getSiblings().get(0)));
  }
}
