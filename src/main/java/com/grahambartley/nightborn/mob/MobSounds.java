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
import java.util.function.Function;
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
      voices(
          VAMPIRE,
          VampireVoice.class,
          Arrays.asList(VampireVoice.values()),
          VampireVoice::id,
          Set.of(VampireRig.values()));
  private static final Map<VampireRig, Map<HunterVoice, SoundEvent>> HUNTERS =
      voices(
          HUNTER,
          HunterVoice.class,
          Arrays.asList(HunterVoice.values()),
          HunterVoice::id,
          VampireRig.COMMON);

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

  private static <V extends Enum<V>> Map<VampireRig, Map<V, SoundEvent>> voices(
      final String speaker,
      final Class<V> kind,
      final List<V> spoken,
      final Function<V, String> id,
      final Set<VampireRig> rigs) {
    final Map<VampireRig, Map<V, SoundEvent>> voices = new EnumMap<>(VampireRig.class);
    for (final VampireRig rig : new TreeSet<>(rigs)) {
      final Map<V, SoundEvent> sounds = new EnumMap<>(kind);
      spoken.forEach(
          voice ->
              sounds.put(
                  voice, NightbornSounds.register(voicePath(speaker, rig, id.apply(voice)))));
      voices.put(rig, sounds);
    }
    return voices;
  }
}
