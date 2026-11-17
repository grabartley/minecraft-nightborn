package com.grahambartley.nightborn.gramophone;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.sound.NightbornSounds;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class MusicDiscs {

  public static final List<String> TRACKS =
      List.of("prelude_in_a_minor", "variation_25", "la_malinconia", "adagio_in_f_minor");

  private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

  static {
    TRACKS.forEach(MusicDiscs::register);
  }

  private MusicDiscs() {}

  public static void initialize() {}

  public static List<Item> all() {
    return List.copyOf(ITEMS.values());
  }

  public static String itemId(final String track) {
    return "music_disc_" + track;
  }

  public static String soundId(final String track) {
    return "music_disc." + track;
  }

  public static RegistryKey<JukeboxSong> song(final String track) {
    return RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(Nightborn.MOD_ID, track));
  }

  private static void register(final String track) {
    NightbornSounds.register(soundId(track));
    ITEMS.put(
        track,
        Registry.register(
            Registries.ITEM,
            Identifier.of(Nightborn.MOD_ID, itemId(track)),
            new Item(
                new Item.Settings().maxCount(1).rarity(Rarity.RARE).jukeboxPlayable(song(track)))));
  }
}
