package com.grahambartley.nightborn.screen;

import org.lwjgl.glfw.GLFW;

public final class SkillScroll {

  static final double HALF_LIFE_TICKS = 0.6;
  static final double SETTLED = 0.25;

  private double target;
  private double shown;

  public void by(final double amount, final int max) {
    target = clamp(target + amount, max);
  }

  public void to(final double value, final int max) {
    target = clamp(value, max);
  }

  public void jump(final double value, final int max) {
    to(value, max);
    shown = target;
  }

  public void fit(final int max) {
    target = clamp(target, max);
    shown = clamp(shown, max);
  }

  public void step(final float ticks) {
    shown =
        Math.abs(target - shown) < SETTLED
            ? target
            : target + (shown - target) * Math.pow(0.5, Math.max(0.0F, ticks) / HALF_LIFE_TICKS);
  }

  public static double forKey(final int keyCode, final int lane, final int page, final int max) {
    return switch (keyCode) {
      case GLFW.GLFW_KEY_UP -> -lane;
      case GLFW.GLFW_KEY_DOWN -> lane;
      case GLFW.GLFW_KEY_PAGE_UP -> -page;
      case GLFW.GLFW_KEY_PAGE_DOWN -> page;
      case GLFW.GLFW_KEY_HOME -> -max;
      case GLFW.GLFW_KEY_END -> max;
      default -> 0.0;
    };
  }

  public int shown() {
    return (int) Math.round(shown);
  }

  public double target() {
    return target;
  }

  private static double clamp(final double value, final int max) {
    return Math.max(0.0, Math.min(Math.max(0, max), value));
  }
}
