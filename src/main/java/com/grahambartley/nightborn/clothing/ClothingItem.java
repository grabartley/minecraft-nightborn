package com.grahambartley.nightborn.clothing;

import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ClothingItem extends ArmorItem implements GeoItem {

  private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(ClothingMotion.IDLE);
  private static final RawAnimation WALK = RawAnimation.begin().thenLoop(ClothingMotion.WALK);

  private final ClothingPiece piece;
  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public ClothingItem(final ClothingPiece piece, final Settings settings) {
    super(ClothingMaterial.CLOTH, piece.type(), settings);
    this.piece = piece;
  }

  public ClothingPiece piece() {
    return this.piece;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(new AnimationController<>(this, "motion", 4, this::motion));
  }

  private PlayState motion(final AnimationState<ClothingItem> state) {
    final Entity wearer = state.getData(DataTickets.ENTITY);
    final float limbSpeed =
        wearer instanceof LivingEntity living ? living.limbAnimator.getSpeed() : 0;
    return state.setAndContinue(
        ClothingMotion.WALK.equals(ClothingMotion.clip(limbSpeed)) ? WALK : IDLE);
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
