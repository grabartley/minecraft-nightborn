package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.mob.VampirePose;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StakedFallTest {

  @ParameterizedTest(name = "vampire {0}, staked {1}: falls {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("only a vampire killed by a stake gets the staked fall")
  void falls(final boolean vampire, final boolean staked, final boolean falls) {
    assertEquals(falls, StakedFall.falls(vampire, staked));
  }

  @ParameterizedTest(name = "death tick {0}: over {1}")
  @CsvSource({"1, false", "20, false", "59, false", "60, true", "61, true"})
  @DisplayName("a staked vampire stays on the ground past vanilla's one second before it goes")
  void over(final int deathTime, final boolean over) {
    assertEquals(over, StakedFall.over(deathTime));
  }

  @ParameterizedTest(name = "staked {0}, vanilla roll {1}: {2}")
  @CsvSource({"true, 90.0, 0.0", "false, 90.0, 90.0", "false, 45.0, 45.0", "true, 45.0, 0.0"})
  @DisplayName("a staked vampire is not tipped over by the vanilla death roll")
  void deathRoll(final boolean staked, final float vanilla, final float roll) {
    assertEquals(roll, StakedFall.deathRoll(staked, vanilla));
  }

  @Test
  @DisplayName("the body stays until the staked death clip has played through")
  void outlastsTheClip() throws IOException {
    final double seconds =
        JsonParser.parseString(
                Files.readString(
                    Path.of(
                        "src/client/resources/assets/nightborn/animations/entity/vampire.animation.json")))
            .getAsJsonObject()
            .getAsJsonObject("animations")
            .getAsJsonObject(VampirePose.STAKED_DEATH.animation())
            .get("animation_length")
            .getAsDouble();
    assertTrue(StakedFall.FALL_TICKS >= seconds * 20.0, "clip " + seconds + "s");
  }
}
