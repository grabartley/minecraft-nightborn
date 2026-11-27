package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ThinkingTest {

  private static PowerChoice.Option option(final PowerUse.Verdict verdict, final String... facts) {
    final List<MobUse> uses =
        facts.length == 0
            ? List.of()
            : List.of(
                new MobUse(
                    "fight",
                    "target",
                    Arrays.stream(facts)
                        .map(fact -> new Consideration(fact, Curve.BETWEEN, 0.0F, 1.0F))
                        .toList(),
                    1.0F));
    return new PowerChoice.Option(
        Identifier.of("nightborn", "test/power"),
        verdict,
        0,
        new MobPowerUse(0.0F, 8.0F, 5, Identifier.of("minecraft", "x"), "tell", false, uses));
  }

  @ParameterizedTest(name = "{0} with [{1}] -> reads [{2}]")
  @CsvSource({
    "READY, '', TARGET_DISTANCE",
    "READY, target_in_sight sun_on_me, TARGET_IN_SIGHT SUN_ON_ME",
    "READY, moon_phase, ''",
    "COOLING, target_in_sight, ''"
  })
  @DisplayName("a think reads only the facts a ready power's uses ask for")
  void needed(final PowerUse.Verdict verdict, final String facts, final String needed) {
    final Set<Fact> expected =
        needed.isBlank()
            ? EnumSet.noneOf(Fact.class)
            : Arrays.stream(needed.split(" ")).map(Fact::valueOf).collect(Collectors.toSet());
    assertEquals(
        expected,
        Thinking.needed(
            List.of(option(verdict, facts.isBlank() ? new String[0] : facts.split(" ")))));
  }

  @ParameterizedTest(name = "dodge power {0}, dodge think {1}, {2}, stuck {3}: offered {4}")
  @CsvSource({
    "false, false, FIGHT, false, true",
    "false, true, FIGHT, false, false",
    "true, true, FIGHT, false, true",
    "true, false, FIGHT, false, false",
    "true, false, CLOSE_IN, false, false",
    "true, false, RETREAT, false, true",
    "false, false, RETREAT, false, true",
    "true, true, RETREAT, false, true",
    "false, true, RETREAT, false, false",
    "true, false, FIGHT, true, true",
    "true, false, CLOSE_IN, true, true",
    "false, false, FIGHT, true, true",
    "false, true, FIGHT, true, false"
  })
  @DisplayName(
      "a power kept for dodging is weighed as a dodge in a fight, unless the mob is stuck, and"
          + " like any other power out of one")
  void offered(
      final boolean dodgePower,
      final boolean dodgeThink,
      final Intent intent,
      final boolean stuck,
      final boolean offered) {
    assertEquals(
        offered,
        Thinking.offered(
            new MobPowerUse(0.0F, 8.0F, 1, Identifier.of("minecraft", "x"), "dodge", dodgePower),
            dodgeThink,
            intent,
            stuck));
  }

  @ParameterizedTest(name = "{0}: aims [{1}]")
  @CsvSource({"FIGHT, TARGET SELF", "CLOSE_IN, TARGET", "RETREAT, SPOT", "REST, ''"})
  @DisplayName("a power is checked only for the aims its uses for this intent need")
  void aims(final Intent intent, final String aims) {
    final MobPowerUse use =
        new MobPowerUse(
            0.0F,
            8.0F,
            1,
            Identifier.of("minecraft", "x"),
            "tell",
            false,
            List.of(
                new MobUse("fight", "target", List.of(), 1.0F),
                new MobUse("fight", "self", List.of(), 1.0F),
                new MobUse("close_in", "target", List.of(), 1.0F),
                new MobUse("retreat", "spot", List.of(), 1.0F)));
    final Set<Aim> expected = EnumSet.noneOf(Aim.class);
    if (!aims.isEmpty()) {
      Arrays.stream(aims.split(" ")).map(Aim::valueOf).forEach(expected::add);
    }
    assertEquals(expected, Thinking.aims(use, intent));
  }

  @ParameterizedTest(name = "[{0}] -> {1}")
  @CsvSource({"READY, READY", "NOWHERE READY, READY", "COOLING NOWHERE, COOLING", "'', NOWHERE"})
  @DisplayName("a power is ready when any aim it needs is ready, else it keeps the first refusal")
  void overall(final String verdicts, final PowerUse.Verdict overall) {
    assertEquals(
        overall,
        Thinking.overall(
            verdicts.isEmpty()
                ? List.of()
                : Arrays.stream(verdicts.split(" ")).map(PowerUse.Verdict::valueOf).toList()));
  }
}
