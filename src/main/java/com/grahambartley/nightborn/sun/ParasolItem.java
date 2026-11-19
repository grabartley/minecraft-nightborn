package com.grahambartley.nightborn.sun;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ParasolItem extends Item implements GeoItem {

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public ParasolItem(final Settings settings) {
    super(settings);
  }

  @Override
  public TypedActionResult<ItemStack> use(
      final World world, final PlayerEntity user, final Hand hand) {
    final ItemStack stack = user.getStackInHand(hand);
    final ParasolRules.Use use = ParasolRules.use(Parasols.isOpen(stack), Parasols.closing(user));
    if (use == ParasolRules.Use.REFUSE) {
      return TypedActionResult.fail(stack);
    }
    if (!world.isClient) {
      Parasols.setOpen(user, stack, use == ParasolRules.Use.OPEN);
    }
    return TypedActionResult.success(stack, world.isClient);
  }

  @Override
  public void inventoryTick(
      final ItemStack stack,
      final World world,
      final Entity entity,
      final int slot,
      final boolean selected) {
    if (world instanceof ServerWorld server) {
      GeoItem.getOrAssignId(stack, server);
    }
    if (!world.isClient
        && Parasols.isOpen(stack)
        && entity instanceof LivingEntity holder
        && !Parasols.inHand(holder, stack)) {
      Parasols.setOpen(holder, stack, false);
    }
  }

  @Override
  public boolean canRepair(final ItemStack stack, final ItemStack ingredient) {
    return ingredient.isIn(ItemTags.WOOL);
  }

  @Override
  public boolean isEnchantable(final ItemStack stack) {
    return false;
  }

  @Override
  public int getEnchantability() {
    return 0;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "canopy",
            0,
            state ->
                state.setAndContinue(
                    ParasolClips.next(
                        state.getController().getCurrentRawAnimation(),
                        Parasols.isOpen(state.getData(DataTickets.ITEMSTACK)),
                        GeoItem.getId(state.getData(DataTickets.ITEMSTACK)) != Long.MAX_VALUE))));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
