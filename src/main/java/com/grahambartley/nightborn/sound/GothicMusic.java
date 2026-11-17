package com.grahambartley.nightborn.sound;

import java.util.Optional;
import java.util.Set;
import net.minecraft.registry.Registries;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.MusicType;

public final class GothicMusic {

  public static final MusicSound NIGHT =
      MusicType.createIngameMusic(Registries.SOUND_EVENT.getEntry(NightbornSounds.MUSIC_GOTHIC));

  private static final Set<MusicSound> KEPT =
      Set.of(
          MusicType.MENU, MusicType.CREDITS, MusicType.UNDERWATER, MusicType.DRAGON, MusicType.END);

  private GothicMusic() {}

  public static MusicSound choose(
      final MusicSound vanilla,
      final Optional<MusicSound> boss,
      final Optional<MusicSound> structure,
      final boolean night) {
    if (KEPT.contains(vanilla)) {
      return vanilla;
    }
    return boss.or(() -> structure).orElse(night ? NIGHT : vanilla);
  }
}
