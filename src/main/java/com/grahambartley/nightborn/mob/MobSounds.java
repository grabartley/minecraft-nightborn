package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.mob.look.VampireVoice;
import com.grahambartley.nightborn.sound.NightbornSounds;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.sound.SoundEvent;

public final class MobSounds {

  static final String VAMPIRE = "vampire";
  static final String HUNTER = "hunter";

  public static final SoundEvent VAMPIRE_BITE = NightbornSounds.register("entity.vampire.bite");
  public static final SoundEvent VAMPIRE_BAT_BITE =
      NightbornSounds.register("entity.vampire_bat.bite");
  public static final SoundEvent VAMPIRE_BAT_AMBIENT =
      NightbornSounds.register("entity.vampire_bat.ambient");
  public static final SoundEvent VAMPIRE_BAT_HURT =
      NightbornSounds.register("entity.vampire_bat.hurt");
  public static final SoundEvent VAMPIRE_BAT_DEATH =
      NightbornSounds.register("entity.vampire_bat.death");

  static final List<VampireVoice> HUNTER_VOICES =
      List.of(VampireVoice.AMBIENT, VampireVoice.HURT, VampireVoice.DEATH);

  private static final Map<VampireRig, Map<VampireVoice, SoundEvent>> VOICES =
      voices(VAMPIRE, Arrays.asList(VampireVoice.values()));
  private static final Map<VampireRig, Map<VampireVoice, SoundEvent>> HUNTERS =
      voices(HUNTER, HUNTER_VOICES);

  private MobSounds() {}

  static void initialize() {}

  public static SoundEvent vampire(final VampireRig rig, final VampireVoice voice) {
    return VOICES.get(rig).get(voice);
  }

  public static SoundEvent hunter(final VampireRig rig, final VampireVoice voice) {
    final SoundEvent sound = HUNTERS.get(rig).get(voice);
    if (sound == null) {
      throw new IllegalArgumentException("hunters have no " + voice.id() + " voice");
    }
    return sound;
  }

  static String voicePath(final VampireRig rig, final VampireVoice voice) {
    return voicePath(VAMPIRE, rig, voice);
  }

  static String voicePath(final String speaker, final VampireRig rig, final VampireVoice voice) {
    return "entity." + speaker + "." + rig.asString() + "." + voice.id();
  }

  private static Map<VampireRig, Map<VampireVoice, SoundEvent>> voices(
      final String speaker, final List<VampireVoice> spoken) {
    final Map<VampireRig, Map<VampireVoice, SoundEvent>> voices = new EnumMap<>(VampireRig.class);
    for (final VampireRig rig : VampireRig.values()) {
      final Map<VampireVoice, SoundEvent> sounds = new EnumMap<>(VampireVoice.class);
      spoken.forEach(
          voice -> sounds.put(voice, NightbornSounds.register(voicePath(speaker, rig, voice))));
      voices.put(rig, sounds);
    }
    return voices;
  }
}
