package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.Arm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class BodyPartTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(BodyPart.class)
  @DisplayName("every part is found by the bone name the rigs use")
  void namedByBone(final BodyPart part) {
    assertEquals(Optional.of(part), BodyPart.named(part.asString()));
  }

  @ParameterizedTest(name = "bone [{0}]")
  @CsvSource({"body", "jaw", "armor_right_arm", "item_left", "''"})
  @DisplayName("bones that are not a part name no part")
  void otherBones(final String bone) {
    assertEquals(Optional.empty(), BodyPart.named(bone));
  }

  @Test
  @DisplayName("the four limbs are the parts that are not the head")
  void limbs() {
    assertEquals(
        List.of(BodyPart.RIGHT_ARM, BodyPart.LEFT_ARM, BodyPart.RIGHT_LEG, BodyPart.LEFT_LEG),
        BodyPart.limbs());
  }

  @ParameterizedTest(name = "{0} arm is {1}")
  @CsvSource({"RIGHT, RIGHT_ARM", "LEFT, LEFT_ARM"})
  @DisplayName("a hand maps to the arm on its side")
  void armOfHand(final Arm arm, final BodyPart part) {
    assertEquals(part, BodyPart.arm(arm));
  }

  @ParameterizedTest(name = "right {0}: {1} {2}")
  @CsvSource({"true, RIGHT_ARM, RIGHT_LEG", "false, LEFT_ARM, LEFT_LEG"})
  @DisplayName("a side picks the arm and leg on that side")
  void bySide(final boolean right, final BodyPart arm, final BodyPart leg) {
    assertEquals(arm, BodyPart.arm(right));
    assertEquals(leg, BodyPart.leg(right));
  }
}
