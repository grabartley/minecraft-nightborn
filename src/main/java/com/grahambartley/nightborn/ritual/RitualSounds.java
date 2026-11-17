package com.grahambartley.nightborn.ritual;

import com.grahambartley.nightborn.sound.NightbornSounds;
import net.minecraft.sound.SoundEvent;

public final class RitualSounds {

  public static final SoundEvent RELIC_PLACE =
      NightbornSounds.register("block.ritual_altar.relic_place");
  public static final SoundEvent RELIC_TAKE =
      NightbornSounds.register("block.ritual_altar.relic_take");
  public static final SoundEvent BUILD_UP = NightbornSounds.register("block.ritual_altar.build_up");
  public static final SoundEvent SUMMON = NightbornSounds.register("block.ritual_altar.summon");

  private RitualSounds() {}

  public static void initialize() {}
}
