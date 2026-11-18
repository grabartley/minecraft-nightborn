package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AttackRunTest {

  private static final Identifier SOUND = Identifier.ofVanilla("entity.bat.takeoff");

  private static AttackRun run(final int tellTicks, final int flurryHits) {
    final Optional<FlurryAttack> flurry =
        flurryHits > 0
            ? Optional.of(new FlurryAttack(flurryHits, 5, 4.0F, 3.0F, 0.0F))
            : Optional.empty();
    final Optional<Identifier> power =
        flurryHits > 0 ? Optional.empty() : Optional.of(Identifier.of("nightborn", "lord/rally"));
    return new AttackRun(
        Identifier.of("nightborn", "test"),
        new BossAttack(
            0.0F,
            8.0F,
            1,
            0,
            tellTicks,
            "hiss",
            SOUND,
            "attack",
            SOUND,
            power,
            Optional.empty(),
            flurry,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()),
        100L);
  }

  @ParameterizedTest(name = "tell {0}, {1} flurry hits, at time {2}: {3}, strike tick {4}")
  @CsvSource({
    "10, 0, 100, TELL, -10",
    "10, 0, 109, TELL, -1",
    "10, 0, 110, STRIKE, 0",
    "10, 0, 111, DONE, 1",
    "10, 3, 110, STRIKE, 0",
    "10, 3, 125, STRIKE, 15",
    "10, 3, 126, DONE, 16",
    "10, 3, 90, TELL, -10"
  })
  @DisplayName("an attack tells, strikes for as long as its kind lasts, then is done")
  void stages(
      final int tellTicks,
      final int hits,
      final long now,
      final AttackRun.Stage stage,
      final int strikeTick) {
    final AttackRun run = run(tellTicks, hits);
    assertEquals(stage, run.stage(now));
    assertEquals(strikeTick, run.strikeTick(now));
  }

  @ParameterizedTest(name = "a flurry at time {0}: moves {1}, drains {2}")
  @CsvSource({"100, false, false", "112, false, false"})
  @DisplayName("an attack with no zone and no drain neither moves the boss nor drains")
  void plain(final long now, final boolean moves, final boolean drains) {
    final AttackRun run = run(10, 3);
    assertEquals(moves, run.moves());
    assertEquals(drains, run.drains(now));
  }

  @ParameterizedTest(name = "{0} strike ticks: held {1}")
  @CsvSource({"0, false", "1, true", "40, true"})
  @DisplayName("only a strike that lasts keeps its clip going until it is told to stop")
  void holdsStrike(final int strikeTicks, final boolean held) {
    assertEquals(held, AttackRun.holdsStrike(strikeTicks));
  }

  @ParameterizedTest(name = "at time {0}: bar {1}, held {2}")
  @CsvSource({
    "100, false, false",
    "119, false, false",
    "120, true, true",
    "280, true, true",
    "281, false, false"
  })
  @DisplayName("a channel shows its bar and holds its item only while it strikes")
  void channels(final long now, final boolean bar, final boolean held) {
    final BossAttack prayer =
        BossAttack.CODEC
            .parse(
                com.mojang.serialization.JsonOps.INSTANCE,
                com.google.gson.JsonParser.parseString(
                    "{\"tell_ticks\":20,\"tell_clip\":\"t\",\"tell_sound\":\"a:b\","
                        + "\"clip\":\"c\",\"sound\":\"a:b\",\"channel\":{\"ticks\":160,"
                        + "\"held\":\"nightborn:gold_crucifix\",\"bar\":{\"color\":\"green\","
                        + "\"key\":\"nightborn.boss.praying\"}}}"))
            .getOrThrow();
    final AttackRun run = new AttackRun(Identifier.of("nightborn", "prayer"), prayer, 100L);
    assertEquals(bar, run.channelBar(now).isPresent());
    assertEquals(held, run.held(now).isPresent());
  }
}
