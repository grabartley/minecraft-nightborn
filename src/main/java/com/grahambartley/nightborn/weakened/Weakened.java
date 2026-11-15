package com.grahambartley.nightborn.weakened;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.WeakenedSettings;
import com.grahambartley.nightborn.vampire.Modifiers;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;

public final class Weakened {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "weakened");

  public static final AttachmentType<WeakenedSources> TYPE =
      AttachmentRegistry.create(ID, builder -> builder.persistent(WeakenedSources.CODEC));

  static final Identifier SLOWER = Identifier.of(Nightborn.MOD_ID, "weakened_speed");
  static final Identifier WEAKER = Identifier.of(Nightborn.MOD_ID, "weakened_blows");

  private Weakened() {}

  public static void register() {}

  public static WeakenedSources sources(final LivingEntity entity) {
    return entity.getAttachedOrElse(TYPE, WeakenedSources.NONE);
  }

  public static void add(
      final LivingEntity entity, final String sourceId, final int strength, final int ticks) {
    store(entity, sources(entity).with(sourceId, strength, ticks));
  }

  public static void set(
      final LivingEntity entity, final String sourceId, final int strength, final int ticks) {
    store(entity, sources(entity).replacing(sourceId, strength, ticks));
  }

  public static int tick(
      final LivingEntity vampire, final boolean atCap, final WeakenedSettings settings) {
    final WeakenedSources ticked = sources(vampire).tick();
    store(vampire, ticked);
    final int level = ticked.level(atCap);
    final WeakenedEffects effects = WeakenedEffects.of(level, settings);
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_MOVEMENT_SPEED,
        SLOWER,
        effects.speedMultiplier(),
        Operation.ADD_MULTIPLIED_TOTAL);
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_ATTACK_DAMAGE,
        WEAKER,
        effects.damageMultiplier(),
        Operation.ADD_MULTIPLIED_TOTAL);
    return level;
  }

  public static void clear(final LivingEntity entity) {
    entity.removeAttached(TYPE);
    Modifiers.remove(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, SLOWER);
    Modifiers.remove(entity, EntityAttributes.GENERIC_ATTACK_DAMAGE, WEAKER);
  }

  private static void store(final LivingEntity entity, final WeakenedSources sources) {
    if (sources.isEmpty()) {
      if (entity.hasAttached(TYPE)) {
        entity.removeAttached(TYPE);
      }
    } else {
      entity.setAttached(TYPE, sources);
    }
  }
}
