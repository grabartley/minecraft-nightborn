package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.brain.ChoiceFixtures;
import com.grahambartley.nightborn.mob.brain.Disposition;
import com.grahambartley.nightborn.mob.brain.IntentBudget;
import com.grahambartley.nightborn.mob.brain.Mind;
import com.grahambartley.nightborn.mob.brain.Sharpness;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TemperamentTest {

  private static final Path DATA =
      Path.of("src/main/resources/data/nightborn/nightborn/vampire_temperament");

  @ParameterizedTest(name = "{0}: weight {1}, temper {2}, mourns {3}s, favours {4}")
  @CsvSource({
    "brooding, 3, 15, 120, READ",
    "convivial, 3, 10, 60, CONVERSE",
    "quarrelsome, 2, 70, 30, CONVERSE",
    "melancholic, 2, 20, 300, SIT",
    "restless, 2, 35, 45, STROLL"
  })
  @DisplayName("every shipped temperament loads and favours its own hobby")
  void shippedTemperamentsLoad(
      final String id,
      final int weight,
      final int temper,
      final int mourning,
      final Hobby favourite)
      throws IOException {
    final Temperament temperament = shipped(id);
    assertEquals(weight, temperament.weight());
    assertEquals(temper, temperament.temper());
    assertEquals(mourning, temperament.mourning());
    final int top =
        temperament.hobbies().values().stream().mapToInt(Integer::intValue).max().orElseThrow();
    assertEquals(top, temperament.hobbies().get(favourite));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"weight\":1,\"hobbies\":{},\"temper\":0,\"pastime_seconds\":30,\"mourning_seconds\":0}",
        "{\"weight\":1,\"hobbies\":{\"sit\":0},\"temper\":0,\"pastime_seconds\":30,\"mourning_seconds\":0}",
        "{\"weight\":0,\"hobbies\":{\"sit\":1},\"temper\":0,\"pastime_seconds\":30,\"mourning_seconds\":0}",
        "{\"weight\":1,\"hobbies\":{\"sit\":1},\"temper\":101,\"pastime_seconds\":30,\"mourning_seconds\":0}",
        "{\"weight\":1,\"hobbies\":{\"sit\":1},\"temper\":0,\"pastime_seconds\":0,\"mourning_seconds\":0}",
        "{\"weight\":1,\"hobbies\":{\"sit\":1},\"temper\":0,\"pastime_seconds\":30,\"mourning_seconds\":0,\"idle_voice_seconds\":0}",
        "{\"weight\":1,\"hobbies\":{\"sit\":1},\"temper\":0,\"pastime_seconds\":30,\"mourning_seconds\":0,\"idle_voice_seconds\":601}",
        "{\"weight\":1,\"hobbies\":{\"sit\":1},\"temper\":0,\"pastime_seconds\":30,\"mourning_seconds\":0,\"disposition\":{\"aims\":{\"sky\":2}}}"
      })
  @DisplayName(
      "a temperament with no weighted hobby, values out of range or a bad disposition is refused")
  void refusesBadData(final String json) {
    assertTrue(Temperament.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "{0}: speaks to itself every {1} to {2} seconds")
  @CsvSource({
    "convivial, 15, 35",
    "quarrelsome, 15, 35",
    "restless, 20, 40",
    "melancholic, 30, 75",
    "brooding, 40, 90"
  })
  @DisplayName(
      "a vampire's idle voice waits as long as its temperament says, never the few seconds a"
          + " plain mob waits")
  void shippedIdleVoiceWaits(final String id, final int fewest, final int most) throws IOException {
    final Temperament temperament = shipped(id);
    final Random random = Random.create(7L);
    for (int i = 0; i < 200; i++) {
      final int ticks = temperament.idleVoiceTicks(random);
      assertTrue(ticks >= fewest * 20 && ticks <= most * 20, id + " waited " + ticks + " ticks");
    }
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"weight\":1,\"hobbies\":{\"sit\":1},\"temper\":0,\"pastime_seconds\":30,\"mourning_seconds\":0}",
        "{\"weight\":2,\"hobbies\":{\"read\":1},\"temper\":50,\"pastime_seconds\":60,\"mourning_seconds\":10}"
      })
  @DisplayName(
      "a temperament that leaves out its idle voice waits as an even-tempered vampire does")
  void idleVoiceDefaultsToEven(final String json) {
    final Temperament temperament =
        Temperament.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(Temperament.EVEN.idleVoiceSeconds(), temperament.idleVoiceSeconds());
  }

  @ParameterizedTest(name = "{0} seconds is {1} ticks")
  @CsvSource({"1, 20", "30, 600", "600, 12000"})
  @DisplayName("the idle voice wait is counted in ticks, twenty to a second")
  void idleVoiceTicksAreSeconds(final int seconds, final int ticks) {
    final Temperament temperament =
        new Temperament(
            1,
            Temperament.EVEN.hobbies(),
            0,
            Temperament.EVEN.pastimeSeconds(),
            0,
            Disposition.EVEN,
            ConstantIntProvider.create(seconds));
    assertEquals(ticks, temperament.idleVoiceTicks(Random.create()));
  }

  private static Temperament shipped(final String id) throws IOException {
    return Temperament.CODEC
        .parse(
            JsonOps.INSTANCE, JsonParser.parseString(Files.readString(DATA.resolve(id + ".json"))))
        .getOrThrow();
  }

  private static Disposition shippedDisposition(final String id) throws IOException {
    return shipped(id).disposition();
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"quarrelsome, strike", "brooding, guard", "melancholic, guard"})
  @DisplayName(
      "two vampires of one stage open a fight differently: a quarrelsome one spends its blood on"
          + " a strike, a brooding one keeps it back")
  void shippedTemperamentsOpenDifferently(final String id, final String picked) throws IOException {
    final Mind mind = new Mind(20, Sharpness.KEEN, shippedDisposition(id));
    assertEquals(
        ChoiceFixtures.id(picked),
        ChoiceFixtures.pick(
            ChoiceFixtures.strikeOrGuard(6),
            mind,
            new IntentBudget(0, 0, 0.0F, 0.5F, 0.0F),
            () -> 0.5));
  }
}
