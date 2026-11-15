package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillNamesTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "nightborn:core/thick_blood, skill.nightborn.core.thick_blood",
    "nightborn:noble/noble_blood, skill.nightborn.noble.noble_blood",
    "apack:fangs, skill.apack.fangs"
  })
  @DisplayName("a skill's name and description keys follow its id")
  void skill(final String id, final String key) {
    assertEquals(key, SkillNames.skill(Identifier.of(id)));
    assertEquals(key + ".description", SkillNames.description(Identifier.of(id)));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"nightborn:noble, skill_branch.nightborn.noble", "apack:hex, skill_branch.apack.hex"})
  @DisplayName("a branch's name key follows its id")
  void branch(final String id, final String key) {
    assertEquals(key, SkillNames.branch(Identifier.of(id)));
  }

  @ParameterizedTest(name = "[{0}]")
  @CsvSource(
      delimiter = '|',
      value = {
        "''|nightborn.command.player.skills.none",
        "nightborn:noble|skill_branch.nightborn.noble",
        "nightborn:noble,nightborn:beast|skill_branch.nightborn.noble, skill_branch.nightborn.beast"
      })
  @DisplayName("chosen bloodlines are listed by their translated names, or as none")
  void bloodlines(final String ids, final String keys) {
    final List<Identifier> bloodlines =
        ids.isEmpty() ? List.of() : Stream.of(ids.split(",")).map(Identifier::of).toList();
    final Text text = SkillNames.bloodlines(bloodlines);
    final List<String> parts = new ArrayList<>();
    collect(text, parts);
    assertEquals(keys, String.join("", parts));
  }

  private static void collect(final Text text, final List<String> out) {
    if (text.getContent() instanceof TranslatableTextContent translatable) {
      out.add(translatable.getKey());
    } else if (text.getContent() instanceof PlainTextContent plain) {
      out.add(plain.string());
    }
    text.getSiblings().forEach(sibling -> collect(sibling, out));
  }
}
