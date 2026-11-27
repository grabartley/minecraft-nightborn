package com.grahambartley.nightborn.gore;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.gear.VampireBane;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class Blows {

  public static final TagKey<Item> SEVERING_WEAPONS =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "severing_weapons"));
  public static final TagKey<DamageType> SEVERING =
      TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, "severing"));
  public static final TagKey<DamageType> NEVER_SEVERS =
      TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, "never_severs"));

  private Blows() {}

  public static Blow read(final DamageSource source) {
    final boolean stake = Staking.staked(source);
    return new Blow(
        source.isOf(NightbornDamage.BITE),
        stake,
        VampireBane.isSilverBlow(source),
        VampireBane.meleeWith(source, SEVERING_WEAPONS) || source.isIn(SEVERING),
        barred(
            stake,
            source.isIn(NEVER_SEVERS),
            source.getAttacker() != null,
            source.isIn(DamageTypeTags.IS_EXPLOSION)));
  }

  static boolean barred(
      final boolean stake, final boolean neverSevers, final boolean struck, final boolean blast) {
    return stake || neverSevers || !(struck || blast);
  }
}
