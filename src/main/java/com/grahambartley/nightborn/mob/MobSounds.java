package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class MobSounds {

  public static final SoundEvent VAMPIRE_HURT = register("entity.vampire.hurt");
  public static final SoundEvent VAMPIRE_DEATH = register("entity.vampire.death");
  public static final SoundEvent VAMPIRE_BITE = register("entity.vampire.bite");

  private MobSounds() {}

  static void initialize() {}

  private static SoundEvent register(final String path) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, path);
    return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
  }
}
