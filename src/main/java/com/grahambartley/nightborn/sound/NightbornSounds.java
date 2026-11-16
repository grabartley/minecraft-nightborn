package com.grahambartley.nightborn.sound;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
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

  private NightbornSounds() {}

  public static void initialize() {}

  public static float variedPitch(final Random random) {
    return 0.9F + random.nextFloat() * 0.2F;
  }

  public static List<SoundEvent> all() {
    return List.of(
        DRINK, DRAIN, SIZZLE, STAKE_HIT, SILVER_HIT, HOLY_WATER_HISS, COFFIN_OPEN, COFFIN_CLOSE);
  }

  public static SoundEvent sip(final boolean drains) {
    return drains ? DRAIN : DRINK;
  }

  public static SoundEvent lid(final boolean open) {
    return open ? COFFIN_OPEN : COFFIN_CLOSE;
  }

  public static SoundEvent register(final String path) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, path);
    return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
  }
}
