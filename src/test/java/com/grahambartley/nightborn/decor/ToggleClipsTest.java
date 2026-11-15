package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.RawAnimation;

class ToggleClipsTest {

  static Stream<Arguments> sequences() {
    return Stream.of(
        Arguments.of(List.of(false), List.of(Optional.of(List.of("inactive")))),
        Arguments.of(List.of(true), List.of(Optional.of(List.of("active")))),
        Arguments.of(
            List.of(false, false, true),
            List.of(
                Optional.of(List.of("inactive")),
                Optional.empty(),
                Optional.of(List.of("activate", "active")))),
        Arguments.of(
            List.of(true, false, false),
            List.of(
                Optional.of(List.of("active")),
                Optional.of(List.of("deactivate", "inactive")),
                Optional.empty())));
  }

  @ParameterizedTest(name = "{0} plays {1}")
  @MethodSource("sequences")
  void holdsOnFirstSightAndTransitionsOnlyOnChange(
      final List<Boolean> observed, final List<Optional<List<String>>> expected) {
    final ToggleClips clips = new ToggleClips(ToggleClips.FURNITURE);
    final List<Optional<List<String>>> played = new ArrayList<>();
    observed.forEach(active -> played.add(clips.observe(active)));
    assertEquals(expected, played);
  }

  static Stream<Arguments> sequenced() {
    return Stream.of(
        Arguments.of(List.of("active"), List.of("active:loop")),
        Arguments.of(List.of("activate", "active"), List.of("activate:play", "active:loop")),
        Arguments.of(
            List.of("deactivate", "inactive"), List.of("deactivate:play", "inactive:loop")));
  }

  @ParameterizedTest(name = "{0} becomes {1}")
  @MethodSource("sequenced")
  void playsEachTransitionOnceThenLoopsTheHold(
      final List<String> names, final List<String> expected) {
    final RawAnimation animation = ToggleClips.sequence(names);
    assertEquals(
        expected,
        animation.getAnimationStages().stream()
            .map(
                stage ->
                    stage.animationName()
                        + (stage.loopType() == Animation.LoopType.LOOP ? ":loop" : ":play"))
            .toList());
  }

  static Stream<Arguments> named() {
    final ToggleClips.Names lid = new ToggleClips.Names("open", "opened", "close", "closed");
    return Stream.of(
        Arguments.of(lid, List.of(true), List.of(Optional.of(List.of("opened")))),
        Arguments.of(
            lid,
            List.of(false, true, false),
            List.of(
                Optional.of(List.of("closed")),
                Optional.of(List.of("open", "opened")),
                Optional.of(List.of("close", "closed")))));
  }

  @ParameterizedTest(name = "{1} plays {2}")
  @MethodSource("named")
  void playsTheClipNamesItWasGiven(
      final ToggleClips.Names names,
      final List<Boolean> observed,
      final List<Optional<List<String>>> expected) {
    final ToggleClips clips = new ToggleClips(names);
    final List<Optional<List<String>>> played = new ArrayList<>();
    observed.forEach(active -> played.add(clips.observe(active)));
    assertEquals(expected, played);
  }
}
