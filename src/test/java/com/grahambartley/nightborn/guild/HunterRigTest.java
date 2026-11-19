package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.mob.look.VampireRig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class HunterRigTest {

  private static final Path CLIENT = Path.of("src/client/resources/assets");

  private static Path file(final net.minecraft.util.Identifier id) {
    return CLIENT.resolve(id.getNamespace()).resolve(id.getPath());
  }

  private static Set<String> bones(final HunterRig rig) throws IOException {
    final Set<String> names = new HashSet<>();
    JsonParser.parseString(Files.readString(file(rig.geo())))
        .getAsJsonObject()
        .getAsJsonArray("minecraft:geometry")
        .get(0)
        .getAsJsonObject()
        .getAsJsonArray("bones")
        .forEach(bone -> names.add(bone.getAsJsonObject().get("name").getAsString()));
    return names;
  }

  @ParameterizedTest(name = "female {0}: {1}")
  @CsvSource({"true, FEMALE", "false, MALE"})
  void aHunterIsMaleOrFemale(final boolean female, final HunterRig rig) {
    assertEquals(rig, HunterRig.hunter(female));
  }

  @ParameterizedTest(name = "female {0}: {1}")
  @CsvSource({"true, VETERAN_FEMALE", "false, VETERAN_MALE"})
  void aVeteranIsMaleOrFemale(final boolean female, final HunterRig rig) {
    assertEquals(rig, HunterRig.veteran(female));
  }

  @ParameterizedTest(name = "{0} is voiced and built as {1}")
  @CsvSource({
    "MALE, MALE",
    "FEMALE, FEMALE",
    "GUILD_MASTER, MALE",
    "VETERAN_MALE, MALE",
    "VETERAN_FEMALE, FEMALE"
  })
  void takesItsVoiceAndBustFromAFigure(final HunterRig rig, final VampireRig figure) {
    assertEquals(figure, rig.figure());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(HunterRig.class)
  void everyRigHasTheBonesSharedRulesUse(final HunterRig rig) throws IOException {
    assertTrue(
        bones(rig)
            .containsAll(
                Set.of(
                    "head",
                    "jaw",
                    "nose",
                    "item_right",
                    "item_left",
                    "armor_head",
                    "armor_body",
                    "armor_right_arm",
                    "armor_left_arm",
                    "armor_right_leg",
                    "armor_left_leg",
                    "armor_right_boot",
                    "armor_left_boot")));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(HunterRig.class)
  void everyRigHasEveryBoneTheSharedClipsMove(final HunterRig rig) throws IOException {
    final Set<String> bones = bones(rig);
    final JsonObject clips =
        JsonParser.parseString(Files.readString(file(HunterRig.ANIMATION)))
            .getAsJsonObject()
            .getAsJsonObject("animations");
    for (final String clip : clips.keySet()) {
      for (final String bone : clips.getAsJsonObject(clip).getAsJsonObject("bones").keySet()) {
        assertTrue(bones.contains(bone), rig + " has no " + bone + " for " + clip);
      }
    }
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"FEMALE, true", "MALE, false", "GUILD_MASTER, false"})
  void onlyTheWomanHasABust(final HunterRig rig, final boolean bust) throws IOException {
    assertEquals(bust, bones(rig).contains("bust"));
  }
}
