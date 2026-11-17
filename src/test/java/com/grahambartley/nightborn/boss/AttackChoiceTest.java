package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AttackChoiceTest {

  private static final Identifier SOUND = Identifier.ofVanilla("entity.bat.takeoff");

  private static AttackChoice.Option option(
      final String name, final float min, final float max, final int weight, final long readyAt) {
    return new AttackChoice.Option(
        Identifier.of("nightborn", name),
        new BossAttack(
            min,
            max,
            weight,
            0,
            10,
            "hiss",
            SOUND,
            "attack",
            SOUND,
            Optional.of(Identifier.of("nightborn", "lord/rally")),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()),
        readyAt);
  }

  private static final List<AttackChoice.Option> OPTIONS =
      List.of(
          option("close", 0.0F, 4.0F, 3, 0L),
          option("far", 5.0F, 16.0F, 2, 0L),
          option("any", 0.0F, 16.0F, 1, 0L),
          option("cooling", 0.0F, 16.0F, 5, 500L));

  @ParameterizedTest(name = "{0} blocks away at time {1}, roll {2}: {3}")
  @CsvSource({
    "2.0, 100, 0, close",
    "2.0, 100, 2, close",
    "2.0, 100, 3, any",
    "2.0, 100, 4, close",
    "8.0, 100, 0, far",
    "8.0, 100, 1, far",
    "8.0, 100, 2, any",
    "4.5, 100, 0, any",
    "20.0, 100, 0, none",
    "2.0, 500, 3, any",
    "2.0, 500, 4, cooling",
    "2.0, 500, 8, cooling",
    "2.0, 100, -1, any"
  })
  @DisplayName("an attack is picked by weight from those in range and off cooldown")
  void chooses(final double distance, final long now, final int roll, final String want) {
    final Optional<String> chosen =
        AttackChoice.choose(OPTIONS, distance, now, total -> roll)
            .map(option -> option.id().getPath());
    assertEquals(want.equals("none") ? Optional.empty() : Optional.of(want), chosen);
  }

  @ParameterizedTest(name = "{0} blocks away at time {1}: weights add to {2}")
  @CsvSource({"2.0, 100, 4", "8.0, 100, 3", "2.0, 500, 9", "4.5, 100, 1"})
  @DisplayName("the roll is asked for a number below the weight of the open attacks")
  void rollsWithinTheOpenWeight(final double distance, final long now, final int total) {
    final int[] asked = new int[1];
    AttackChoice.choose(
        OPTIONS,
        distance,
        now,
        bound -> {
          asked[0] = bound;
          return 0;
        });
    assertEquals(total, asked[0]);
  }
}
