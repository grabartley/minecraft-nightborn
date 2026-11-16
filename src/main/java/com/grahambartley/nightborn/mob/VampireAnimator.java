package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.decor.SeatEntity;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import java.util.Optional;
import java.util.Set;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public final class VampireAnimator {

  static final String ACTION_CONTROLLER = "action_clip";
  static final int ANIMATION_BLEND_TICKS = 4;
  static final String HISS_CONTROLLER = "hiss";
  static final String HISS_CLIP = "hiss";
  static final RawAnimation HISS = RawAnimation.begin().thenPlay(HISS_CLIP);
  static final Set<String> HEAD_CLIPS = Set.of(VampireEntity.MOURN_CLIP);

  private final EndingEdge landingEdge = new EndingEdge();
  private final ShownAction shownAction = new ShownAction();
  private VampirePose shownPose = VampirePose.IDLE;

  public VampirePose shownPose() {
    return shownPose;
  }

  public boolean holdsHead() {
    return shownPose.holdsHead() || shownAction.showing(HEAD_CLIPS);
  }

  void register(
      final VampireEntity vampire, final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            vampire,
            "pose",
            ANIMATION_BLEND_TICKS,
            state -> {
              final boolean levitating = Levitation.isLevitating(vampire);
              landingEdge.ended(levitating);
              final double speed =
                  Math.hypot(vampire.getX() - vampire.prevX, vampire.getZ() - vampire.prevZ);
              final VampirePose pose =
                  VampirePose.of(
                      new VampirePose.Cue(
                          CoffinRise.pose(vampire),
                          VisibleStateAttachment.get(vampire).downed(),
                          levitating,
                          landingEdge.ending(),
                          vampire.isSprinting(),
                          state.getLimbSwingAmount(),
                          speed,
                          vampire.getVehicle() instanceof SeatEntity,
                          vampire.reading()));
              shownPose =
                  pose == VampirePose.IDLE ? IdleFidget.pose(vampire.getId(), vampire.age) : pose;
              final PlayState play =
                  state.setAndContinue(StageClips.clip(shownPose, vampire.stage().look()));
              landingEdge.shown(
                  pose == VampirePose.LAND,
                  pose == VampirePose.LAND && state.getController().hasAnimationFinished());
              return play;
            }));
    controllers.add(
        new AnimationController<>(
            vampire,
            "action",
            ANIMATION_BLEND_TICKS,
            state ->
                VampirePose.playOrStop(
                    state,
                    shownPose.allowsActions()
                        ? VampirePose.action(vampire.handSwinging, false, true)
                        : Optional.empty())));
    controllers.add(
        new AnimationController<>(
            vampire,
            ACTION_CONTROLLER,
            2,
            state -> shownAction.play(state, vampire.actionSequence(), vampire.action())));
    controllers.add(
        new AnimationController<>(vampire, HISS_CONTROLLER, 2, state -> PlayState.STOP)
            .triggerableAnim(HISS_CLIP, HISS));
  }
}
