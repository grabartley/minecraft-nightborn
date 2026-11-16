package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

public final class MindTies {

  public static final AttachmentType<UUID> MESMERISED_BY =
      AttachmentRegistry.createPersistent(
          Identifier.of(Nightborn.MOD_ID, "mesmerised_by"), Uuids.INT_STREAM_CODEC);
  public static final AttachmentType<UUID> CHARMED_BY =
      AttachmentRegistry.createPersistent(
          Identifier.of(Nightborn.MOD_ID, "charmed_by"), Uuids.INT_STREAM_CODEC);

  private MindTies() {}

  public static void register() {}

  public static boolean mesmerised(final LivingEntity entity) {
    return entity.hasStatusEffect(MindEffect.MESMERISED);
  }

  public static boolean charmed(final LivingEntity entity) {
    return entity.hasStatusEffect(MindEffect.CHARMED);
  }

  public static boolean mesmerisedBy(final LivingEntity entity, final Entity caster) {
    return tiedTo(entity, MindEffect.MESMERISED, MESMERISED_BY, caster);
  }

  public static boolean charmedBy(final LivingEntity entity, final Entity charmer) {
    return tiedTo(entity, MindEffect.CHARMED, CHARMED_BY, charmer);
  }

  public static Optional<UUID> mesmeriser(final LivingEntity entity) {
    return mesmerised(entity)
        ? Optional.ofNullable(entity.getAttached(MESMERISED_BY))
        : Optional.empty();
  }

  static boolean tiedTo(
      final LivingEntity entity,
      final RegistryEntry<StatusEffect> effect,
      final AttachmentType<UUID> tie,
      final Entity other) {
    return entity.hasStatusEffect(effect) && other.getUuid().equals(entity.getAttached(tie));
  }
}
