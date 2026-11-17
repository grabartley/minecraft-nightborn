package com.grahambartley.nightborn.age;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodCapacity;
import com.grahambartley.nightborn.config.AgeSettings;
import com.grahambartley.nightborn.core.CoreBoosts;
import com.grahambartley.nightborn.vampire.Modifiers;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class AgeBody {

  static final Identifier HEALTH = Identifier.of(Nightborn.MOD_ID, "age_health");
  static final Identifier DAMAGE = Identifier.of(Nightborn.MOD_ID, "age_damage");

  private AgeBody() {}

  public static void apply(final PlayerEntity vampire, final AgeSettings settings) {
    final AgeGifts gifts = AgeGifts.of(AgeAttachment.get(vampire).rank(), settings);
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_MAX_HEALTH,
        HEALTH,
        gifts.healthBeforeMultiplier(CoreBoosts.DOUBLE_HEALTH),
        Operation.ADD_VALUE);
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_ATTACK_DAMAGE,
        DAMAGE,
        gifts.damage(),
        Operation.ADD_VALUE);
    Modifiers.clampHealth(vampire);
    BloodCapacity.setExtra(vampire, gifts.bloodBar());
  }

  public static void remove(final PlayerEntity player) {
    Modifiers.remove(player, EntityAttributes.GENERIC_MAX_HEALTH, HEALTH);
    Modifiers.remove(player, EntityAttributes.GENERIC_ATTACK_DAMAGE, DAMAGE);
    Modifiers.clampHealth(player);
    BloodCapacity.setExtra(player, 0);
  }
}
