package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.levitation.Levitation;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class Parasols {

  public static final Identifier OPEN_PREDICATE = Identifier.of(Nightborn.MOD_ID, "open");

  private Parasols() {}

  public static boolean isOpen(final ItemStack stack) {
    return stack.isOf(ParasolItems.PARASOL) && stack.getOrDefault(ParasolItems.OPEN, false);
  }

  public static float openness(final ItemStack stack, @Nullable final LivingEntity holder) {
    return isOpen(stack) && holder != null && inHand(holder, stack) ? 1.0F : 0.0F;
  }

  public static boolean inHand(final LivingEntity holder, final ItemStack stack) {
    return holder.getMainHandStack() == stack || holder.getOffHandStack() == stack;
  }

  public static boolean closing(final LivingEntity holder) {
    return ParasolRules.closes(
        holder.isSprinting(),
        Levitation.isLevitating(holder),
        BatForm.isBat(holder),
        holder.isSleeping());
  }

  public static boolean shades(final LivingEntity holder) {
    return ParasolRules.shades(openHand(holder).isPresent(), closing(holder));
  }

  public static Optional<Hand> openHand(final LivingEntity holder) {
    if (isOpen(holder.getMainHandStack())) {
      return Optional.of(Hand.MAIN_HAND);
    }
    return isOpen(holder.getOffHandStack()) ? Optional.of(Hand.OFF_HAND) : Optional.empty();
  }

  public static void fold(final ItemStack stack) {
    if (stack.isOf(ParasolItems.PARASOL)) {
      stack.remove(ParasolItems.OPEN);
    }
  }

  public static void setOpen(final LivingEntity holder, final ItemStack stack, final boolean open) {
    if (isOpen(stack) == open) {
      return;
    }
    if (open) {
      stack.set(ParasolItems.OPEN, true);
    } else {
      stack.remove(ParasolItems.OPEN);
    }
    holder
        .getWorld()
        .playSound(
            null,
            holder.getBlockPos(),
            open
                ? SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA.value()
                : SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value(),
            holder.getSoundCategory(),
            0.8F,
            open ? 1.2F : 1.0F);
  }

  public static void closeAll(final LivingEntity holder) {
    for (final Hand hand : Hand.values()) {
      setOpen(holder, holder.getStackInHand(hand), false);
    }
  }
}
