package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class TrapDamage {

  public static final RegistryKey<DamageType> SILVER = key("silver");
  public static final RegistryKey<DamageType> HOLY_LIGHT = key("holy_light");

  private TrapDamage() {}

  public static DamageSource of(
      final Entity at, final RegistryKey<DamageType> type, @Nullable final Entity attacker) {
    return at.getWorld().getDamageSources().create(type, at, attacker);
  }

  private static RegistryKey<DamageType> key(final String path) {
    return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, path));
  }
}
