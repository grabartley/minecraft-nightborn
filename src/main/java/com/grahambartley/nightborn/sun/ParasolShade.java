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

  private ParasolShade() {}

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
      ParasolWalks.step(holder);
    }
  }
}
