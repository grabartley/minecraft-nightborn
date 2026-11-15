package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.skill.SkillNames;
import com.grahambartley.nightborn.skill.SkillPurchase;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class SkillTooltipTest {

  static final Identifier ID = Identifier.of("nightborn", "core/blood_rush");
  static final Identifier NEED = Identifier.of("nightborn", "core/thick_blood");
  static final Optional<SkillTooltip.Power> RUSH = Optional.of(new SkillTooltip.Power(3, 600));

  static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  static Stream<Arguments> skills() {
    return Stream.of(
        Arguments.of("passive", List.of(), Optional.empty(), List.of("nightborn.skills.cost")),
        Arguments.of(
            "with a requirement",
            List.of(NEED),
            Optional.empty(),
            List.of("nightborn.skills.cost", "nightborn.skills.needs")),
        Arguments.of(
            "active",
            List.of(NEED),
            RUSH,
            List.of("nightborn.skills.cost", "nightborn.skills.needs", "nightborn.skills.power")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("skills")
  @DisplayName("before buying, a skill shows what it does, what it costs and what it needs")
  void showsWhatItDoes(
      final String name,
      final List<Identifier> requires,
      final Optional<SkillTooltip.Power> power,
      final List<String> facts) {
    final List<Text> lines =
        SkillTooltip.lines(ID, 2, requires, power, SkillPurchase.Verdict.ALLOWED);
    final List<String> expected =
        Stream.concat(Stream.of(SkillNames.skill(ID), SkillNames.description(ID)), facts.stream())
            .toList();
    assertEquals(
        expected, lines.subList(0, lines.size() - 1).stream().map(SkillTooltipTest::key).toList());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillPurchase.Verdict.class)
  @DisplayName("the last line says whether it can be bought, and why not")
  void status(final SkillPurchase.Verdict verdict) {
    assertEquals(
        verdict == SkillPurchase.Verdict.ALLOWED
            ? "nightborn.skills.click_to_buy"
            : verdict.translationKey(),
        key(SkillTooltip.status(verdict)));
  }

  @Test
  @DisplayName("the power line shows its blood cost and cooldown in seconds")
  void powerArgs() {
    final Text line =
        SkillTooltip.lines(ID, 2, List.of(), RUSH, SkillPurchase.Verdict.OWNED).get(3);
    assertEquals(List.of(3, 30L), List.of(((TranslatableTextContent) line.getContent()).getArgs()));
  }

  @ParameterizedTest(name = "{0} blood")
  @CsvSource({"3, nightborn.skills.power, 2", "0, nightborn.skills.power.free, 1"})
  @DisplayName("a power that costs no blood shows only its cooldown")
  void powerKey(final int blood, final String key, final int args) {
    final Text line = SkillTooltip.powerLine(new SkillTooltip.Power(blood, 1200));
    final TranslatableTextContent content = (TranslatableTextContent) line.getContent();
    assertEquals(key, content.getKey());
    assertEquals(args, content.getArgs().length);
    assertEquals(60L, content.getArgs()[args - 1]);
  }
}
