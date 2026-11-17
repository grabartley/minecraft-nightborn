package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.age.Ageing;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class BloodBottleItem extends Item {

  static final int DRINK_TICKS = 32;
  static final String GIFT_TOOLTIP = "item.nightborn.blood_bottle.gift";

  public BloodBottleItem(final Settings settings) {
    super(settings);
  }

  public static ItemStack of(final BloodQuality quality) {
    return of(quality, Optional.empty());
  }

  public static ItemStack of(final BloodQuality quality, final Optional<Identifier> gift) {
    final ItemStack stack = new ItemStack(BloodItems.BLOOD_BOTTLE);
    stack.set(BloodItems.BLOOD_QUALITY, quality);
    gift.ifPresent(id -> stack.set(BloodItems.BLOOD_GIFT, id));
    return stack;
  }

  public static Optional<Identifier> giftOf(final ItemStack stack) {
    return Optional.ofNullable(stack.get(BloodItems.BLOOD_GIFT));
  }

  public static BloodQuality qualityOf(final ItemStack stack) {
    return stack.getOrDefault(BloodItems.BLOOD_QUALITY, BloodQuality.ANIMAL);
  }

  public static int bloodRestored(final BloodQuality quality, final NightbornConfig config) {
    return Math.round(
        BloodValue.worth(
            config.feeding().bottleSize(),
            quality.worthPercent(config),
            config.blood().bottledBlood()));
  }

  public static void drink(final PlayerEntity player, final BloodQuality quality) {
    final int before = BloodAttachment.get(player);
    BloodAttachment.set(player, before + bloodRestored(quality, ServerConfigService.current()));
    if (player instanceof ServerPlayerEntity vampire) {
      Ageing.drank(vampire, BloodAttachment.get(player) - before);
    }
  }

  @Override
  public TypedActionResult<ItemStack> use(
      final World world, final PlayerEntity user, final Hand hand) {
    if (!Vampires.isVampire(user)) {
      return TypedActionResult.fail(user.getStackInHand(hand));
    }
    return ItemUsage.consumeHeldItem(world, user, hand);
  }

  @Override
  public ItemStack finishUsing(final ItemStack stack, final World world, final LivingEntity user) {
    if (!world.isClient && user instanceof PlayerEntity player && Vampires.isVampire(player)) {
      drink(player, qualityOf(stack));
      if (player instanceof ServerPlayerEntity vampire) {
        giftOf(stack).ifPresent(gift -> BloodGiftGrants.grant(vampire, gift, BloodGift.BOTTLED));
      }
      player.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 1.0F, 1.0F);
      return ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE), false);
    }
    return stack;
  }

  @Override
  public int getMaxUseTime(final ItemStack stack, final LivingEntity user) {
    return DRINK_TICKS;
  }

  @Override
  public UseAction getUseAction(final ItemStack stack) {
    return UseAction.DRINK;
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    final BloodQuality quality = qualityOf(stack);
    tooltip.add(
        Text.translatable(
                "item.nightborn.blood_bottle.quality", Text.translatable(quality.translationKey()))
            .formatted(quality.colour()));
    final RegistryWrapper.WrapperLookup registries = context.getRegistryLookup();
    giftOf(stack)
        .flatMap(id -> registries == null ? Optional.empty() : BloodGifts.byId(registries, id))
        .ifPresent(
            gift ->
                tooltip.add(
                    Text.translatable(GIFT_TOOLTIP, Text.translatable(gift.name()))
                        .formatted(Formatting.DARK_PURPLE)));
  }
}
