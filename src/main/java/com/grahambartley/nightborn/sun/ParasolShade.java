package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.SunSettings;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public final class ParasolShade {

  static final AttachmentType<Long> SUN_TICKS =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "parasol_sun_ticks"));

  static final AttachmentType<Long> SHADED_AT =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "parasol_shaded_at"));

  private ParasolShade() {}

  public static long shadedAt(final LivingEntity holder) {
    return holder.getAttachedOrElse(SHADED_AT, Long.MIN_VALUE);
  }

  public static void tick(final LivingEntity holder, final SunSettings settings) {
    final Optional<Hand> hand = Parasols.openHand(holder);
    if (hand.isEmpty()) {
      return;
    }
    if (Parasols.closing(holder)) {
      Parasols.closeAll(holder);
      return;
    }
    if (!DirectSun.reaches(holder)) {
      return;
    }
    final ItemStack parasol = holder.getStackInHand(hand.get());
    final long sunTicks = holder.getAttachedOrElse(SUN_TICKS, 0L) + 1L;
    holder.setAttached(SUN_TICKS, sunTicks);
    final int worn = ParasolWear.points(sunTicks, settings.parasolSeconds());
    if (worn > 0) {
      parasol.damage(worn, holder, LivingEntity.getSlotForHand(hand.get()));
    }
    if (!parasol.isEmpty()) {
      holder.setAttached(SHADED_AT, holder.getWorld().getTime());
      ParasolWalks.step(holder);
    }
  }
}
