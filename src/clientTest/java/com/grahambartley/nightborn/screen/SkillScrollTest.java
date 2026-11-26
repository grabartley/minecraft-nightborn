package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lwjgl.glfw.GLFW;

class SkillScrollTest {

  static final int MAX = 300;

  @ParameterizedTest(name = "from {0} by {1} -> {2}")
  @CsvSource({"0, 28, 28", "0, -28, 0", "290, 28, 300", "100, -56, 44", "0, 1000, 300"})
  @DisplayName("scrolling never goes above the first band or below the last")
  void by(final double from, final double amount, final double target) {
    final SkillScroll scroll = new SkillScroll();
    scroll.jump(from, MAX);
    scroll.by(amount, MAX);
    assertEquals(target, scroll.target(), 1.0E-9);
  }

  @Test
  @DisplayName("the trees glide to the new place quickly, never overshoot, and come to rest on it")
  void glides() {
    final SkillScroll scroll = new SkillScroll();
    scroll.to(100, MAX);
    int last = scroll.shown();
    for (int frame = 0; frame < 3; frame++) {
      scroll.step(0.2F);
      assertTrue(scroll.shown() > last && scroll.shown() <= 100, "moved to " + scroll.shown());
      last = scroll.shown();
    }
    for (int frame = 0; frame < 40; frame++) {
      scroll.step(0.2F);
      assertTrue(scroll.shown() <= 100);
    }
    assertEquals(100, scroll.shown());
  }

  @ParameterizedTest(name = "{0} ticks")
  @CsvSource({"0.6, 50", "1.2, 25", "2.4, 6"})
  @DisplayName("the glide halves the distance left every 30 milliseconds")
  void halfLife(final float ticks, final int left) {
    final SkillScroll scroll = new SkillScroll();
    scroll.to(100, MAX);
    scroll.step(ticks);
    assertEquals(100 - left, scroll.shown(), 1);
  }

  @Test
  @DisplayName("dragging the scrollbar moves the trees at once, with no glide behind the mouse")
  void jump() {
    final SkillScroll scroll = new SkillScroll();
    scroll.jump(140, MAX);
    assertEquals(140, scroll.shown());
    assertEquals(140, scroll.target(), 1.0E-9);
  }

  @Test
  @DisplayName("when the screen grows and the trees need less scrolling, the place is pulled back")
  void fit() {
    final SkillScroll scroll = new SkillScroll();
    scroll.jump(250, MAX);
    scroll.fit(120);
    assertEquals(120, scroll.shown());
    assertEquals(120, scroll.target(), 1.0E-9);
    scroll.fit(0);
    assertEquals(0, scroll.shown());
  }

  @ParameterizedTest(name = "key {0} -> {1}")
  @CsvSource({
    GLFW.GLFW_KEY_UP + ", -28",
    GLFW.GLFW_KEY_DOWN + ", 28",
    GLFW.GLFW_KEY_PAGE_UP + ", -150",
    GLFW.GLFW_KEY_PAGE_DOWN + ", 150",
    GLFW.GLFW_KEY_HOME + ", -300",
    GLFW.GLFW_KEY_END + ", 300",
    GLFW.GLFW_KEY_A + ", 0"
  })
  @DisplayName("arrow, page, home and end keys scroll by a lane, a page and all the way")
  void keys(final int key, final double step) {
    assertEquals(step, SkillScroll.forKey(key, 28, 150, MAX), 1.0E-9);
  }
}
