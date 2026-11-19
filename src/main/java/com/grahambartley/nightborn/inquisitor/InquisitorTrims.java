package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class InquisitorTrims {

  public static final RegistryKey<ArmorTrimPattern> CROSS = key("cross");
  public static final RegistryKey<ArmorTrimPattern> CHAIN = key("chain");

  private InquisitorTrims() {}

  private static RegistryKey<ArmorTrimPattern> key(final String path) {
    return RegistryKey.of(RegistryKeys.TRIM_PATTERN, Identifier.of(Nightborn.MOD_ID, path));
  }
}
