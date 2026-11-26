package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.client.anim.PlayerClips;
import com.grahambartley.nightborn.mob.Gait;
import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class PlayerRigAnimatable implements GeoReplacedEntity {

  public static final PlayerRigAnimatable INSTANCE = new PlayerRigAnimatable();

  static final int BLEND_TICKS = 4;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  private PlayerRigAnimatable() {}

  @Override
  public EntityType<?> getReplacingEntityType() {
    return EntityType.PLAYER;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "stride",
            BLEND_TICKS,
            state ->
                VampirePose.playOrStop(
                    state,
                    switch (Gait.of(state.getLimbSwingAmount())) {
                      case RUN -> Optional.of(VampirePose.RUN);
                      case WALK -> Optional.of(VampirePose.WALK);
                      case STILL -> Optional.empty();
                    })));
    controllers.add(
        new AnimationController<>(
            this,
            "pose",
            BLEND_TICKS,
            state ->
                state.getData(DataTickets.ENTITY) instanceof PlayerEntity player
                    ? PlayerClips.clip(player)
                        .map(pose -> state.setAndContinue(pose.clip()))
                        .orElse(PlayState.STOP)
                    : PlayState.STOP));
    controllers.add(
        new AnimationController<>(this, "power", PlayerClips.POWER_BLEND_TICKS, this::power));
    controllers.add(
        new AnimationController<>(
            this,
            "action",
            BLEND_TICKS,
            state ->
                VampirePose.playOrStop(
                    state,
                    state.getData(DataTickets.ENTITY) instanceof PlayerEntity player
                        ? PlayerClips.action(player)
                        : Optional.empty())));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }

  private PlayState power(final AnimationState<PlayerRigAnimatable> state) {
    if (!(state.getData(DataTickets.ENTITY) instanceof PlayerEntity player)) {
      return PlayState.STOP;
    }
    final Optional<String> clip = PlayerClips.power(player);
    if (clip.isEmpty() || PlayerClips.freshPower(player)) {
      state.getController().forceAnimationReset();
    }
    return clip.map(name -> state.setAndContinue(RawAnimation.begin().thenPlay(name)))
        .orElse(PlayState.STOP);
  }
}
