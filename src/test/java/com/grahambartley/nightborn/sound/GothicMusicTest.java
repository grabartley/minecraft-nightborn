package com.grahambartley.nightborn.sound;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Optional;
import net.minecraft.registry.Registries;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.MusicType;
import net.minecraft.sound.SoundEvents;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GothicMusicTest {

  private static final MusicSound HOUSE =
      new MusicSound(
          Registries.SOUND_EVENT.getEntry(NightbornSounds.MUSIC_GOTHIC), 600, 2400, true);

  private static MusicSound vanilla(final String name) {
    return switch (name) {
      case "MENU" -> MusicType.MENU;
      case "CREDITS" -> MusicType.CREDITS;
      case "UNDERWATER" -> MusicType.UNDERWATER;
      case "DRAGON" -> MusicType.DRAGON;
      case "END" -> MusicType.END;
      case "CREATIVE" -> MusicType.CREATIVE;
      case "GAME" -> MusicType.GAME;
      case "CHERRY_GROVE" -> MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_CHERRY_GROVE);
      default -> throw new IllegalArgumentException(name);
    };
  }

  private static MusicSound expected(final String name, final MusicSound vanilla) {
    return switch (name) {
      case "VANILLA" -> vanilla;
      case "HOUSE" -> HOUSE;
      case "NIGHT" -> GothicMusic.NIGHT;
      default -> throw new IllegalArgumentException(name);
    };
  }

  @ParameterizedTest(name = "{0}, in a structure {1}, at night {2}: {3}")
  @CsvSource({
    "GAME, false, false, VANILLA",
    "GAME, false, true, NIGHT",
    "GAME, true, false, HOUSE",
    "GAME, true, true, HOUSE",
    "CHERRY_GROVE, false, true, NIGHT",
    "CHERRY_GROVE, false, false, VANILLA",
    "CREATIVE, true, false, HOUSE",
    "CREATIVE, false, true, NIGHT",
    "MENU, true, true, VANILLA",
    "CREDITS, true, true, VANILLA",
    "UNDERWATER, true, true, VANILLA",
    "DRAGON, true, true, VANILLA",
    "END, true, true, VANILLA"
  })
  @DisplayName(
      "a structure's music wins, then gothic music at night, but menus, credits, underwater and"
          + " the End keep their own")
  void chooses(
      final String vanillaName, final boolean inStructure, final boolean night, final String want) {
    final MusicSound vanilla = vanilla(vanillaName);
    assertSame(
        expected(want, vanilla),
        GothicMusic.choose(vanilla, inStructure ? Optional.of(HOUSE) : Optional.empty(), night));
  }
}
