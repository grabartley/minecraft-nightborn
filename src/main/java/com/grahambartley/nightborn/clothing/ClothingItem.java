package com.grahambartley.nightborn.clothing;

import java.util.function.Consumer;
import net.minecraft.item.ArmorItem;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ClothingItem extends ArmorItem implements GeoItem, Dressed {

  private final ClothingPiece piece;
  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public ClothingItem(final ClothingPiece piece, final Settings settings) {
    super(ClothingMaterial.of(piece.fabric()), piece.type(), settings);
    this.piece = piece;
  }

  public ClothingPiece piece() {
    return this.piece;
  }

  @Override
  public Garment garment() {
    return this.piece;
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
