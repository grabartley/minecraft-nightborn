package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class HunterLookingTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(HunterRig.class)
  @DisplayName(
      "a hunter is drawn with its rig's model and the shared hunter clips unless it says otherwise")
  void defaults(final HunterRig rig) {
    final HunterLooking looking =
        new HunterLooking() {
          @Override
          public HunterRig rig() {
            return rig;
          }

          @Override
          public Identifier texture() {
            return Identifier.of("nightborn", "textures/entity/hunter/male_gideon.png");
          }

          @Override
          public float bust() {
            return 0.0F;
          }
        };
    assertEquals(rig.geo(), looking.geo());
    assertEquals(HunterRig.ANIMATION, looking.animation());
  }
}
