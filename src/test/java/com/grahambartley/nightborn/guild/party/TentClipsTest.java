package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.RawAnimation;

class TentClipsTest {

  static Stream<Arguments> firstSight() {
    return Stream.of(
        Arguments.of(false, 0L, List.of(TentClips.PITCH, TentClips.IDLE)),
        Arguments.of(false, TentClips.PITCH_TICKS - 1, List.of(TentClips.PITCH, TentClips.IDLE)),
        Arguments.of(false, TentClips.PITCH_TICKS, List.of(TentClips.IDLE)),
        Arguments.of(false, Long.MAX_VALUE, List.of(TentClips.IDLE)),
        Arguments.of(true, 0L, List.of(TentClips.STRIKE)));
  }

  @ParameterizedTest(name = "struck {0}, age {1}: {2}")
  @MethodSource("firstSight")
  @DisplayName("a tent goes up only when it is seen in its first second, and stands otherwise")
  void firstSightPicksTheClip(final boolean struck, final long age, final List<String> clips) {
    assertEquals(Optional.of(clips), new TentClips().observe(struck, age));
  }

  @ParameterizedTest(name = "age {0}")
  @MethodSource("ages")
  @DisplayName("a standing tent keeps its clip until it is struck, then comes down once")
  void strikeFollowsTheStandingTent(final long age) {
    final TentClips clips = new TentClips();
    clips.observe(false, age);
    assertEquals(Optional.empty(), clips.observe(false, age + 5));
    assertEquals(Optional.of(List.of(TentClips.STRIKE)), clips.observe(true, age + 10));
    assertEquals(Optional.empty(), clips.observe(true, age + 15));
  }

  static Stream<Long> ages() {
    return Stream.of(0L, 400L);
  }

  static Stream<Arguments> sequences() {
    return Stream.of(
        Arguments.of(
            List.of(TentClips.PITCH, TentClips.IDLE),
            List.of(Animation.LoopType.DEFAULT, Animation.LoopType.LOOP)),
        Arguments.of(List.of(TentClips.IDLE), List.of(Animation.LoopType.LOOP)),
        Arguments.of(List.of(TentClips.STRIKE), List.of(Animation.LoopType.DEFAULT)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("sequences")
  @DisplayName("only the idle sway loops, and the others play as their clip file says")
  void onlyIdleLoops(final List<String> names, final List<Animation.LoopType> loops) {
    final RawAnimation animation = TentClips.animation(names);
    assertEquals(
        names,
        animation.getAnimationStages().stream().map(RawAnimation.Stage::animationName).toList());
    assertEquals(
        loops, animation.getAnimationStages().stream().map(RawAnimation.Stage::loopType).toList());
  }
}
