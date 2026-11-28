package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.dismember.BodyPart;
import com.grahambartley.nightborn.dismember.BodyState;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class LostLimbsTest {

  private static PlayerEntityModel<LivingEntity> player() {
    final ModelPart root =
        TexturedModelData.of(PlayerEntityModel.getTexturedModelData(Dilation.NONE, false), 64, 64)
            .createModel();
    return new PlayerEntityModel<>(root, false);
  }

  @ParameterizedTest(name = "lost {0}, bone {1}: hidden {2}")
  @CsvSource({
    "LEFT_ARM, left_arm, true",
    "LEFT_ARM, right_arm, false",
    "LEFT_ARM, armor_left_arm, false",
    "RIGHT_LEG, right_leg, true",
    "HEAD, head, true",
    "HEAD, body, false"
  })
  @DisplayName("only the bone named for a lost part is skipped, its children go with it")
  void hidesBone(final BodyPart lost, final String bone, final boolean hidden) {
    assertEquals(hidden, LostLimbs.hidden(BodyState.WHOLE.cut(lost, 0L, 0), bone));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BodyPart.class)
  @DisplayName("a whole body hides no bone")
  void wholeHidesNothing(final BodyPart part) {
    assertEquals(false, LostLimbs.hidden(BodyState.WHOLE, part.asString()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BodyPart.class)
  @DisplayName("a lost part hides its piece of the plain player model and nothing else")
  void hidesPlayerModel(final BodyPart lost) {
    final PlayerEntityModel<LivingEntity> model = player();
    model.setVisible(true);
    LostLimbs.hide(model, BodyState.WHOLE.cut(lost, 0L, 0));
    assertEquals(lost != BodyPart.HEAD, model.head.visible, "head");
    assertEquals(lost != BodyPart.HEAD, model.hat.visible, "hat");
    assertEquals(lost != BodyPart.RIGHT_ARM, model.rightArm.visible, "right arm");
    assertEquals(lost != BodyPart.RIGHT_ARM, model.rightSleeve.visible, "right sleeve");
    assertEquals(lost != BodyPart.LEFT_ARM, model.leftArm.visible, "left arm");
    assertEquals(lost != BodyPart.LEFT_ARM, model.leftSleeve.visible, "left sleeve");
    assertEquals(lost != BodyPart.RIGHT_LEG, model.rightLeg.visible, "right leg");
    assertEquals(lost != BodyPart.RIGHT_LEG, model.rightPants.visible, "right pants");
    assertEquals(lost != BodyPart.LEFT_LEG, model.leftLeg.visible, "left leg");
    assertEquals(lost != BodyPart.LEFT_LEG, model.leftPants.visible, "left pants");
    assertTrue(model.body.visible);
    assertTrue(model.jacket.visible);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "vampire_male",
        "vampire_female",
        "vampire_lord",
        "hunter_male",
        "hunter_female",
        "player_wide",
        "player_slim"
      })
  @DisplayName("every rig that can lose a part names a bone for each part")
  void rigsNameEveryPart(final String rig) throws IOException {
    final Set<String> bones = GeoAssets.boneNames(GeoAssets.ENTITY_GEO.resolve(rig + ".geo.json"));
    final List<String> parts = List.of(BodyPart.values()).stream().map(BodyPart::asString).toList();
    assertTrue(bones.containsAll(parts), rig + " has " + bones);
  }
}
