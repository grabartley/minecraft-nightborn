package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Optional;
import net.minecraft.sound.SoundEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BaneHitSoundsTest {

  @ParameterizedTest(name = "vampire={0} stake={1} silver={2} -> {3}")
  @CsvSource({
    "true, true, false, item.stake.hit",
    "false, true, false, item.stake.hit",
    "true, true, true, item.stake.hit",
    "true, false, true, item.silver.hit",
    "false, false, true, ''",
    "true, false, false, ''",
    "false, false, false, ''"
  })
  @DisplayName("a stake always lands with its own sound, silver only rings on a vampire")
  void soundForBlow(
      final boolean vampire, final boolean stake, final boolean silver, final String expected) {
    final Optional<SoundEvent> sound = BaneHitSounds.of(vampire, stake, silver);
    assertEquals(expected, sound.map(s -> s.getId().getPath()).orElse(""));
  }
}
