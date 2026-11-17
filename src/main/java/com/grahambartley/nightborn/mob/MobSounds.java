package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.guild.HunterVoice;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.mob.look.VampireVoice;
import com.grahambartley.nightborn.sound.NightbornSounds;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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

  private static final Map<VampireRig, Map<VampireVoice, SoundEvent>> VOICES =
      voices(VAMPIRE, Arrays.asList(VampireVoice.values()), Set.of(VampireRig.values()));
  private static final Map<VampireRig, Map<HunterVoice, SoundEvent>> HUNTERS = hunterVoices();

  private MobSounds() {}

  static void initialize() {}

  public static SoundEvent vampire(final VampireRig rig, final VampireVoice voice) {
    return VOICES.get(rig).get(voice);
  }

  public static SoundEvent hunter(final VampireRig rig, final HunterVoice voice) {
    final Map<HunterVoice, SoundEvent> voices = HUNTERS.get(rig);
    if (voices == null) {
      throw new IllegalArgumentException("hunters have no " + rig.asString() + " voice");
    }
    return voices.get(voice);
  }

  static String voicePath(final String speaker, final VampireRig rig, final VampireVoice voice) {
    return voicePath(speaker, rig, voice.id());
  }

  static String voicePath(final String speaker, final VampireRig rig, final String voice) {
    return "entity." + speaker + "." + rig.asString() + "." + voice;
  }

  private static Map<VampireRig, Map<HunterVoice, SoundEvent>> hunterVoices() {
    final Map<VampireRig, Map<HunterVoice, SoundEvent>> voices = new EnumMap<>(VampireRig.class);
    for (final VampireRig rig : new TreeSet<>(VampireRig.COMMON)) {
      final Map<HunterVoice, SoundEvent> sounds = new EnumMap<>(HunterVoice.class);
      for (final HunterVoice voice : HunterVoice.values()) {
        sounds.put(voice, NightbornSounds.register(voicePath(HUNTER, rig, voice.id())));
      }
      voices.put(rig, sounds);
    }
    return voices;
  }

  private static Map<VampireRig, Map<VampireVoice, SoundEvent>> voices(
      final String speaker, final List<VampireVoice> spoken, final Set<VampireRig> rigs) {
    final Map<VampireRig, Map<VampireVoice, SoundEvent>> voices = new EnumMap<>(VampireRig.class);
    for (final VampireRig rig : new TreeSet<>(rigs)) {
      final Map<VampireVoice, SoundEvent> sounds = new EnumMap<>(VampireVoice.class);
      spoken.forEach(
          voice -> sounds.put(voice, NightbornSounds.register(voicePath(speaker, rig, voice))));
      voices.put(rig, sounds);
    }
    return voices;
  }
}
