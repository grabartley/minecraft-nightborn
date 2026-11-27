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
}
