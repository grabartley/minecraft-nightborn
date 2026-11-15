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

  private BloodBar() {}

  public static void draw(
      final DrawContext context, final int blood, final int top, final int right) {
    for (int i = 0; i < ICONS; i++) {
      final int x = right - i * STEP - ICON_SIZE;
      context.drawGuiTexture(EMPTY, x, top, ICON_SIZE, ICON_SIZE);
      final Identifier fill = BloodBarIcons.fill(i, blood);
      if (fill != null) {
        context.drawGuiTexture(fill, x, top, ICON_SIZE, ICON_SIZE);
      }
    }
  }
}
