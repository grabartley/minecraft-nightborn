package com.grahambartley.nightborn.screen;

import java.util.ArrayList;
import java.util.List;

public record SkillLayout(
    int left,
    int top,
    int width,
    int height,
    int contentLeft,
    int contentWidth,
    int contentHeight,
    int viewHeight,
    List<Integer> bandTops,
    double zoom) {

  public static final int NODE = 22;
  static final int COLUMN_GAP = 16;
  static final int LANE_GAP = 6;
  static final int COLUMN_STEP = NODE + COLUMN_GAP;
  static final int LANE_STEP = NODE + LANE_GAP;
  static final int BAND_GAP = 10;
  static final int VIEW_INSET = 4;
  static final int BANNER = 13;
  static final int BANNER_GAP = 4;
  static final int BANNER_PAD = 8;
  static final int BANNER_TEXT_INSET = 3;
  static final int POINTS_TOP = 20;
  static final int HEADER_GAP = 6;
  static final int BAR_GAP = 6;
  static final int PAD = 8;
  static final int MARGIN = 4;
  static final int MIN_WIDTH = 200;
  static final int SCROLLBAR = 6;
  static final int SCROLLBAR_GAP = 6;
  static final int MIN_THUMB = 16;
  static final int SHOWN_LANES = 3;
  static final int MIN_VIEW = BANNER + BANNER_GAP + LANE_STEP * SHOWN_LANES - LANE_GAP;
  static final int HEADER = POINTS_TOP + SkillPointsRow.HEIGHT + HEADER_GAP;
  static final int FOOTER = BAR_GAP + PowerBarRow.SLOT + PAD;
  static final double ROUNDING = 1.0E-6;

  public SkillLayout {
    bandTops = List.copyOf(bandTops);
  }

  public static SkillLayout of(
      final int screenWidth,
      final int screenHeight,
      final List<Integer> bandLanes,
      final int columns,
      final int headerWidth,
      final int footer) {
    final List<Integer> bandTops = new ArrayList<>();
    int contentHeight = VIEW_INSET;
    for (final int lanes : bandLanes) {
      if (!bandTops.isEmpty()) {
        contentHeight += BAND_GAP;
      }
      bandTops.add(contentHeight);
      contentHeight += bandHeight(lanes);
    }
    contentHeight = Math.max(BANNER, contentHeight) + VIEW_INSET;
    final int contentWidth = Math.max(1, columns) * COLUMN_STEP - COLUMN_GAP;
    final int shortestView = Math.min(contentHeight, MIN_VIEW);
    final int leastWidth = panelWidth(contentWidth, true, headerWidth) + MARGIN * 2;
    final int leastHeight = HEADER + shortestView + FOOTER + footer + MARGIN * 2;
    final double zoom =
        Math.min(
            1.0, Math.min((double) screenWidth / leastWidth, (double) screenHeight / leastHeight));
    final int roomWidth = (int) Math.floor(screenWidth / zoom + ROUNDING);
    final int roomHeight = (int) Math.floor(screenHeight / zoom + ROUNDING);
    final int viewHeight =
        Math.max(
            shortestView,
            Math.min(contentHeight, roomHeight - MARGIN * 2 - footer - HEADER - FOOTER));
    final boolean scrolls = contentHeight > viewHeight;
    final int width = panelWidth(contentWidth, scrolls, headerWidth);
    final int height = HEADER + viewHeight + FOOTER;
    final int left = Math.max(0, (roomWidth - width) / 2);
    return new SkillLayout(
        left,
        Math.max(0, (roomHeight - height - footer) / 2),
        width,
        height,
        left + (width - innerWidth(contentWidth, scrolls)) / 2,
        contentWidth,
        contentHeight,
        viewHeight,
        bandTops,
        zoom);
  }

  static int bandHeight(final int lanes) {
    return BANNER + BANNER_GAP + Math.max(1, lanes) * LANE_STEP - LANE_GAP;
  }

  private static int innerWidth(final int contentWidth, final boolean scrolls) {
    return contentWidth + (scrolls ? SCROLLBAR_GAP + SCROLLBAR : 0);
  }

  private static int panelWidth(
      final int contentWidth, final boolean scrolls, final int headerWidth) {
    return Math.max(
        innerWidth(contentWidth, scrolls) + PAD * 2, Math.max(MIN_WIDTH, headerWidth + PAD * 2));
  }

  public double unzoom(final double screen) {
    return screen / zoom;
  }

  public int screen(final int layout) {
    return (int) Math.round(layout * zoom);
  }

  public boolean scrolls() {
    return contentHeight > viewHeight;
  }

  public int maxScroll() {
    return Math.max(0, contentHeight - viewHeight);
  }

  public int bottom() {
    return top + height;
  }

  public int viewTop() {
    return top + HEADER;
  }

  public int viewBottom() {
    return viewTop() + viewHeight;
  }

  public boolean inView(final double y) {
    return y >= viewTop() && y < viewBottom();
  }

  public int nodeX(final int row) {
    return contentLeft + row * COLUMN_STEP;
  }

  public int nodeY(final int band, final int lane, final int scroll) {
    return bannerY(band, scroll) + BANNER + BANNER_GAP + lane * LANE_STEP;
  }

  public int jointX(final int row) {
    return nodeX(row) - COLUMN_GAP / 2;
  }

  public int bannerY(final int band, final int scroll) {
    return viewTop() + bandTops.get(band) - scroll;
  }

  public int bannerTextY(final int band, final int scroll) {
    return bannerY(band, scroll) + BANNER_TEXT_INSET;
  }

  public int bannerWidth(final int textWidth) {
    return Math.min(contentWidth, textWidth + BANNER_PAD * 2);
  }

  public int scrollbarX() {
    return contentLeft + contentWidth + SCROLLBAR_GAP;
  }

  public int thumbHeight() {
    return Math.max(
        MIN_THUMB, Math.min(viewHeight, viewHeight * viewHeight / Math.max(1, contentHeight)));
  }

  public int thumbY(final int scroll) {
    final int travel = viewHeight - thumbHeight();
    return viewTop()
        + (maxScroll() == 0 ? 0 : (int) Math.round((double) travel * scroll / maxScroll()));
  }

  public double scrollForThumb(final double thumbTop) {
    final int travel = viewHeight - thumbHeight();
    return travel <= 0 ? 0.0 : (thumbTop - viewTop()) * maxScroll() / travel;
  }

  public boolean overScrollbar(final double x, final double y) {
    return scrolls() && x >= scrollbarX() && x < scrollbarX() + SCROLLBAR && inView(y);
  }

  public boolean overThumb(final double x, final double y, final int scroll) {
    return overScrollbar(x, y) && y >= thumbY(scroll) && y < thumbY(scroll) + thumbHeight();
  }

  public int powerBarY() {
    return top + height - PAD - PowerBarRow.SLOT;
  }

  public int pointsY() {
    return top + POINTS_TOP;
  }

  public static boolean contains(
      final int x, final int y, final double mouseX, final double mouseY) {
    return mouseX >= x && mouseX < x + NODE && mouseY >= y && mouseY < y + NODE;
  }
}
