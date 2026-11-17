package com.grahambartley.nightborn.core;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.Starvation;
import com.grahambartley.nightborn.config.BloodSettings;
import com.grahambartley.nightborn.config.CoreSettings;
import com.grahambartley.nightborn.skill.SkillBareHands;
import com.grahambartley.nightborn.vampire.Modifiers;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class VampireCore {

  static final Identifier MAX_HEALTH = Identifier.of(Nightborn.MOD_ID, "vampire_health");
  static final Identifier BARE_HANDS = Identifier.of(Nightborn.MOD_ID, "vampire_bare_hands");
  static final Identifier NIGHT_SPEED = Identifier.of(Nightborn.MOD_ID, "vampire_night_speed");
  static final Identifier NIGHT_JUMP = Identifier.of(Nightborn.MOD_ID, "vampire_night_jump");

  private VampireCore() {}

  public static void tick(
      final LivingEntity vampire, final CoreSettings settings, final BloodSettings blood) {
    final boolean night = NightSight.isNight(vampire.getWorld());
    apply(
        vampire,
        CoreBoosts.of(
            settings,
            blood,
            vampire.getMainHandStack().isEmpty(),
            night,
            starving(vampire),
            SkillBareHands.damage(vampire)));
    if (vampire instanceof PlayerEntity
        && NightSight.sees(night, VampireAttributes.has(vampire, VampireAttributes.DARK_SIGHT))) {
      NightSight.keep(vampire);
    }
  }

  public static void refreshBareHands(
      final LivingEntity vampire, final CoreSettings settings, final BloodSettings blood) {
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_ATTACK_DAMAGE,
        BARE_HANDS,
        CoreBoosts.of(
                settings,
                blood,
                vampire.getMainHandStack().isEmpty(),
                false,
                starving(vampire),
                SkillBareHands.damage(vampire))
            .bareHandDamage(),
        Operation.ADD_VALUE);
  }

  public static void remove(final LivingEntity entity) {
    Modifiers.remove(entity, EntityAttributes.GENERIC_MAX_HEALTH, MAX_HEALTH);
    Modifiers.remove(entity, EntityAttributes.GENERIC_ATTACK_DAMAGE, BARE_HANDS);
    Modifiers.remove(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, NIGHT_SPEED);
    Modifiers.remove(entity, EntityAttributes.GENERIC_JUMP_STRENGTH, NIGHT_JUMP);
    Modifiers.clampHealth(entity);
  }

  private static void apply(final LivingEntity vampire, final CoreBoosts boosts) {
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_MAX_HEALTH,
        MAX_HEALTH,
        boosts.maxHealthMultiplier(),
        Operation.ADD_MULTIPLIED_BASE);
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_ATTACK_DAMAGE,
        BARE_HANDS,
        boosts.bareHandDamage(),
        Operation.ADD_VALUE);
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_MOVEMENT_SPEED,
        NIGHT_SPEED,
        boosts.speedMultiplier(),
        Operation.ADD_MULTIPLIED_TOTAL);
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_JUMP_STRENGTH,
        NIGHT_JUMP,
        boosts.jumpMultiplier(),
        Operation.ADD_MULTIPLIED_BASE);
  }

  private static int starving(final LivingEntity vampire) {
    return Weakened.sources(vampire).strength(Starvation.SOURCE);
  }
}
