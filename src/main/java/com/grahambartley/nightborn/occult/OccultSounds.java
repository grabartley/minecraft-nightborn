package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.sound.NightbornSounds;
import net.minecraft.sound.SoundEvent;

public final class OccultSounds {

  public static final SoundEvent FAMILIAR_AMBIENT =
      NightbornSounds.register("entity.ghostly_familiar.ambient");
  public static final SoundEvent FAMILIAR_HURT =
      NightbornSounds.register("entity.ghostly_familiar.hurt");
  public static final SoundEvent FAMILIAR_DEATH =
      NightbornSounds.register("entity.ghostly_familiar.death");
  public static final SoundEvent FAMILIAR_FADE =
      NightbornSounds.register("entity.ghostly_familiar.fade");

  private OccultSounds() {}

  static void register() {}
}
