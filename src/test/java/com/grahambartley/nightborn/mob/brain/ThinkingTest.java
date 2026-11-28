package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
    assertEquals(offered, Thinking.offered(dodgePower, dodgeThink, intent, stuck));
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
    assertEquals(expected, Thinking.aims(use.effectiveUses(), intent));
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

  @ParameterizedTest(name = "dodge {0}, target {1}, reported {2}: aids {3}")
  @CsvSource({
    "false, false, REST, true",
    "false, false, TRAVEL, true",
    "false, false, RETREAT, false",
    "false, true, FIGHT, false",
    "true, false, REST, false"
  })
  @DisplayName(
      "a mob with no target of its own thinks about helping its allies, unless it is getting away"
          + " or dodging")
  void aids(final boolean dodge, final boolean target, final Intent reported, final boolean aids) {
    assertEquals(aids, Thinking.aids(dodge, target, reported));
  }

  private static PowerChoice.Option helpOption(
      final String name, final String aim, final String fact, final Set<Aim> ready) {
    return new PowerChoice.Option(
        Identifier.of("nightborn", "test/" + name),
        PowerUse.Verdict.READY,
        0,
        0,
        false,
        new MobPowerUse(
            0.0F,
            8.0F,
            5,
            Identifier.of("minecraft", "x"),
            "tell",
            false,
            List.of(
                new MobUse(
                    "fight",
                    aim,
                    List.of(new Consideration(fact, Curve.BETWEEN, 0.0F, 1.0F)),
                    1.0F))),
        ready);
  }

  @Test
  @DisplayName(
      "a mob standing by weighs only the powers that help an ally and are ready for that aim")
  void helpingKeepsOnlyReadyHelp() {
    final PowerChoice.Option mend = helpOption("mend", "ally", "ally_health", EnumSet.of(Aim.ALLY));
    final PowerChoice.Option strike =
        helpOption("strike", "target", "target_distance", EnumSet.of(Aim.TARGET));
    final PowerChoice.Option unready =
        helpOption("ward", "ally", "ally_under_attack", EnumSet.noneOf(Aim.class));
    assertEquals(List.of(mend), Thinking.helping(List.of(mend, strike, unready), Intent.FIGHT));
    assertEquals(List.of(), Thinking.helping(List.of(mend), Intent.RETREAT));
    assertEquals(true, Thinking.helps(mend.uses()));
    assertEquals(false, Thinking.helps(strike.uses()));
  }

  @ParameterizedTest(name = "helps {0}, {1}, scale {2}, fight think {3}: every {4}")
  @CsvSource({
    "false, FIGHT, 4, 10, 10",
    "false, CLOSE_IN, 4, 10, 10",
    "false, TRAVEL, 4, 10, 40",
    "false, REST, 4, 10, 40",
    "false, RETREAT, 1, 10, 10",
    "true, REST, 4, 10, 10",
    "true, TRAVEL, 4, 25, 25"
  })
  @DisplayName(
      "outside a fight a mob thinks its intent's scale slower, unless it could help an ally in"
          + " one")
  void paced(
      final boolean helps,
      final Intent intent,
      final int scale,
      final int fightTicks,
      final int ticks) {
    assertEquals(
        ticks,
        Thinking.paced(helps, intent, new IntentBudget(0, 0, 0.0F, 0.0F, 0.0F, scale), fightTicks));
  }

  @ParameterizedTest(name = "dodge {0}, target {1}, player near {2}: thinks {3}")
  @CsvSource({
    "false, false, true, true",
    "false, false, false, false",
    "false, true, false, true",
    "true, false, false, true"
  })
  @DisplayName("a mob with nothing to fight thinks only while a player is near enough to see it")
  void thinksAlone(
      final boolean dodge, final boolean target, final boolean watched, final boolean thinks) {
    assertEquals(thinks, Thinking.thinksAlone(dodge, target, watched));
  }

  private static final Identifier LEAP = Identifier.of("nightborn", "leap");
  private static final Identifier SIDESTEP = Identifier.of("nightborn", "sidestep");
  private static final Identifier UNKNOWN = Identifier.of("nightborn", "unknown");

  private static Optional<MobAction> data(final Identifier id) {
    if (id.equals(LEAP)) {
      return Optional.of(new MobAction(List.of(new MobUse("fight", "target", List.of(), 1.0F))));
    }
    return id.equals(SIDESTEP)
        ? Optional.of(
            new MobAction(
                List.of(
                    new MobUse("fight", "target", List.of(), 1.0F),
                    new MobUse("retreat", "target", List.of(), 1.0F))))
        : Optional.empty();
  }

  @ParameterizedTest(name = "{0}: [{1}]")
  @CsvSource({
    "FIGHT, 'nightborn:leap nightborn:sidestep'",
    "RETREAT, 'nightborn:sidestep'",
    "TRAVEL, ''"
  })
  @DisplayName(
      "a built-in action joins the options like a power, only for an intent it has a use for, and"
          + " an action with no data is left out")
  void actions(final Intent intent, final String expected) {
    final List<PowerChoice.Option> options =
        Thinking.actions(
            List.of(
                ActionReadiness.of(LEAP, true, 60),
                ActionReadiness.of(SIDESTEP, true, 60),
                ActionReadiness.of(UNKNOWN, true, 60)),
            ThinkingTest::data,
            intent);
    assertEquals(
        expected.isEmpty()
            ? List.of()
            : Arrays.stream(expected.split(" ")).map(Identifier::of).toList(),
        options.stream().map(PowerChoice.Option::id).toList());
    options.forEach(option -> assertEquals(PowerChoice.Source.ACTION, option.source()));
  }

  @ParameterizedTest(name = "ready {0}")
  @CsvSource({"true, READY, 'TARGET SELF ALLY ALLIES FOES SPOT'", "false, COOLING, ''"})
  @DisplayName("a built-in action is ready for every aim while it can be used and none while not")
  void actionAims(final boolean ready, final PowerUse.Verdict verdict, final String aims) {
    final PowerChoice.Option option =
        Thinking.actions(
                List.of(ActionReadiness.of(LEAP, ready, 60)), ThinkingTest::data, Intent.FIGHT)
            .getFirst();
    assertEquals(verdict, option.verdict());
    assertEquals(0, option.cost());
    assertEquals(60, option.cooldownTicks());
    assertEquals(
        aims.isEmpty()
            ? Set.of()
            : Arrays.stream(aims.split(" ")).map(Aim::valueOf).collect(Collectors.toSet()),
        option.readyAims());
  }
}
