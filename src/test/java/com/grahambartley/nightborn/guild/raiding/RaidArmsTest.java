package com.grahambartley.nightborn.guild.raiding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.guild.HunterKit;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RaidArmsTest {

  static final Path DATA = Path.of("src/main/resources/data/nightborn/nightborn");

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  static RaidArms shipped(final String stage) throws IOException {
    return RaidArms.CODEC
        .parse(
            JsonOps.INSTANCE,
            JsonParser.parseString(
                Files.readString(DATA.resolve("raiding_party").resolve(stage + ".json"))))
        .getOrThrow();
  }

  static HunterKit kit(final Identifier id) throws IOException {
    return HunterKit.CODEC
        .parse(
            JsonOps.INSTANCE,
            JsonParser.parseString(
                Files.readString(DATA.resolve("hunter_kit").resolve(id.getPath() + ".json"))))
        .getOrThrow();
  }

  @ParameterizedTest(name = "{0} hunters at {1}%, veteran {2}: {3} in all")
  @CsvSource({
    "3, 100, false, 3",
    "11, 100, true, 12",
    "8, 200, true, 12",
    "8, 50, true, 5",
    "4, 50, false, 2",
    "4, 0, true, 0",
    "7, 400, false, 12"
  })
  void scalesWithTheSettingButNeverHoldsMoreThanTwelve(
      final int hunters, final int percent, final boolean veteran, final int size) {
    final RaidArms arms =
        new RaidArms(
            id("stage"),
            veteran ? Optional.of(id("veteran")) : Optional.empty(),
            List.of(new RaidArms.Kits(id("a"), hunters)),
            Map.of());
    assertEquals(size, arms.size(percent));
    assertEquals(size - (arms.led(percent) ? 1 : 0), arms.hunterKits(percent).size());
  }

  @ParameterizedTest(name = "{0} hunters, veteran {1}: loads {2}")
  @CsvSource({"12, false, true", "11, true, true", "12, true, false", "13, false, false"})
  void rejectsAPartyOfMoreThanTwelve(
      final int hunters, final boolean veteran, final boolean loads) {
    final String json =
        "{\"stage\":\"nightborn:elder\","
            + (veteran ? "\"veteran\":\"nightborn:veteran\"," : "")
            + "\"kits\":[{\"kit\":\"nightborn:a\",\"count\":"
            + hunters
            + "}]}";
    assertEquals(
        loads,
        RaidArms.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().isPresent());
  }

  @ParameterizedTest(name = "fills kits in order: {0}")
  @CsvSource({"2, 1, a;a;b", "1, 3, a;b;b;b"})
  void listsEachKitAsOftenAsItIsCounted(final int a, final int b, final String expected) {
    final RaidArms arms =
        new RaidArms(
            id("stage"),
            Optional.empty(),
            List.of(new RaidArms.Kits(id("a"), a), new RaidArms.Kits(id("b"), b)),
            Map.of());
    assertEquals(
        List.of(expected.split(";")).stream().map(RaidArmsTest::id).toList(), arms.hunterKits(100));
  }

  @ParameterizedTest(name = "{0}: {1} hunters, {2} with silver, {3} with holy water, veteran {4}")
  @CsvSource({
    "fledgling, 3, 1, 0, false",
    "adult, 5, 3, 1, false",
    "elder, 8, 6, 2, true",
    "ancient, 12, 12, 3, true"
  })
  void shipsTheLockedPartyForEachStage(
      final String stage,
      final int size,
      final int silver,
      final int holyWater,
      final boolean veteran)
      throws IOException {
    final RaidArms arms = shipped(stage);
    assertEquals(id(stage), arms.stage());
    assertEquals(size, arms.size(100));
    assertEquals(veteran, arms.veteran().isPresent());
    int silvered = veteran ? 1 : 0;
    int holy = 0;
    for (final Identifier kitId : arms.hunterKits(100)) {
      final HunterKit kit = kit(kitId);
      silvered += kit.weapon().getPath().startsWith("silver") ? 1 : 0;
      holy += kit.holyWater() ? 1 : 0;
      assertEquals(0, kit.weight(), kitId + " must never be picked for an ordinary hunter");
    }
    assertEquals(silver, silvered);
    assertEquals(holyWater, holy);
  }

  @ParameterizedTest(name = "{0} at {1}%: {2} hunters, {3} with silver, {4} with holy water")
  @CsvSource({
    "elder, 50, 5, 4, 1",
    "elder, 200, 12, 11, 4",
    "adult, 50, 3, 2, 1",
    "ancient, 50, 7, 7, 2"
  })
  void keepsTheSharesCloseWhenTheSettingScalesAParty(
      final String stage, final int percent, final int size, final int silver, final int holyWater)
      throws IOException {
    final RaidArms arms = shipped(stage);
    int silvered = arms.led(percent) ? 1 : 0;
    int holy = 0;
    for (final Identifier kitId : arms.hunterKits(percent)) {
      final HunterKit kit = kit(kitId);
      silvered += kit.weapon().getPath().startsWith("silver") ? 1 : 0;
      holy += kit.holyWater() ? 1 : 0;
    }
    assertEquals(size, arms.size(percent));
    assertEquals(silver, silvered);
    assertEquals(holyWater, holy);
  }

  @ParameterizedTest(name = "{0}: veteran weight {1}, master weight {2}")
  @CsvSource({"fledgling, 0, 0", "adult, 20, 5", "elder, 38, 12", "ancient, 45, 25"})
  void sendsOlderRanksAfterOlderVampires(final String stage, final int veteran, final int master)
      throws IOException {
    final RaidArms arms = shipped(stage);
    assertEquals(veteran, arms.rankWeights().getOrDefault(id("veteran"), 0));
    assertEquals(master, arms.rankWeights().getOrDefault(id("master"), 0));
  }
}
