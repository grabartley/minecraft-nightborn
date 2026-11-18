package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.clothing.ClothingMotion;
import com.grahambartley.nightborn.clothing.ClothingRenderProviders;
import com.grahambartley.nightborn.clothing.Dressed;
import com.grahambartley.nightborn.clothing.Garment;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AccessoryItem extends Item implements GeoItem, Dressed {

  private final AccessoryPiece piece;
  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public AccessoryItem(final AccessoryPiece piece, final Settings settings) {
    super(settings);
    this.piece = piece;
  }

  public AccessoryPiece piece() {
    return this.piece;
  }

  @Override
  public Garment garment() {
    return this.piece;
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
