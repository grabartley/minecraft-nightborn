package com.grahambartley.nightborn.mob;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import software.bernie.geckolib.animation.RawAnimation;

public final class ClipCache {

  private static final Map<String, RawAnimation> ONCE = new ConcurrentHashMap<>();
  private static final Map<String, RawAnimation> LOOPS = new ConcurrentHashMap<>();

  private ClipCache() {}

  public static RawAnimation once(final String clip) {
    return ONCE.computeIfAbsent(clip, name -> RawAnimation.begin().thenPlay(name));
  }

  public static RawAnimation loop(final String clip) {
    return LOOPS.computeIfAbsent(clip, name -> RawAnimation.begin().thenLoop(name));
  }
}
