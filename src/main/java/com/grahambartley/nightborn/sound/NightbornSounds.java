package com.grahambartley.nightborn.sound;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public final class NightbornSounds {

  public static final SoundEvent DRINK = register("entity.vampire.drink");
  public static final SoundEvent DRAIN = register("entity.vampire.drain");
  public static final SoundEvent SIZZLE = register("entity.vampire.sizzle");
  public static final SoundEvent STAKE_HIT = register("item.stake.hit");
  public static final SoundEvent SILVER_HIT = register("item.silver.hit");
  public static final SoundEvent HOLY_WATER_HISS = register("item.holy_water.hiss");
  public static final SoundEvent COFFIN_OPEN = register("block.coffin.open");
  public static final SoundEvent COFFIN_CLOSE = register("block.coffin.close");
  public static final SoundEvent STONE_COFFIN_OPEN = register("block.stone_coffin.open");
  public static final SoundEvent STONE_COFFIN_CLOSE = register("block.stone_coffin.close");
  public static final SoundEvent MUSIC_GOTHIC = register("music.gothic");
  public static final SoundEvent MUSIC_BOSS_ELDEST = register("music.boss.eldest");
  public static final SoundEvent MUSIC_BOSS_ELDEST_FINAL = register("music.boss.eldest_final");
  public static final SoundEvent BOSS_ELDEST_ORGAN = register("boss.eldest.organ");

  private NightbornSounds() {}

  public static void initialize() {}

  public static float variedPitch(final Random random) {
    return 0.9F + random.nextFloat() * 0.2F;
  }

  public static SoundEvent sip(final boolean drains) {
    return drains ? DRAIN : DRINK;
  }

  public static SoundEvent register(final String path) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, path);
    return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
  }
}
