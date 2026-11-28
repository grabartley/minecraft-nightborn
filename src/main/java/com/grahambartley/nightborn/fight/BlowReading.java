package com.grahambartley.nightborn.fight;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.gear.VampireBane;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class BlowReading {

  public static final TagKey<Item> SEVERING_WEAPONS =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "severing_weapons"));
  public static final TagKey<DamageType> SEVERING =
      TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, "severing"));
  public static final TagKey<DamageType> NEVER_SEVERS =
      TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, "never_severs"));
  public static final TagKey<EntityType<?>> NEVER_SEVERING_SHOTS =
      TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "never_severing_shots"));

  private BlowReading() {}

  public static Blow read(final DamageSource source) {
    return read(source, Cutting.WEAPON);
  }

  public static Blow read(final DamageSource source, final Cutting cutting) {
    final boolean stake = Staking.staked(source);
    final Entity direct = source.getSource();
    final boolean marked = source.isIn(SEVERING);
    return new Blow(
        source.isOf(NightbornDamage.BITE),
        stake,
        VampireBane.isSilverBlow(source),
        cutting.severing(
            marked,
            VampireBane.meleeWith(source, SEVERING_WEAPONS),
            source.isDirect() && source.isIn(NightbornDamage.WEAPON_BLOWS)),
        cutting.bars(marked)
            || barred(
                stake || source.isIn(NEVER_SEVERS),
                direct != null && direct.getType().isIn(NEVER_SEVERING_SHOTS),
                source.isDirect()
                    && direct instanceof PlayerEntity player
                    && player.getMainHandStack().isEmpty(),
                source.getAttacker() != null,
                blast(source)));
  }

  public static boolean blast(final DamageSource source) {
    return source.isIn(DamageTypeTags.IS_EXPLOSION);
  }

  static boolean barred(
      final boolean neverSevers,
      final boolean neverSeveringShot,
      final boolean fist,
      final boolean struck,
      final boolean blast) {
    return neverSevers || neverSeveringShot || fist || !(struck || blast);
  }
}
