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
  static final int TITLE = 0xBEE0EA;
  static final Optional<SkillTooltip.Power> RUSH = Optional.of(new SkillTooltip.Power(3, 600));

  static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  static final Identifier ELDER = Identifier.of("nightborn", "elder");

  static SkillTooltip.Facts facts(
      final List<Identifier> requires, final Optional<SkillTooltip.Power> power) {
    return new SkillTooltip.Facts(2, 0, 1, requires, Optional.empty(), power);
  }

  static Stream<Arguments> skills() {
    return Stream.of(
        Arguments.of(
            "passive", facts(List.of(), Optional.empty()), List.of("nightborn.skills.cost")),
        Arguments.of(
            "with a requirement",
            facts(List.of(NEED), Optional.empty()),
            List.of("nightborn.skills.cost", "nightborn.skills.needs")),
        Arguments.of(
            "active",
            facts(List.of(NEED), RUSH),
            List.of("nightborn.skills.cost", "nightborn.skills.needs", "nightborn.skills.power")),
        Arguments.of(
            "with ranks left to buy",
            new SkillTooltip.Facts(2, 1, 3, List.of(), Optional.empty(), Optional.empty()),
            List.of("nightborn.skills.rank", "nightborn.skills.cost")),
        Arguments.of(
            "at its last rank",
            new SkillTooltip.Facts(3, 3, 3, List.of(), Optional.empty(), Optional.empty()),
            List.of("nightborn.skills.rank")),
        Arguments.of(
            "needing a stage",
            new SkillTooltip.Facts(3, 0, 1, List.of(NEED), Optional.of(ELDER), Optional.empty()),
            List.of("nightborn.skills.cost", "nightborn.skills.needs", "nightborn.skills.stage")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("skills")
  @DisplayName(
      "before buying, a skill shows what it does, its ranks, its cost, what it needs and its stage")
  void showsWhatItDoes(
      final String name, final SkillTooltip.Facts facts, final List<String> shown) {
    final List<Text> lines = SkillTooltip.lines(ID, facts, SkillPurchase.Verdict.ALLOWED, TITLE);
    final List<String> expected =
        Stream.concat(Stream.of(SkillNames.skill(ID), SkillNames.description(ID)), shown.stream())
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
        key(SkillTooltip.status(verdict, 0)));
  }

  @ParameterizedTest(name = "rank {0} held: {1}")
  @CsvSource({"0, nightborn.skills.click_to_buy", "1, nightborn.skills.click_to_rank"})
  @DisplayName("a skill with a rank already held offers its next rank")
  void nextRank(final int rank, final String key) {
    assertEquals(key, key(SkillTooltip.status(SkillPurchase.Verdict.ALLOWED, rank)));
  }

  @ParameterizedTest(name = "rank {0} of a power: hint shown {1}")
  @CsvSource({"0, false", "1, true"})
  @DisplayName("a power the player owns says it can be dragged onto the bar")
  void dragHint(final int rank, final boolean hinted) {
    final List<Text> lines =
        SkillTooltip.lines(
            ID,
            new SkillTooltip.Facts(2, rank, 1, List.of(), Optional.empty(), RUSH),
            rank > 0 ? SkillPurchase.Verdict.OWNED : SkillPurchase.Verdict.ALLOWED,
            TITLE);
    assertEquals(hinted, key(lines.get(lines.size() - 1)).equals("nightborn.skills.bar.drag"));
  }

  @Test
  @DisplayName("the stage line names the stage the skill needs")
  void stageName() {
    final Text line =
        SkillTooltip.lines(
                ID,
                new SkillTooltip.Facts(3, 0, 1, List.of(), Optional.of(ELDER), Optional.empty()),
                SkillPurchase.Verdict.NEEDS_STAGE,
                TITLE)
            .get(3);
    assertEquals(
        SkillNames.stage(ELDER),
        key((Text) ((TranslatableTextContent) line.getContent()).getArgs()[0]));
  }

  @Test
  @DisplayName("the power line shows its blood cost and cooldown in seconds")
  void powerArgs() {
    final Text line =
        SkillTooltip.lines(ID, facts(List.of(), RUSH), SkillPurchase.Verdict.ALLOWED, TITLE).get(3);
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

  @ParameterizedTest(name = "title colour {0}")
  @CsvSource({"0xE6C77A", "0xBEE0EA"})
  @DisplayName("the skill name is drawn in the colour of the tree's theme")
  void titleColour(final String colour) {
    final int rgb = Integer.decode(colour);
    final Text title =
        SkillTooltip.lines(ID, facts(List.of(), Optional.empty()), SkillPurchase.Verdict.OWNED, rgb)
            .get(0);
    assertEquals(rgb, title.getStyle().getColor().getRgb());
  }
}
