package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StageLookTest {

  @ParameterizedTest(name = "{0} on {1}: {2}")
  @CsvSource({
    "nightborn:textures/entity/vampire/stage/elder, male, nightborn:textures/entity/vampire/stage/elder_male.png",
    "nightborn:textures/entity/vampire/stage/ancient, female, nightborn:textures/entity/vampire/stage/ancient_female.png"
  })
  @DisplayName("a stage's marks use one sheet per rig")
  void marksTexture(final String marks, final String rig, final String texture) {
    assertEquals(
        Optional.of(Identifier.of(texture)),
        new StageLook(Optional.empty(), Optional.of(Identifier.of(marks))).marksTexture(rig));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"male", "female"})
  @DisplayName("a stage with no marks draws nothing over the look")
  void noMarks(final String rig) {
    assertEquals(Optional.empty(), StageLook.PLAIN.marksTexture(rig));
  }
}
