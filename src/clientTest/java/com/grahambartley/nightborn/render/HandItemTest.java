package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.util.Arm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HandItemTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"item_right, RIGHT", "item_left, LEFT", "right_item, ", "right_arm, "})
  @DisplayName("only the player rig's hand bones carry an item")
  void mapsHandBones(final String bone, final Arm arm) {
    assertEquals(Optional.ofNullable(arm), HandItem.arm(bone));
  }

  @ParameterizedTest(name = "{0} hand, {1} main arm, main hand {2}")
  @CsvSource({"RIGHT, RIGHT, true", "LEFT, RIGHT, false", "LEFT, LEFT, true", "RIGHT, LEFT, false"})
  @DisplayName("the main arm's hand holds the main hand item, the other the off hand")
  void picksTheHandStack(final Arm hand, final Arm mainArm, final boolean main) {
    assertEquals(main ? "main" : "off", HandItem.stack(hand, mainArm, "main", "off"));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"RIGHT, THIRD_PERSON_RIGHT_HAND", "LEFT, THIRD_PERSON_LEFT_HAND"})
  @DisplayName("each hand draws its item with vanilla's third person display for that hand")
  void usesVanillaThirdPersonDisplay(final Arm hand, final ModelTransformationMode mode) {
    assertEquals(mode, HandItem.mode(hand));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"hunter_male", "hunter_female", "guild_master", "player_wide"})
  @DisplayName("every rig that holds items has the right hand bone")
  void rigsHaveTheHandBone(final String rig) throws IOException {
    assertTrue(
        GeoAssets.boneNames(GeoAssets.ENTITY_GEO.resolve(rig + ".geo.json"))
            .contains(HandItem.RIGHT_BONE));
  }
}
