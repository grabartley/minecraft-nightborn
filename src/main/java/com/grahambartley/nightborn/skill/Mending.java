package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.downed.Downed;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class Mending {

  public static final AttachmentType<Mend> TYPE =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "mending"));

  private Mending() {}

  public static void register() {}

  public static void start(final LivingEntity caster, final HealPower heal) {
    caster.setAttached(TYPE, heal.mend());
  }

  public static void tick(final LivingEntity entity) {
    final Mend mend = entity.getAttached(TYPE);
    if (mend == null) {
      return;
    }
    final boolean helpless = !entity.isAlive() || Downed.helpless(entity);
    if (!helpless) {
      entity.heal(mend.perTick());
    }
    entity.setAttached(TYPE, mend.next(helpless));
  }
}
