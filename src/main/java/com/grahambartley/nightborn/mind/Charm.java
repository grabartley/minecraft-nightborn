package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.village.VillageSuspicion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class Charm {

  private Charm() {}

  public static boolean charms(
      final boolean mesmerisedByPlayer,
      final boolean alreadyCharmedByPlayer,
      final int charm,
      final boolean sneaking) {
    return mesmerisedByPlayer && !alreadyCharmedByPlayer && charm > 0 && !sneaking;
  }

  static ActionResult onUse(
      final PlayerEntity player,
      final World world,
      final Hand hand,
      final Entity entity,
      @Nullable final EntityHitResult hit) {
    if (hand == Hand.MAIN_HAND
        && player instanceof ServerPlayerEntity vampire
        && entity instanceof VillagerEntity villager
        && charms(
            MindTies.mesmerisedBy(villager, vampire),
            MindTies.charmedBy(villager, vampire),
            MindAttributes.charm(vampire),
            vampire.isSneaking())) {
      charm(vampire, villager);
    }
    return ActionResult.PASS;
  }

  static void charm(final ServerPlayerEntity vampire, final VillagerEntity villager) {
    final int ticks = ServerConfigService.current().minds().charmSeconds() * 20;
    villager.setAttached(MindTies.CHARMED_BY, vampire.getUuid());
    VillageSuspicion.charmed(vampire.getServerWorld(), villager);
    villager.addStatusEffect(new StatusEffectInstance(MindEffect.CHARMED, ticks), vampire);
    villager
        .getWorld()
        .playSound(
            null,
            villager.getBlockPos(),
            SoundEvents.ENTITY_VILLAGER_CELEBRATE,
            SoundCategory.NEUTRAL,
            1.0F,
            0.8F);
    vampire.sendMessage(
        Text.translatable("nightborn.charm.charmed", villager.getDisplayName()), true);
  }

  public static void discount(final VillagerEntity villager, final PlayerEntity customer) {
    final int charm = MindAttributes.charm(customer);
    if (!CharmDiscount.applies(
        MindTies.charmedBy(villager, customer),
        charm,
        customer.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE))) {
      return;
    }
    for (final TradeOffer offer : villager.getOffers()) {
      offer.increaseSpecialPrice(
          -CharmDiscount.cut(offer.getOriginalFirstBuyItem().getCount(), charm));
    }
  }
}
