package com.grahambartley.nightborn.vampire;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public final class NightbornDamage {

  public static final RegistryKey<DamageType> BITE = key("bite");
  public static final RegistryKey<DamageType> SUN = key("sun");

  public static final TagKey<DamageType> KILLS_VAMPIRES =
      TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, "kills_vampires"));

  private NightbornDamage() {}

  public static DamageSource bite(final World world, final Entity vampire) {
    return new DamageSource(
        world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(BITE), vampire);
  }

  public static DamageSource sun(final World world) {
    return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(SUN));
  }

  public static boolean killsVampires(final DamageSource source) {
    return source.isIn(KILLS_VAMPIRES);
  }

  private static RegistryKey<DamageType> key(final String path) {
    return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, path));
  }
}
