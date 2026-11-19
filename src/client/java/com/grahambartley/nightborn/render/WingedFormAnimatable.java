package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.batform.WingedForm;
import com.grahambartley.nightborn.batform.WingedState;
import com.grahambartley.nightborn.mob.NightbornMobs;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class WingedFormAnimatable implements GeoReplacedEntity {

  static final int STRIKE_BLEND_TICKS = 2;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  @Override
  public EntityType<?> getReplacingEntityType() {
    return NightbornMobs.VAMPIRE_BAT;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    final WingedCues cues = new WingedCues();
    controllers.add(BatFormAnimatable.flight(this));
    controllers.add(
        new AnimationController<>(
            this, "strike", STRIKE_BLEND_TICKS, state -> strike(state, cues)));
  }

  private static PlayState strike(
      final AnimationState<WingedFormAnimatable> state, final WingedCues cues) {
    if (!(state.getData(DataTickets.ENTITY) instanceof LivingEntity entity)) {
      return PlayState.STOP;
    }
    final Optional<WingedStrike> begun =
        cues.observe(
            WingedForm.state(entity).map(WingedState::since).orElse(0L),
            entity.getWorld().getTime(),
            entity.hurtTime,
            entity.handSwinging);
    final AnimationController<WingedFormAnimatable> controller = state.getController();
    final Optional<WingedStrike> playing =
        WingedStrike.playing(controller.getCurrentRawAnimation())
            .filter(clip -> !controller.hasAnimationFinished());
    final Optional<WingedStrike> next = WingedStrike.next(begun, playing);
    if (next.isEmpty()) {
      return PlayState.STOP;
    }
    if (next.equals(begun)) {
      controller.forceAnimationReset();
    }
    return state.setAndContinue(next.get().clip());
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
