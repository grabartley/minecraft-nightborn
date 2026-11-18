package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.levitation.Levitation;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class Parasols {

  public static final ComponentType<Boolean> OPEN =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(Nightborn.MOD_ID, "parasol_open"),
          ComponentType.<Boolean>builder()
              .codec(Codec.BOOL)
              .packetCodec(PacketCodecs.BOOL)
              .build());

  public static final Item PARASOL =
      Registry.register(
          Registries.ITEM,
          Identifier.of(Nightborn.MOD_ID, "parasol"),
          new ParasolItem(new Item.Settings().maxDamage(ParasolWear.DURABILITY)));

  public static final Identifier OPEN_PREDICATE = Identifier.of(Nightborn.MOD_ID, "open");

  private Parasols() {}

  public static boolean isOpen(final ItemStack stack) {
    return stack.isOf(PARASOL) && stack.getOrDefault(OPEN, false);
  }

  public static float openness(final ItemStack stack, @Nullable final LivingEntity holder) {
    return isOpen(stack) && holder != null && inHand(holder, stack) ? 1.0F : 0.0F;
  }

  public static boolean inHand(final LivingEntity holder, final ItemStack stack) {
    return holder.getMainHandStack() == stack || holder.getOffHandStack() == stack;
  }

  public static boolean closing(final LivingEntity holder) {
    return ParasolRules.closes(
        holder.isSprinting(), Levitation.isLevitating(holder), BatForm.isBat(holder));
  }

  public static boolean shades(final LivingEntity holder) {
    return ParasolRules.shades(openHand(holder).isPresent(), closing(holder));
  }

  public static Optional<Hand> openHand(final LivingEntity holder) {
    return Arrays.stream(Hand.values())
        .filter(hand -> isOpen(holder.getStackInHand(hand)))
        .findFirst();
  }

  public static void setOpen(final LivingEntity holder, final ItemStack stack, final boolean open) {
    if (isOpen(stack) == open) {
      return;
    }
    if (open) {
      stack.set(OPEN, true);
    } else {
      stack.remove(OPEN);
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
