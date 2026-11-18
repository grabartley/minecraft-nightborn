package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.clothing.ClothingMotion;
import com.grahambartley.nightborn.clothing.ClothingRenderProviders;
import com.grahambartley.nightborn.clothing.Dressed;
import dev.emi.trinkets.api.TrinketItem;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AccessoryItem extends Item implements GeoItem, Dressed {

  private final String id;
  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public AccessoryItem(final String id, final Settings settings) {
    super(settings);
    this.id = id;
  }

  @Override
  public String artId() {
    return this.id;
  }

  @Override
  public int undyedColor(final RegistryWrapper.WrapperLookup registries) {
    return Accessories.of(registries, this).map(Accessory::color).orElse(Accessory.UNDYED);
  }

  @Override
  public TypedActionResult<ItemStack> use(
      final World world, final PlayerEntity user, final Hand hand) {
    final ItemStack stack = user.getStackInHand(hand);
    return TrinketItem.equipItem(user, stack)
        ? TypedActionResult.success(stack, world.isClient())
        : super.use(world, user, hand);
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    if (context.getRegistryLookup() != null) {
      Accessories.of(context.getRegistryLookup(), stack)
          .flatMap(Accessory::worksFor)
          .map(AccessoryTooltip::worksFor)
          .ifPresent(tooltip::add);
    }
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(ClothingMotion.controller(this));
  }

  @Override
  public void createGeoRenderer(final Consumer<GeoRenderProvider> consumer) {
    consumer.accept(ClothingRenderProviders.forItem(this));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
