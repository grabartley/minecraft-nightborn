package com.grahambartley.nightborn.mob.stage;

import static com.grahambartley.nightborn.mob.stage.StageFixtures.NOBLE;
import static com.grahambartley.nightborn.mob.stage.StageFixtures.NOBLE_BLOOD;
import static com.grahambartley.nightborn.mob.stage.StageFixtures.RUSH;
import static com.grahambartley.nightborn.mob.stage.StageFixtures.THICK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class VampireStagingTest {

  static Stream<Arguments> learned() {
    return Stream.of(
        Arguments.of(List.of(), 0, List.of()),
        Arguments.of(List.of(THICK), 1, List.of()),
        Arguments.of(List.of(THICK, RUSH), 3, List.of()),
        Arguments.of(List.of(THICK, NOBLE_BLOOD, RUSH), 4, List.of(NOBLE)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("learned")
  @DisplayName("a vampire mob holds its picks as bought skills, with every point spent")
  void learned(final List<Identifier> picks, final int spent, final List<Identifier> bloodlines) {
    final TreeProgress progress =
        VampireStaging.learned(picks, StageFixtures.SKILLS, StageFixtures.BRANCHES);
    assertEquals(picks, progress.ownedInOrder());
    assertEquals(spent, progress.spent());
    assertEquals(0, progress.free());
    assertEquals(bloodlines, progress.bloodlines());
  }

  @ParameterizedTest(name = "\"{0}\" reads as {1}")
  @CsvSource({
    "'', nightborn:none",
    "'  ', nightborn:none",
    "'Bad Id', nightborn:none",
    "nightborn:elder, nightborn:elder",
    "nightborn:ancient, nightborn:ancient"
  })
  @DisplayName("a saved stage reads back, and a blank or broken one reads as no stage")
  void parse(final String saved, final String id) {
    assertEquals(Identifier.of(id), VampireStaging.parse(saved));
  }
}
