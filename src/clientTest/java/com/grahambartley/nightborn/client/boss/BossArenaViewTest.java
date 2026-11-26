package com.grahambartley.nightborn.client.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.network.BossArenaPayload;
import java.util.Optional;
import net.minecraft.sound.MusicSound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossArenaViewTest {

  private static final Identifier TRACK = Identifier.of("nightborn", "music.boss.eldest");

  @ParameterizedTest(name = "in the arena {0}, fighting {1}, has a track {2}: plays {3}")
  @CsvSource({
    "true, true, true, true",
    "true, false, true, false",
    "false, true, true, false",
    "true, true, false, false",
    "false, false, false, false"
  })
  @DisplayName("boss music plays only inside the arena while the fight is on")
  void music(
      final boolean inside, final boolean fighting, final boolean track, final boolean plays) {
    final Optional<MusicSound> music =
        BossArenaView.music(
            new BossArenaPayload(
                3,
                inside ? Optional.of(new Arena(BlockPos.ORIGIN, 16)) : Optional.empty(),
                fighting,
                track ? Optional.of(TRACK) : Optional.empty(),
                Optional.empty()));
    assertEquals(plays, music.isPresent());
    music.ifPresent(
        sound -> {
          assertEquals(TRACK, sound.getSound().value().getId());
          assertEquals(0, sound.getMinDelay());
          assertEquals(0, sound.getMaxDelay());
          assertEquals(true, sound.shouldReplaceCurrentMusic());
        });
  }

  @ParameterizedTest(name = "showing boss {0}, word from boss {1}, entering {2}: applies {3}")
  @CsvSource({
    "-1, 5, true, true",
    "-1, 5, false, true",
    "5, 5, false, true",
    "5, 9, false, false",
    "5, 9, true, true",
    "5, 5, true, true"
  })
  @DisplayName("one boss leaving never clears the arena another boss is showing")
  void applies(final int shown, final int sender, final boolean entering, final boolean applies) {
    assertEquals(applies, BossArenaView.applies(shown, sender, entering));
  }

  @ParameterizedTest(name = "in the arena {0}, fighting {1}: holds the track {2}")
  @CsvSource({"true, true, true", "true, false, false", "false, true, false"})
  @DisplayName("the track the view holds is the boss music's own sound, or nothing")
  void track(final boolean inside, final boolean fighting, final boolean held) {
    final Optional<Identifier> track =
        BossArenaView.track(
            BossArenaView.music(
                new BossArenaPayload(
                    3,
                    inside ? Optional.of(new Arena(BlockPos.ORIGIN, 16)) : Optional.empty(),
                    fighting,
                    Optional.of(TRACK),
                    Optional.empty())));
    assertEquals(held ? Optional.of(TRACK) : Optional.empty(), track);
  }
}
