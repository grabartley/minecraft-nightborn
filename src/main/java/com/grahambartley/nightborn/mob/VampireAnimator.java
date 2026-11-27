package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.decor.SeatEntity;
import com.grahambartley.nightborn.gear.StakedFall;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.sun.SunFire;
import com.grahambartley.nightborn.vampire.VisibleState;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  static final Set<String> HEAD_CLIPS =
      Stream.concat(Stream.of(VampireEntity.MOURN_CLIP), VampirePose.gestureClips().stream())
          .collect(Collectors.toUnmodifiableSet());

  private final EndingEdge landingEdge = new EndingEdge();
  private final EndingEdge risingEdge = new EndingEdge();
  private final ShownAction shownAction = new ShownAction();
  private final LevitationGait levitationGait = new LevitationGait();
  private VampirePose shownPose = VampirePose.IDLE;

  public VampirePose shownPose() {
    return shownPose;
  }

  public double levitationSpread() {
    return levitationGait.spread();
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
              final VisibleState visible = VisibleStateAttachment.get(vampire);
              final boolean downed = visible.downed();
              risingEdge.ended(downed);
              levitationGait.observe(
                  vampire.age,
                  levitating,
                  vampire.isSprinting(),
                  LevitationGait.horizontalSpeed(vampire));
              final VampirePose pose =
                  VampirePose.of(
                      new VampirePose.Cue(
                          CoffinRise.pose(vampire),
                          StakedFall.staked(vampire),
                          downed,
                          risingEdge.ending(),
                          !vampire.isAlive(),
                          levitating ? Optional.of(levitationGait.pose()) : Optional.empty(),
                          landingEdge.ending(),
                          state.getLimbSwingAmount(),
                          vampire.getVehicle() instanceof SeatEntity,
                          vampire.reading(),
                          SunFire.burning(vampire)));
              shownPose =
                  pose == VampirePose.IDLE
                      ? IdleFidget.pose(
                          vampire.getId(),
                          vampire.age,
                          IdleFidget.calm(
                              vampire.handSwinging,
                              vampire.isAttacking(),
                              shownAction.playing(),
                              visible.feeding()))
                      : pose;
              final PlayState play =
                  state.setAndContinue(
                      vampire
                          .poseClip(shownPose)
                          .orElseGet(() -> StageClips.clip(shownPose, vampire.stage().look())));
              landingEdge.shown(
                  pose == VampirePose.LAND,
                  pose == VampirePose.LAND && state.getController().hasAnimationFinished());
              risingEdge.shown(
                  pose == VampirePose.RISE,
                  pose == VampirePose.RISE && state.getController().hasAnimationFinished());
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
            state ->
                shownAction.play(
                    state,
                    vampire.actionSequence(),
                    vampire.action(),
                    VampirePose.yields(vampire.action()),
                    shownPose.allowsActions()
                        && !vampire.handSwinging
                        && !VisibleStateAttachment.get(vampire).feeding())));
    controllers.add(
        new AnimationController<>(vampire, HISS_CONTROLLER, 2, state -> PlayState.STOP)
            .triggerableAnim(HISS_CLIP, HISS));
  }
}
