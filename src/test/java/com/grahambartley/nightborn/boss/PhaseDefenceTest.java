package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhaseDefenceTest {

  private static final PhaseDefence DEFENCE =
      new PhaseDefence(
          3, 60, 36.0F, 10.0F, 1.5F, 40, List.of(Identifier.of("nightborn", "beast_pounce")));

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | true",
        "{\"guard_hits\":3,\"window_ticks\":60,\"break_damage\":36,\"break_away\":10,\"guard_knock\":1.5,\"retarget_chance\":40,\"counters\":[\"nightborn:beast_pounce\"]} | true",
        "{\"guard_hits\":-1} | false",
        "{\"window_ticks\":0} | false",
        "{\"retarget_chance\":101} | false",
        "{\"break_away\":40} | false"
      })
  @DisplayName("a defence reads its guard, its break away and its counters, and defaults to none")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads,
        PhaseDefence.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0} blows already landed in the window: {1}")
  @CsvSource({"0, TAKE", "2, TAKE", "3, GUARD", "9, GUARD"})
  @DisplayName("after three blows in a row the next one is guarded")
  void guards(final int hits, final PhaseDefence.Reaction reaction) {
    assertEquals(reaction, DEFENCE.before(hits));
  }

  @ParameterizedTest(name = "{0} damage taken in the window: {1}")
  @CsvSource({"0, TAKE", "35.9, TAKE", "36, BREAK_AWAY", "200, BREAK_AWAY"})
  @DisplayName("enough damage in a short time makes the boss break away")
  void breaksAway(final float damage, final PhaseDefence.Reaction reaction) {
    assertEquals(reaction, DEFENCE.after(damage));
  }

  @ParameterizedTest(name = "{0} blows and {1} damage against no defence")
  @CsvSource({"0, 0", "50, 5000"})
  @DisplayName("a phase with no defence never guards and never breaks away")
  void none(final int hits, final float damage) {
    assertEquals(PhaseDefence.Reaction.TAKE, PhaseDefence.NONE.before(hits));
    assertEquals(PhaseDefence.Reaction.TAKE, PhaseDefence.NONE.after(damage));
    assertEquals(false, PhaseDefence.NONE.retargets(false, 0));
  }

  @ParameterizedTest(name = "struck by its target {0}, roll {1}: turns on the striker {2}")
  @CsvSource({"false, 0, true", "false, 39, true", "false, 40, false", "true, 0, false"})
  @DisplayName("a blow from someone else may turn the boss on them")
  void retargets(final boolean sameFoe, final int roll, final boolean turns) {
    assertEquals(turns, DEFENCE.retargets(sameFoe, roll));
  }
}
