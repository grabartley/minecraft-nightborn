package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
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
import org.junit.jupiter.params.provider.ValueSource;

class VampireClipAssetsTest {

  private static final Path CLIPS = GeoAssets.ENTITY_CLIPS.resolve("vampire.animation.json");
  private static final double ARM_FORWARD = vanillaArmForward();
  private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");
  private static final double TIP_DOWN = -Math.signum(Vec3d.fromPolar(30.0F, 0.0F).y);

  @ParameterizedTest(name = "{0} {1}")
  @CsvSource({
    "hiss, right_arm",
    "hiss, left_arm",
    "attack, right_arm",
    "attack, left_arm",
    "mesmerise, right_arm",
    "use_item_right, right_arm",
    "use_item_left, left_arm",
    "feed, right_arm",
    "feed, left_arm",
    "drain, right_arm",
    "drain, left_arm",
    "dash, right_arm",
    "dash, left_arm",
    "blood_rush, right_arm",
    "blood_rush, left_arm",
    "leap, right_arm",
    "leap, left_arm"
  })
  @DisplayName("a reaching clip swings the arm forward the way vanilla raises a zombie's arms")
  void reachesForward(final String clip, final String bone) throws IOException {
    assertEquals(ARM_FORWARD, Math.signum(peak(clip, bone)));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
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
    assertEquals(TIP_DOWN, Math.signum(peak(clip, "body")));
  }

  static Stream<String> jawClips() throws IOException {
    final JsonObject clips = GeoAssets.animations(CLIPS);
    return clips.keySet().stream()
        .filter(clip -> clips.getAsJsonObject(clip).getAsJsonObject("bones").has("jaw"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("jawClips")
  @DisplayName("the jaw only ever drops open, never folds up into the skull")
  void jawDropsOpen(final String clip) throws IOException {
    final List<Double> angles = angles(clip, "jaw");
    assertFalse(angles.isEmpty());
    for (final double angle : angles) {
      if (angle != 0.0) {
        assertEquals(TIP_DOWN, Math.signum(angle), clip + " jaw at " + angle);
      }
    }
  }

  private static double vanillaArmForward() {
    final ModelPart model =
        TexturedModelData.of(PlayerEntityModel.getTexturedModelData(Dilation.NONE, false), 64, 64)
            .createModel();
    final ModelPart rightArm = model.getChild(EntityModelPartNames.RIGHT_ARM);
    CrossbowPosing.meleeAttack(
        model.getChild(EntityModelPartNames.LEFT_ARM), rightArm, true, 0.0F, 0.0F);
    return Math.signum(rightArm.pitch);
  }

  private static double peak(final String clip, final String bone) throws IOException {
    return angles(clip, bone).stream()
        .max(Comparator.comparingDouble(Math::abs))
        .orElseThrow(() -> new AssertionError(clip + " moves no " + bone));
  }

  private static List<Double> angles(final String clip, final String bone) throws IOException {
    final JsonObject rotation =
        GeoAssets.animations(CLIPS)
            .getAsJsonObject(clip)
            .getAsJsonObject("bones")
            .getAsJsonObject(bone);
    final List<Double> angles = new ArrayList<>();
    if (rotation == null || !rotation.has("rotation")) {
      return angles;
    }
    final JsonElement frames = rotation.get("rotation");
    if (frames.isJsonArray()) {
      addX(frames.getAsJsonArray(), angles);
      return angles;
    }
    for (final Map.Entry<String, JsonElement> frame : frames.getAsJsonObject().entrySet()) {
      final JsonElement value = frame.getValue();
      addX(
          value.isJsonArray()
              ? value.getAsJsonArray()
              : value.getAsJsonObject().getAsJsonArray("post"),
          angles);
    }
    return angles;
  }

  private static void addX(final JsonArray triple, final List<Double> angles) {
    final String x = triple.get(0).getAsString();
    if (NUMBER.matcher(x).matches()) {
      angles.add(Double.parseDouble(x));
    }
  }
}
