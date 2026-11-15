package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.mob.look.VampireVoice;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class MobSounds {

  public static final SoundEvent VAMPIRE_BITE = register("entity.vampire.bite");
  public static final SoundEvent VAMPIRE_BAT_BITE = register("entity.vampire_bat.bite");

  private static final Map<VampireRig, Map<VampireVoice, SoundEvent>> VOICES = voices();

  private MobSounds() {}

  static void initialize() {}

  public static SoundEvent vampire(final VampireRig rig, final VampireVoice voice) {
    return VOICES.get(rig).get(voice);
  }

  static String voicePath(final VampireRig rig, final VampireVoice voice) {
    return "entity.vampire." + rig.asString() + "." + voice.id();
  }

  private static Map<VampireRig, Map<VampireVoice, SoundEvent>> voices() {
    final Map<VampireRig, Map<VampireVoice, SoundEvent>> voices = new EnumMap<>(VampireRig.class);
    for (final VampireRig rig : VampireRig.values()) {
      final Map<VampireVoice, SoundEvent> sounds = new EnumMap<>(VampireVoice.class);
      Arrays.stream(VampireVoice.values())
          .forEach(voice -> sounds.put(voice, register(voicePath(rig, voice))));
      voices.put(rig, sounds);
    }
    return voices;
  }

  private static SoundEvent register(final String path) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, path);
    return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
  }
}
