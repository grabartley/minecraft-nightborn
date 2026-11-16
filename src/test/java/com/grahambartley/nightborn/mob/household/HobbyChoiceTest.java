package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HobbyChoiceTest {

  private static final Temperament READER =
      new Temperament(
          1,
          Map.of(Hobby.READ, 3, Hobby.SIT, 1, Hobby.CONVERSE, 0),
          0,
          ConstantIntProvider.create(30),
          0);

  static Stream<Arguments> choices() {
    final Set<Hobby> all = EnumSet.allOf(Hobby.class);
    return Stream.of(
        Arguments.of(NightPhase.DAY, all, 0.0, null),
        Arguments.of(NightPhase.GATHER, all, 0.9, Hobby.CONVERSE),
        Arguments.of(NightPhase.GATHER, EnumSet.of(Hobby.SIT, Hobby.STROLL), 0.0, Hobby.SIT),
        Arguments.of(NightPhase.PASTIME, all, 0.0, Hobby.SIT),
        Arguments.of(NightPhase.PASTIME, all, 0.3, Hobby.READ),
        Arguments.of(NightPhase.PASTIME, all, 0.99, Hobby.READ),
        Arguments.of(NightPhase.PASTIME, EnumSet.of(Hobby.SIT, Hobby.STROLL), 0.99, Hobby.SIT),
        Arguments.of(NightPhase.PASTIME, EnumSet.of(Hobby.CONVERSE, Hobby.STROLL), 0.5, null),
        Arguments.of(NightPhase.WIND_DOWN, all, 0.3, Hobby.READ));
  }

  @ParameterizedTest(name = "{0}, can do {1}, roll {2}: {3}")
  @MethodSource("choices")
  @DisplayName(
      "a vampire gathers to talk at dusk, then follows its temperament among what its home offers")
  void picks(
      final NightPhase phase, final Set<Hobby> available, final double roll, final Hobby hobby) {
    assertEquals(Optional.ofNullable(hobby), HobbyChoice.pick(READER, phase, available, roll));
  }
}
