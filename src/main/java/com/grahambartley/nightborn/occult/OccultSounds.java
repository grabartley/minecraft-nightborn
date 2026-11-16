package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class OccultSounds {

  public static final SoundEvent FAMILIAR_AMBIENT = register("entity.ghostly_familiar.ambient");
  public static final SoundEvent FAMILIAR_HURT = register("entity.ghostly_familiar.hurt");
  public static final SoundEvent FAMILIAR_DEATH = register("entity.ghostly_familiar.death");
  public static final SoundEvent FAMILIAR_FADE = register("entity.ghostly_familiar.fade");

  private OccultSounds() {}

  static void register() {}

  private static SoundEvent register(final String path) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, path);
    return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
  }
}
