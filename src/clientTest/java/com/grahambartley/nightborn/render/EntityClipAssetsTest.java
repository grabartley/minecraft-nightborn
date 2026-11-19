package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class EntityClipAssetsTest {

  private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");
  private static final int PITCH = 0;
  private static final int ROLL = 2;
  private static final double TIP_DOWN = -Math.signum(Vec3d.fromPolar(30.0F, 0.0F).y);
  private static final double ARM_FORWARD;
  private static final double RIGHT_ARM_OUTWARD;

  static {
    final ModelPart forward = player();
    CrossbowPosing.meleeAttack(
        forward.getChild(EntityModelPartNames.LEFT_ARM),
        forward.getChild(EntityModelPartNames.RIGHT_ARM),
        true,
        0.0F,
        0.0F);
    ARM_FORWARD = Math.signum(forward.getChild(EntityModelPartNames.RIGHT_ARM).pitch);
    final ModelPart sway = player();
    CrossbowPosing.swingArms(
        sway.getChild(EntityModelPartNames.RIGHT_ARM),
        sway.getChild(EntityModelPartNames.LEFT_ARM),
        0.0F);
    RIGHT_ARM_OUTWARD = Math.signum(sway.getChild(EntityModelPartNames.RIGHT_ARM).roll);
  }

  @ParameterizedTest(name = "{0} {1} {2}")
  @CsvSource({
    "vampire, hiss, right_arm",
    "vampire, hiss, left_arm",
    "vampire, attack, right_arm",
    "vampire, attack, left_arm",
    "vampire, mesmerise, right_arm",
    "vampire, use_item_right, right_arm",
    "vampire, use_item_left, left_arm",
    "vampire, feed, right_arm",
    "vampire, feed, left_arm",
    "vampire, drain, right_arm",
    "vampire, drain, left_arm",
    "vampire, dash, right_arm",
    "vampire, dash, left_arm",
    "vampire, blood_rush, right_arm",
    "vampire, blood_rush, left_arm",
    "vampire, leap, right_arm",
    "vampire, leap, left_arm",
    "hunter, stake_strike, right_arm",
    "hunter, trade, right_arm",
    "hunter, idle_calm, right_arm",
    "hunter, idle_calm, left_arm",
    "hunter, walk_calm, right_arm",
    "hunter, walk_calm, left_arm",
    "hunter, crossbow_shot, right_arm",
    "hunter, crossbow_shot, left_arm",
    "hunter, raise_crucifix, left_arm",
    "hunter, throw_holy_water, left_arm",
    "hunter, silver_chain, right_arm",
    "hunter, bolt_volley, right_arm",
    "hunter, bolt_volley, left_arm",
    "hunter, holy_water_arc, left_arm",
    "hunter, rallying_cry, right_arm",
    "ghostly_familiar, fight, right_arm",
    "ghostly_familiar, fight, left_arm"
  })
  @DisplayName("a reaching clip swings the arm forward the way vanilla raises a zombie's arms")
  void reachesForward(final String rig, final String clip, final String bone) throws IOException {
    assertEquals(ARM_FORWARD, Math.signum(peak(rig, clip, bone, PITCH)));
  }

  @ParameterizedTest(name = "{0} {1} {2}")
  @CsvSource({
    "vampire, levitate_sprint, right_arm",
    "vampire, levitate_sprint, left_arm",
    "vampire, idle_ancient, right_arm",
    "vampire, idle_ancient, left_arm",
    "ghostly_familiar, scout, right_arm",
    "ghostly_familiar, scout, left_arm"
  })
  @DisplayName("a trailing clip sweeps the arms back on purpose")
  void sweepsBack(final String rig, final String clip, final String bone) throws IOException {
    assertEquals(-ARM_FORWARD, Math.signum(peak(rig, clip, bone, PITCH)));
  }

  @ParameterizedTest(name = "{0} {1}")
  @CsvSource({
    "vampire, idle",
    "vampire, walk",
    "vampire, run",
    "vampire, downed",
    "vampire, rise",
    "vampire, weakened",
    "vampire, weakened_walk",
    "vampire, hiss",
    "vampire, idle_look",
    "vampire, idle_listen",
    "vampire, sit",
    "vampire, levitate",
    "vampire, levitate_sprint",
    "vampire, land",
    "vampire, blood_rush",
    "vampire, leap",
    "vampire, dash",
    "vampire, dodge",
    "vampire, idle_ancient",
    "vampire, mourn",
    "hunter, idle",
    "hunter, walk",
    "hunter, run",
    "ghostly_familiar, float",
    "ghostly_familiar, scout",
    "ghostly_familiar, carry"
  })
  @DisplayName("a hanging or spread arm rolls away from the body the way vanilla's arms sway")
  void rollsArmsOutward(final String rig, final String clip) throws IOException {
    assertEquals(RIGHT_ARM_OUTWARD, Math.signum(peak(rig, clip, "right_arm", ROLL)));
    assertEquals(-RIGHT_ARM_OUTWARD, Math.signum(peak(rig, clip, "left_arm", ROLL)));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "hiss",
    "feed",
    "drain",
    "run",
    "dash",
    "blood_rush_tell",
    "blood_rush",
    "leap",
    "idle_ancient",
    "mourn"
  })
  @DisplayName("a lunging, feeding or hunched clip leans the body forward")
  void leansForward(final String clip) throws IOException {
    assertEquals(TIP_DOWN, Math.signum(peak("vampire", clip, "body", PITCH)));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"hiss", "feed", "drain", "blood_rush", "leap", "mesmerise"})
  @DisplayName("a biting or snarling clip opens the jaw to show the fangs")
  void opensTheJaw(final String clip) throws IOException {
    assertEquals(TIP_DOWN, Math.signum(peak("vampire", clip, "jaw", PITCH)));
  }

  static Stream<String> jawClips() throws IOException {
    final JsonObject clips = GeoAssets.animations(GeoAssets.clips("vampire"));
    return clips.keySet().stream()
        .filter(clip -> clips.getAsJsonObject(clip).getAsJsonObject("bones").has("jaw"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("jawClips")
  @DisplayName("the jaw only ever drops open, never folds up into the skull")
  void jawDropsOpen(final String clip) throws IOException {
    final List<Double> angles = angles("vampire", clip, "jaw", PITCH);
    assertFalse(angles.isEmpty());
    for (final double angle : angles) {
      if (angle != 0.0) {
        assertEquals(TIP_DOWN, Math.signum(angle), clip + " jaw at " + angle);
      }
    }
  }

  private static ModelPart player() {
    return TexturedModelData.of(
            PlayerEntityModel.getTexturedModelData(Dilation.NONE, false), 64, 64)
        .createModel();
  }

  private static double peak(final String rig, final String clip, final String bone, final int axis)
      throws IOException {
    return angles(rig, clip, bone, axis).stream()
        .max(Comparator.comparingDouble(Math::abs))
        .orElseThrow(() -> new AssertionError(rig + " " + clip + " moves no " + bone));
  }

  private static List<Double> angles(
      final String rig, final String clip, final String bone, final int axis) throws IOException {
    final JsonObject animated =
        GeoAssets.animations(GeoAssets.clips(rig))
            .getAsJsonObject(clip)
            .getAsJsonObject("bones")
            .getAsJsonObject(bone);
    final List<Double> angles = new ArrayList<>();
    if (animated == null || !animated.has("rotation")) {
      return angles;
    }
    final JsonElement frames = animated.get("rotation");
    if (frames.isJsonArray()) {
      add(frames.getAsJsonArray(), axis, angles);
      return angles;
    }
    for (final Map.Entry<String, JsonElement> frame : frames.getAsJsonObject().entrySet()) {
      final JsonElement value = frame.getValue();
      add(
          value.isJsonArray()
              ? value.getAsJsonArray()
              : value.getAsJsonObject().getAsJsonArray("post"),
          axis,
          angles);
    }
    return angles;
  }

  private static void add(final JsonArray triple, final int axis, final List<Double> angles) {
    final String value = triple.get(axis).getAsString();
    if (NUMBER.matcher(value).matches()) {
      angles.add(Double.parseDouble(value));
    }
  }
}
