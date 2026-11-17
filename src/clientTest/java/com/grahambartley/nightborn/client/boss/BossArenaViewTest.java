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
                inside ? Optional.of(new Arena(BlockPos.ORIGIN, 16)) : Optional.empty(),
                fighting,
                track ? Optional.of(TRACK) : Optional.empty()));
    assertEquals(plays, music.isPresent());
    music.ifPresent(
        sound -> {
          assertEquals(TRACK, sound.getSound().value().getId());
          assertEquals(0, sound.getMinDelay());
          assertEquals(0, sound.getMaxDelay());
          assertEquals(true, sound.shouldReplaceCurrentMusic());
        });
  }
}
