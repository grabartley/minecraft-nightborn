package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.client.anim.VampirePlayerClips;
import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class VampirePlayerAnimatable implements GeoReplacedEntity {

  public static final VampirePlayerAnimatable INSTANCE = new VampirePlayerAnimatable();

  static final int BLEND_TICKS = 4;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  private VampirePlayerAnimatable() {}

  @Override
  public EntityType<?> getReplacingEntityType() {
    return EntityType.PLAYER;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "pose",
            BLEND_TICKS,
            state ->
                state.getData(DataTickets.ENTITY) instanceof PlayerEntity player
                    ? VampirePlayerClips.clip(player)
                        .map(pose -> state.setAndContinue(pose.clip()))
                        .orElse(PlayState.STOP)
                    : PlayState.STOP));
    controllers.add(
        new AnimationController<>(
            this,
            "action",
            BLEND_TICKS,
            state ->
                VampirePose.playOrStop(
                    state,
                    state.getData(DataTickets.ENTITY) instanceof PlayerEntity player
                        ? VampirePlayerClips.action(player)
                        : Optional.empty())));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
