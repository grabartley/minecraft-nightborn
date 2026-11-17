package com.grahambartley.nightborn.eldest;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class EldestTrims {

  public static final RegistryKey<ArmorTrimPattern> FANG = key("fang");
  public static final RegistryKey<ArmorTrimPattern> SIGIL = key("sigil");

  private EldestTrims() {}

  private static RegistryKey<ArmorTrimPattern> key(final String path) {
    return RegistryKey.of(RegistryKeys.TRIM_PATTERN, Identifier.of(Nightborn.MOD_ID, path));
  }
}
