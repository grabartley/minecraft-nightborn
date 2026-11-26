package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class SkillLayoutTest {

  static final List<Integer> VAMPIRE = List.of(5, 5, 4, 4);
  static final int VAMPIRE_COLUMNS = 7;
  static final List<Integer> HUNTER = List.of(3, 4, 4, 4, 5);
  static final int HUNTER_COLUMNS = 5;
  static final int AGE_FOOTER = 43;
  static final double ROUNDING = 1.0E-6;

  static Stream<Arguments> screens() {
    return Stream.of(
            new int[] {1920, 1080},
            new int[] {960, 540},
            new int[] {854, 480},
            new int[] {640, 360},
            new int[] {569, 320},
            new int[] {480, 270},
            new int[] {427, 240},
            new int[] {320, 240},
            new int[] {320, 180},
            new int[] {240, 160})
        .flatMap(
            size ->
                Stream.of(
                    Arguments.of(size[0], size[1], VAMPIRE, VAMPIRE_COLUMNS, AGE_FOOTER),
                    Arguments.of(size[0], size[1], HUNTER, HUNTER_COLUMNS, 0)));
  }

  @ParameterizedTest(name = "{0}x{1} with bands {2}")
  @MethodSource("screens")
  @DisplayName("the panel, its scrolling trees, the power bar and the age lines all fit the screen")
  void fits(
      final int width,
      final int height,
      final List<Integer> bands,
      final int columns,
      final int footer) {
    final SkillLayout layout = SkillLayout.of(width, height, bands, columns, 0, footer);
    assertTrue(layout.left() >= 0 && layout.top() >= 0, "the panel starts on screen");
    assertTrue(
        (layout.left() + layout.width()) * layout.zoom() <= width + ROUNDING,
        "the panel ends at " + (layout.left() + layout.width()) * layout.zoom());
    assertTrue(
        (layout.bottom() + footer) * layout.zoom() <= height + ROUNDING,
        "the panel and age lines end at " + (layout.bottom() + footer) * layout.zoom());
    assertTrue(layout.pointsY() + SkillPointsRow.HEIGHT <= layout.viewTop(), "header above trees");
    assertTrue(layout.viewBottom() + SkillLayout.BAR_GAP <= layout.powerBarY(), "trees above bar");
    assertTrue(
        layout.powerBarY() + PowerBarRow.SLOT + SkillLayout.PAD <= layout.bottom(),
        "the power bar sits inside the panel");
    assertTrue(
        layout.contentLeft() >= layout.left() + SkillLayout.PAD
            && layout.contentLeft() + layout.contentWidth()
                <= layout.left() + layout.width() - SkillLayout.PAD,
        "the trees sit inside the panel");
    assertTrue(
        layout.viewHeight() >= Math.min(layout.contentHeight(), SkillLayout.MIN_VIEW),
        "at least one band shows");
  }

  @ParameterizedTest(name = "{0}x{1} with bands {2}")
  @MethodSource("screens")
  @DisplayName("gaps between skills are the same on every screen and never shrink")
  void fixedGaps(
      final int width,
      final int height,
      final List<Integer> bands,
      final int columns,
      final int footer) {
    final SkillLayout layout = SkillLayout.of(width, height, bands, columns, 0, footer);
    for (int row = 1; row < columns; row++) {
      assertEquals(
          SkillLayout.NODE + SkillLayout.COLUMN_GAP, layout.nodeX(row) - layout.nodeX(row - 1));
    }
    for (int band = 0; band < bands.size(); band++) {
      for (int lane = 1; lane < bands.get(band); lane++) {
        assertEquals(
            SkillLayout.NODE + SkillLayout.LANE_GAP,
            layout.nodeY(band, lane, 0) - layout.nodeY(band, lane - 1, 0));
      }
    }
  }

  @ParameterizedTest(name = "{0}x{1} with bands {2}")
  @MethodSource("screens")
  @DisplayName("the trees scroll exactly when they are taller than the space for them")
  void scrollsWhenTall(
      final int width,
      final int height,
      final List<Integer> bands,
      final int columns,
      final int footer) {
    final SkillLayout layout = SkillLayout.of(width, height, bands, columns, 0, footer);
    assertEquals(layout.contentHeight() > layout.viewHeight(), layout.scrolls());
    assertEquals(Math.max(0, layout.contentHeight() - layout.viewHeight()), layout.maxScroll());
    if (layout.scrolls()) {
      assertTrue(
          layout.scrollbarX() + SkillLayout.SCROLLBAR
              <= layout.left() + layout.width() - SkillLayout.PAD,
          "the scrollbar sits inside the panel");
      assertTrue(
          layout.scrollbarX() >= layout.contentLeft() + layout.contentWidth(),
          "the scrollbar is clear of the trees");
    }
  }

  @ParameterizedTest(name = "{0}x{1}: zoom {2}")
  @CsvSource({
    "1920, 1080, false",
    "854, 480, false",
    "640, 360, false",
    "427, 240, false",
    "320, 180, true",
    "240, 160, true"
  })
  @DisplayName("the panel zooms out only when the screen cannot hold its fixed parts")
  void zoom(final int width, final int height, final boolean zooms) {
    assertEquals(
        zooms, SkillLayout.of(width, height, VAMPIRE, VAMPIRE_COLUMNS, 0, AGE_FOOTER).zoom() < 1.0);
  }

  @ParameterizedTest(name = "{0}x{1}")
  @CsvSource({"1920, 1080", "640, 360", "427, 240"})
  @DisplayName("a tall screen shows every band at once, and a short one scrolls")
  void tallScreenShowsAll(final int width, final int height) {
    final SkillLayout layout = SkillLayout.of(width, height, HUNTER, HUNTER_COLUMNS, 0, 0);
    assertEquals(height < 700, layout.scrolls());
  }

  @ParameterizedTest(name = "bands {0}")
  @MethodSource("bandSets")
  @DisplayName("bands stack top to bottom, each with its banner above its lanes and a gap between")
  void bandsStack(final List<Integer> bands) {
    final SkillLayout layout = SkillLayout.of(1920, 1080, bands, 3, 0, 0);
    int expected = SkillLayout.VIEW_INSET;
    for (int band = 0; band < bands.size(); band++) {
      assertEquals(expected, layout.bandTops().get(band));
      assertEquals(
          layout.bannerY(band, 0) + SkillLayout.BANNER + SkillLayout.BANNER_GAP,
          layout.nodeY(band, 0, 0));
      expected += SkillLayout.bandHeight(bands.get(band)) + SkillLayout.BAND_GAP;
    }
    assertEquals(expected - SkillLayout.BAND_GAP + SkillLayout.VIEW_INSET, layout.contentHeight());
  }

  static Stream<List<Integer>> bandSets() {
    return Stream.of(List.of(1), List.of(3, 4), VAMPIRE, HUNTER);
  }

  @ParameterizedTest(name = "scroll {0}")
  @CsvSource({"0", "10", "28", "100"})
  @DisplayName("scrolling moves every skill and banner up by the same amount")
  void scrollMoves(final int scroll) {
    final SkillLayout layout = SkillLayout.of(640, 360, VAMPIRE, VAMPIRE_COLUMNS, 0, AGE_FOOTER);
    assertEquals(layout.nodeY(2, 1, 0) - scroll, layout.nodeY(2, 1, scroll));
    assertEquals(layout.bannerY(3, 0) - scroll, layout.bannerY(3, scroll));
  }

  @ParameterizedTest(name = "{0}x{1}")
  @CsvSource({"640, 360", "427, 240", "854, 480"})
  @DisplayName(
      "the scrollbar thumb runs the full track and maps back to within one pixel of the scroll")
  void scrollbar(final int width, final int height) {
    final SkillLayout layout =
        SkillLayout.of(width, height, VAMPIRE, VAMPIRE_COLUMNS, 0, AGE_FOOTER);
    assertTrue(layout.scrolls());
    assertEquals(layout.viewTop(), layout.thumbY(0));
    assertEquals(layout.viewBottom() - layout.thumbHeight(), layout.thumbY(layout.maxScroll()));
    assertTrue(layout.thumbHeight() >= SkillLayout.MIN_THUMB);
    assertTrue(layout.thumbHeight() < layout.viewHeight());
    final double onePixel =
        (double) layout.maxScroll() / (layout.viewHeight() - layout.thumbHeight());
    for (int scroll = 0; scroll <= layout.maxScroll(); scroll += 7) {
      assertEquals(scroll, layout.scrollForThumb(layout.thumbY(scroll)), onePixel);
    }
  }

  @ParameterizedTest(name = "mouse at {0},{1}")
  @CsvSource({"1, 0, false", "1, 1, true", "1, 5, false", "0, 1, false", "0, -1, false"})
  @DisplayName("the scrollbar and its thumb answer the pointer only inside their own strip")
  void overScrollbar(final int across, final int down, final boolean onBar) {
    final SkillLayout layout = SkillLayout.of(640, 360, VAMPIRE, VAMPIRE_COLUMNS, 0, AGE_FOOTER);
    final double x = across == 1 ? layout.scrollbarX() + 1 : layout.scrollbarX() - 1;
    final double y =
        switch (down) {
          case 1 -> layout.viewTop() + 1;
          case 5 -> layout.viewBottom() + 1;
          default -> layout.viewTop() - 1;
        };
    assertEquals(onBar, layout.overScrollbar(x, y));
    assertEquals(onBar, layout.overThumb(x, y, 0));
  }

  @ParameterizedTest(name = "y {0} in view {1}")
  @CsvSource({"-1, false", "0, true", "1, true", "100, false"})
  @DisplayName("skills scrolled under the header or the power bar cannot be hovered or clicked")
  void inView(final int fromTop, final boolean inside) {
    final SkillLayout layout = SkillLayout.of(640, 360, VAMPIRE, VAMPIRE_COLUMNS, 0, AGE_FOOTER);
    final int y = fromTop == 100 ? layout.viewBottom() : layout.viewTop() + fromTop;
    assertEquals(inside, layout.inView(y));
  }

  @ParameterizedTest(name = "screen {0} at zoom {1} -> {2}")
  @CsvSource({"100, 1.0, 100", "100, 0.5, 200", "0, 0.8, 0"})
  @DisplayName("the mouse is read in the shrunken screen's own units")
  void unzoom(final double screen, final double zoom, final double layout) {
    assertEquals(
        layout, new SkillLayout(0, 0, 0, 0, 0, 0, 0, 0, List.of(), zoom).unzoom(screen), 1.0E-9);
  }

  @ParameterizedTest(name = "layout {0} at zoom {1} -> {2}")
  @CsvSource({"100, 1.0, 100", "100, 0.5, 50", "33, 0.5, 17"})
  @DisplayName("the trees are clipped in real screen units, whatever the zoom")
  void screen(final int layout, final double zoom, final int screen) {
    assertEquals(screen, new SkillLayout(0, 0, 0, 0, 0, 0, 0, 0, List.of(), zoom).screen(layout));
  }

  @ParameterizedTest(name = "mouse at {0},{1} -> {2}")
  @CsvSource({"10, 10, true", "31, 31, true", "32, 10, false", "9, 10, false", "10, 32, false"})
  @DisplayName("a node is hovered only inside its square")
  void hover(final double mouseX, final double mouseY, final boolean inside) {
    assertEquals(inside, SkillLayout.contains(10, 10, mouseX, mouseY));
  }

  @ParameterizedTest(name = "title {0} px")
  @CsvSource({"40", "120", "400"})
  @DisplayName("a branch banner wraps its title and never runs past the trees")
  void banner(final int text) {
    final SkillLayout layout = SkillLayout.of(640, 360, HUNTER, HUNTER_COLUMNS, 0, 0);
    final int banner = layout.bannerWidth(text);
    assertTrue(banner <= layout.contentWidth(), "inside the trees");
    assertTrue(banner >= Math.min(text, layout.contentWidth()), "wraps the title");
  }

  @ParameterizedTest(name = "{0}x{1}, header {2} px")
  @CsvSource({"480, 270, 150", "480, 270, 250", "320, 240, 230", "1920, 1080, 0"})
  @DisplayName("the panel is always wide enough for the points row, and the trees stay centred")
  void fitsHeader(final int width, final int height, final int header) {
    final SkillLayout layout = SkillLayout.of(width, height, HUNTER, HUNTER_COLUMNS, header, 0);
    assertTrue(header <= layout.width() - SkillLayout.PAD * 2, "width " + layout.width());
    final int scrollRoom = layout.scrolls() ? SkillLayout.SCROLLBAR_GAP + SkillLayout.SCROLLBAR : 0;
    final int leftSpace = layout.contentLeft() - layout.left();
    final int rightSpace =
        layout.left() + layout.width() - layout.contentLeft() - layout.contentWidth() - scrollRoom;
    assertTrue(Math.abs(leftSpace - rightSpace) <= 1, leftSpace + " vs " + rightSpace);
  }
}
