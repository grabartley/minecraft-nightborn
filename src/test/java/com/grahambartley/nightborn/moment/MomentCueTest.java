package com.grahambartley.nightborn.moment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MomentCueTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(MomentCue.class)
  @DisplayName("every cue plays a vanilla sound at a sane volume and pitch, with particles to see")
  void playable(final MomentCue cue) {
    assertTrue(
        Registries.SOUND_EVENT.containsId(cue.sound().value().getId()),
        "a registered sound " + cue.sound().value().getId());
    assertTrue(cue.volume() > 0.0F && cue.volume() <= 1.0F, "volume " + cue.volume());
    assertTrue(cue.pitch() >= 0.5F && cue.pitch() <= 2.0F, "pitch " + cue.pitch());
    assertTrue(cue.particle() != null, "particles");
  }
}
