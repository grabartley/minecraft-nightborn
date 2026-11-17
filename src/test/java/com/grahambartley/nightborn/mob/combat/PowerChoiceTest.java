package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PowerChoiceTest {

  private static final Identifier RUSH = Identifier.of("nightborn", "core/blood_rush");
  private static final Identifier VANISH = Identifier.of("nightborn", "core/vanish");
  private static final Identifier STARE = Identifier.of("nightborn", "noble/mesmerise");

  private static PowerChoice.Option option(
      final Identifier id, final PowerUse.Verdict verdict, final float min, final float max) {
    return option(id, verdict, min, max, false);
  }

  private static PowerChoice.Option option(
      final Identifier id,
      final PowerUse.Verdict verdict,
      final float min,
      final float max,
      final boolean dodge) {
    return new PowerChoice.Option(
        id,
        verdict,
        3,
        new MobPowerUse(
            min, max, 10, Identifier.of("minecraft", "x"), "tell", Optional.empty(), dodge));
  }

  @ParameterizedTest(name = "{0}, {1} blocks, blood {2}, floor {3}: {4}")
  @CsvSource({
    "READY, 10, 13, 10, true",
    "READY, 10, 12, 10, false",
    "READY, 10, 3, 0, true",
    "READY, 3, 20, 0, false",
    "READY, 21, 20, 0, false",
    "COOLING, 10, 20, 0, false",
    "WEAKENED, 10, 20, 0, false",
    "HELPLESS, 10, 20, 0, false",
    "THIRSTY, 10, 2, 0, false",
    "NO_POWER, 10, 20, 0, false"
  })
  @DisplayName("a vampire casts only a ready power in range that leaves its blood floor")
  void worthCasting(
      final PowerUse.Verdict verdict,
      final double distance,
      final int blood,
      final int floor,
      final boolean casts) {
    assertEquals(
        casts,
        PowerChoice.worthCasting(option(RUSH, verdict, 4.0F, 20.0F), distance, blood, floor));
  }

  @Test
  @DisplayName("the first power worth casting wins, in the order the skills were learned")
  void choosesFirstWorthCasting() {
    final List<PowerChoice.Option> options =
        List.of(
            option(STARE, PowerUse.Verdict.COOLING, 0.0F, 8.0F),
            option(RUSH, PowerUse.Verdict.READY, 4.0F, 20.0F));
    assertEquals(Optional.of(RUSH), PowerChoice.choose(options, 6, 20, 0).map(o -> o.power()));
    assertEquals(Optional.empty(), PowerChoice.choose(options, 2, 20, 0));
  }

  @ParameterizedTest(name = "vanish {0}, {1} blocks, blood {2}, floor {3}: dodges {4}, chosen {5}")
  @CsvSource({
    "READY, 5, 20, 0, VANISH, RUSH",
    "READY, 6, 20, 0, VANISH, RUSH",
    "COOLING, 6, 20, 0, NONE, RUSH",
    "NOWHERE, 6, 20, 0, NONE, RUSH",
    "READY, 40, 20, 0, NONE, NONE",
    "READY, 6, 12, 10, NONE, NONE",
    "READY, 2, 20, 0, VANISH, NONE"
  })
  @DisplayName("a dodge power is kept for dodging and never chosen as an ordinary cast")
  void dodgePowersAreKeptApart(
      final PowerUse.Verdict verdict,
      final double distance,
      final int blood,
      final int floor,
      final String dodged,
      final String chosen) {
    final List<PowerChoice.Option> options =
        List.of(
            option(VANISH, verdict, 0.0F, 32.0F, true),
            option(RUSH, PowerUse.Verdict.READY, 4.0F, 20.0F));
    assertEquals(
        dodged.equals("NONE") ? Optional.empty() : Optional.of(VANISH),
        PowerChoice.dodge(options, distance, blood, floor).map(o -> o.power()));
    assertEquals(
        chosen.equals("NONE") ? Optional.empty() : Optional.of(RUSH),
        PowerChoice.choose(options, distance, blood, floor).map(o -> o.power()));
  }
}
