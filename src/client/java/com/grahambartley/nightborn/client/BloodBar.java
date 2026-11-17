package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class BloodBar {

  static final Identifier FULL = Identifier.of(Nightborn.MOD_ID, "hud/blood_full");
  static final Identifier HALF = Identifier.of(Nightborn.MOD_ID, "hud/blood_half");
  static final Identifier EMPTY = Identifier.of(Nightborn.MOD_ID, "hud/blood_empty");
  static final int ICONS = 10;
  static final int ICON_SIZE = 9;
  static final int STEP = 8;
  static final int ROW_STEP = 10;

  private BloodBar() {}

  public static void draw(
      final DrawContext context,
      final int blood,
      final int maxBlood,
      final int top,
      final int right) {
    for (int i = 0; i < BloodBarIcons.count(maxBlood); i++) {
      final int x = right - BloodBarIcons.column(i) * STEP - ICON_SIZE;
      final int y = top - BloodBarIcons.row(i) * ROW_STEP;
      context.drawGuiTexture(EMPTY, x, y, ICON_SIZE, ICON_SIZE);
      final Identifier fill = BloodBarIcons.fill(i, blood);
      if (fill != null) {
        context.drawGuiTexture(fill, x, y, ICON_SIZE, ICON_SIZE);
      }
    }
  }
}
