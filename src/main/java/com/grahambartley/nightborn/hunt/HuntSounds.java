package com.grahambartley.nightborn.hunt;

import com.grahambartley.nightborn.sound.NightbornSounds;
import net.minecraft.sound.SoundEvent;

public final class HuntSounds {

  public static final SoundEvent BELL_RING = NightbornSounds.register("item.hunters_bell.ring");
  public static final SoundEvent WARNING_TOLL = NightbornSounds.register("event.hunt.warning_toll");
  public static final SoundEvent BELLS_ANSWER = NightbornSounds.register("event.hunt.bells_answer");

  private HuntSounds() {}

  public static void initialize() {}
}
