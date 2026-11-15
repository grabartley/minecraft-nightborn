package com.grahambartley.nightborn.decor;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FurnitureBlockEntity extends BlockEntity implements GeoBlockEntity {

  private static final String CONTROLLER = "toggle";

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final ToggleClips clips = new ToggleClips();

  public FurnitureBlockEntity(final BlockPos pos, final BlockState state) {
    super(DecorBlockEntities.FURNITURE, pos, state);
  }

  public AnimatedFurniture furniture() {
    return (AnimatedFurniture) this.getCachedState().getBlock();
  }

  public boolean isActive() {
    return this.getCachedState().get(this.furniture().toggleProperty());
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            CONTROLLER,
            0,
            state -> {
              this.clips
                  .observe(this.isActive())
                  .ifPresent(names -> state.getController().setAnimation(sequence(names)));
              return PlayState.CONTINUE;
            }));
  }

  private static RawAnimation sequence(final List<String> names) {
    RawAnimation animation = RawAnimation.begin();
    for (int index = 0; index < names.size() - 1; index++) {
      animation = animation.thenPlay(names.get(index));
    }
    return animation.thenLoop(names.getLast());
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
