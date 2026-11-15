package com.grahambartley.nightborn.screen;

record SliderScale(int min, int max) {

  int clamp(final int value) {
    return Math.max(min, Math.min(max, value));
  }

  double fractionOf(final int value) {
    return max == min ? 0.0 : (clamp(value) - min) / (double) (max - min);
  }

  int valueAt(final double fraction) {
    return clamp((int) Math.round(min + (max - min) * fraction));
  }
}
