package com.grahambartley.nightborn.guild.raiding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.guild.HunterKit;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

  @ParameterizedTest(name = "{0} hunters, veteran {1}: {2} in all")
  @CsvSource({"3, false, 3", "11, true, 12", "12, false, 12", "20, true, 12", "20, false, 12"})
  void neverHoldsMoreThanTwelve(final int hunters, final boolean veteran, final int size) {
    final RaidArms arms =
        new RaidArms(
            id("stage"),
            veteran ? Optional.of(id("veteran")) : Optional.empty(),
            List.of(new RaidArms.Kits(id("a"), hunters)));
    assertEquals(size, arms.size());
    assertEquals(size - (veteran ? 1 : 0), arms.hunterKits().size());
  }

  @ParameterizedTest(name = "fills kits in order: {0}")
  @CsvSource({"2, 1, a;a;b", "1, 3, a;b;b;b"})
  void listsEachKitAsOftenAsItIsCounted(final int a, final int b, final String expected) {
    final RaidArms arms =
        new RaidArms(
            id("stage"),
            Optional.empty(),
            List.of(new RaidArms.Kits(id("a"), a), new RaidArms.Kits(id("b"), b)));
    assertEquals(
        List.of(expected.split(";")).stream().map(RaidArmsTest::id).toList(), arms.hunterKits());
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
    assertEquals(size, arms.size());
    assertEquals(veteran, arms.veteran().isPresent());
    int silvered = veteran ? 1 : 0;
    int holy = 0;
    for (final Identifier kitId : arms.hunterKits()) {
      final HunterKit kit = kit(kitId);
      silvered += kit.weapon().getPath().startsWith("silver") ? 1 : 0;
      holy += kit.holyWater() ? 1 : 0;
      assertEquals(0, kit.weight(), kitId + " must never be picked for an ordinary hunter");
    }
    assertEquals(silver, silvered);
    assertEquals(holyWater, holy);
  }
}
