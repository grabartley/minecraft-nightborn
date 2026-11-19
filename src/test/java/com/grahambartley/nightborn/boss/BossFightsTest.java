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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.Registries;
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
                    Files.exists(SKILLS.resolve(power.getPath() + ".json"))
                        && read(SKILLS.resolve(power.getPath() + ".json")).contains("\"power\""),
                    power + " is not a skill with a power to cast"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fights")
  @DisplayName(
      "every shipped boss fight loads, names only attacks that exist, and titles every" + " phase")
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
    }
  }

  private static String read(final Path file) {
    try {
      return Files.readString(file, StandardCharsets.UTF_8);
    } catch (final IOException e) {
      return "";
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

  static final Map<String, String> RIGS =
      Map.of("eldest", "vampire_eldest", "grand_inquisitor", "grand_inquisitor");

  private static Set<String> clipsOf(final Path fight) throws IOException {
    final String fightName = fight.getFileName().toString().replace(".json", "");
    assertTrue(RIGS.containsKey(fightName), fightName + " has no rig in this test");
    return JsonParser.parseString(
            Files.readString(
                Path.of(
                    "src/client/resources/assets/nightborn/animations/entity/"
                        + RIGS.get(fightName)
                        + ".animation.json"),
                StandardCharsets.UTF_8))
        .getAsJsonObject()
        .getAsJsonObject("animations")
        .keySet();
  }

  private static double secondsOf(final Path fight, final String clip) throws IOException {
    final String fightName = fight.getFileName().toString().replace(".json", "");
    return JsonParser.parseString(
            Files.readString(
                Path.of(
                    "src/client/resources/assets/nightborn/animations/entity/"
                        + RIGS.get(fightName)
                        + ".animation.json"),
                StandardCharsets.UTF_8))
        .getAsJsonObject()
        .getAsJsonObject("animations")
        .getAsJsonObject(clip)
        .get("animation_length")
        .getAsDouble();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fights")
  @DisplayName(
      "a fight holds still for its whole rise clip and keeps the body for its whole death clip")
  void clipTicksMatchTheirClips(final Path file) throws IOException {
    final FightClips named = parse(BossFightPlan.CODEC, file).getOrThrow().clips();
    if (!named.rise().isEmpty()) {
      assertEquals(Math.round(secondsOf(file, named.rise()) * 20.0), named.riseTicks());
    }
    if (!named.death().isEmpty()) {
      assertTrue(named.deathTicks() >= Math.ceil(secondsOf(file, named.death()) * 20.0));
    }
  }

  private static List<Path> fightsUsing(final Identifier attack) throws IOException {
    final List<Path> using = new ArrayList<>();
    for (final Path fight : fights().toList()) {
      final BossFightPlan plan = parse(BossFightPlan.CODEC, fight).getOrThrow();
      if (plan.phases().stream()
          .anyMatch(
              phase ->
                  phase.attacks().contains(attack)
                      || phase.defence().counters().contains(attack))) {
        using.add(fight);
      }
    }
    return using;
  }

  private static boolean ships(final Identifier item) {
    return item.getNamespace().equals("minecraft")
        ? Registries.ITEM.containsId(item)
        : Files.exists(
            Path.of(
                "src/main/resources/assets/"
                    + item.getNamespace()
                    + "/models/item/"
                    + item.getPath()
                    + ".json"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("attacks")
  @DisplayName(
      "every attack is used, and its wind-up and strike clips ship in each boss that uses it")
  void attackClipsShip(final Path file) throws IOException {
    final BossAttack attack = parse(BossAttack.CODEC, file).getOrThrow();
    final List<Path> using = fightsUsing(id(file));
    assertFalse(using.isEmpty(), file + " is in no fight");
    for (final Path fight : using) {
      final Set<String> clips = clipsOf(fight);
      assertTrue(clips.contains(attack.tellClip()), attack.tellClip());
      assertTrue(clips.contains(attack.clip()), attack.clip());
    }
    attack
        .channel()
        .flatMap(ChannelAttack::held)
        .ifPresent(held -> assertTrue(ships(held), held.toString()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fights")
  @DisplayName("every clip a fight and its phases name ships in that boss's animations")
  void fightClipsShip(final Path file) throws IOException {
    final BossFightPlan plan = parse(BossFightPlan.CODEC, file).getOrThrow();
    final Set<String> clips = clipsOf(file);
    final FightClips named = plan.clips();
    final Stream<String> phases =
        plan.phases().stream()
            .map(BossPhase::look)
            .flatMap(
                look ->
                    Stream.of(
                        look.idle(),
                        look.walk(),
                        look.form(),
                        look.formEnter(),
                        look.fly(),
                        look.flyForm(),
                        look.enterClip()));
    Stream.concat(
            phases,
            Stream.of(
                plan.changeClip(),
                named.summon(),
                named.guard(),
                named.breakAway(),
                named.stagger(),
                named.takeoff(),
                named.land(),
                named.downedForm(),
                named.fedForm(),
                named.rise(),
                named.death()))
        .filter(clip -> !clip.isEmpty())
        .forEach(clip -> assertTrue(clips.contains(clip), clip));
    plan.phases().stream()
        .flatMap(phase -> phase.look().held().stream())
        .forEach(held -> assertTrue(ships(held), held.toString()));
    plan.phases().stream()
        .flatMap(phase -> phase.look().skin().stream())
        .forEach(
            skin ->
                assertTrue(
                    Files.exists(
                        Path.of(
                            "src/main/resources/assets/"
                                + skin.getNamespace()
                                + "/"
                                + skin.getPath())),
                    skin.toString()));
  }
}
