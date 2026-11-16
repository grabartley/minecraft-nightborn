package com.grahambartley.nightborn.tools.structures.camp;

import com.grahambartley.nightborn.tools.structures.template.Template;

public record CampTent(
    int ridgeFrom, int ridgeTo, int centre, int halfWidth, boolean ridgeAlongX, String canvas) {

  public void build(final Template template) {
    final int back = ridgeFrom;
    for (int along = ridgeFrom; along <= ridgeTo; along++) {
      for (int layer = 0; layer <= halfWidth; layer++) {
        final int y = layer + 1;
        final int reach = halfWidth - layer;
        set(template, along, centre - reach, y, canvas);
        set(template, along, centre + reach, y, canvas);
        for (int across = centre - reach + 1; across < centre + reach; across++) {
          set(template, along, across, y, along == back ? canvas : "air");
        }
      }
    }
  }

  private void set(
      final Template template, final int along, final int across, final int y, final String state) {
    if (ridgeAlongX) {
      template.set(along, y, across, state);
    } else {
      template.set(across, y, along, state);
    }
  }
}
