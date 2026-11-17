package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BossFightsTest {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn/nightborn");
  private static final Path LANG = Path.of("src/main/resources/assets/nightborn/lang/en_us.json");
  private static final Path SKILLS = DATA.resolve("skill");

  static Stream<Path> files(final String folder) throws IOException {
    try (Stream<Path> files = Files.list(DATA.resolve(folder))) {
      return files.sorted().toList().stream();
    }
  }

  static Stream<Path> attacks() throws IOException {
    return files("boss_attack");
  }

  static Stream<Path> fights() throws IOException {
    return files("boss_fight");
  }

  private static <T> DataResult<T> parse(final Codec<T> codec, final Path file) throws IOException {
    return codec.parse(
        JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)));
  }

  private static Identifier id(final Path file) {
    return Identifier.of("nightborn", file.getFileName().toString().replace(".json", ""));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("attacks")
  @DisplayName("every shipped boss attack loads")
  void attackLoads(final Path file) throws IOException {
    final DataResult<BossAttack> parsed = parse(BossAttack.CODEC, file);
    assertTrue(parsed.isSuccess(), () -> parsed.error().orElseThrow().message());
    parsed
        .getOrThrow()
        .power()
        .ifPresent(
            power ->
                assertTrue(
                    Files.exists(SKILLS.resolve(power.getPath() + ".json")),
                    power + " is not a skill"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fights")
  @DisplayName(
      "every shipped boss fight loads, names only attacks and powers that exist, and titles every"
          + " phase")
  void fightLoads(final Path file) throws IOException {
    final DataResult<BossFightPlan> parsed = parse(BossFightPlan.CODEC, file);
    assertTrue(parsed.isSuccess(), () -> parsed.error().orElseThrow().message());
    final BossFightPlan plan = parsed.getOrThrow();
    final Set<Identifier> shipped = attacks().map(BossFightsTest::id).collect(Collectors.toSet());
    assertEquals(
        List.of(),
        BossFights.missing(
            plan, id -> shipped.contains(id) ? parse(id).result() : Optional.empty()));
    final String english = Files.readString(LANG, StandardCharsets.UTF_8);
    for (final BossPhase phase : plan.phases()) {
      assertFalse(phase.attacks().isEmpty(), phase.name() + " has no attacks");
      for (final Identifier counter : phase.defence().counters()) {
        assertTrue(shipped.contains(counter), phase.name() + " counters with a missing " + counter);
      }
      assertTrue(english.contains("\"" + phase.translationKey() + "\""), phase.translationKey());
      for (final Identifier power : phase.powers()) {
        assertTrue(
            Files.exists(SKILLS.resolve(power.getPath() + ".json")), power + " is not a skill");
      }
    }
  }

  private static DataResult<BossAttack> parse(final Identifier id) {
    try {
      return parse(BossAttack.CODEC, DATA.resolve("boss_attack").resolve(id.getPath() + ".json"));
    } catch (final IOException e) {
      return DataResult.error(e::getMessage);
    }
  }

  @Test
  @DisplayName("an attack a fight names but nobody ships is reported once")
  void reportsMissing() throws IOException {
    final BossFightPlan plan =
        BossFightPlan.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"phases\":[{\"name\":\"a\",\"health\":1,\"attacks\":[\"nightborn:here\","
                        + "\"nightborn:gone\"]},{\"name\":\"b\",\"health\":1,\"attacks\":"
                        + "[\"nightborn:gone\",\"nightborn:here\"]}],\"change_clip\":\"hiss\","
                        + "\"change_sound\":\"minecraft:entity.warden.roar\"}"))
            .getOrThrow();
    final Optional<BossAttack> any = parse(attacks().findFirst().orElseThrow().toAbsolutePath());
    assertEquals(
        List.of(Identifier.of("nightborn", "gone")),
        BossFights.missing(plan, id -> id.getPath().equals("here") ? any : Optional.empty()));
  }

  private static Optional<BossAttack> parse(final Path file) throws IOException {
    return parse(BossAttack.CODEC, file).result();
  }
}
