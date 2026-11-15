package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class SkillAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "skills");

  public static final AttachmentType<SkillProgress> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder
                  .persistent(SkillProgress.CODEC)
                  .copyOnDeath()
                  .syncWith(
                      SkillProgress.PACKET_CODEC.cast(), AttachmentSyncPredicate.targetOnly()));

  private SkillAttachment() {}

  public static void register() {}

  public static SkillProgress get(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, SkillProgress.EMPTY);
  }

  public static TreeProgress get(final AttachmentTarget target, final SkillTree tree) {
    return get(target).get(tree);
  }

  public static TreeProgress owned(final LivingEntity entity) {
    return SkillTree.of(entity).map(tree -> get(entity, tree)).orElse(TreeProgress.EMPTY);
  }

  public static void set(
      final AttachmentTarget target, final SkillTree tree, final TreeProgress progress) {
    set(target, get(target).with(tree, progress));
  }

  public static void set(final AttachmentTarget target, final SkillProgress progress) {
    target.setAttached(TYPE, progress.isEmpty() ? null : progress);
  }

  public static void clear(final AttachmentTarget target) {
    target.removeAttached(TYPE);
  }
}
