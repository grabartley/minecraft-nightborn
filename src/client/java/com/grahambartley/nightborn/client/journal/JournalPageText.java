package com.grahambartley.nightborn.client.journal;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import vazkii.patchouli.api.IComponentRenderContext;

final class JournalPageText {

  static final int WIDTH = 116;
  static final int BOTTOM = 156;
  static final int GAP = 4;
  static final int TITLE_GAP = 14;

  private JournalPageText() {}

  static int title(
      final DrawContext graphics,
      final TextRenderer font,
      final String key,
      final int x,
      final int y,
      final IComponentRenderContext context) {
    final Text title = Text.translatable(key).setStyle(context.getFont());
    graphics.drawText(
        font, title, x + (WIDTH - font.getWidth(title)) / 2, y, context.getHeaderColor(), false);
    return y + TITLE_GAP;
  }

  static int wrapped(
      final DrawContext graphics,
      final TextRenderer font,
      final Text text,
      final int x,
      final int top,
      final int color,
      final IComponentRenderContext context) {
    int line = top;
    for (final OrderedText row : font.wrapLines(text.copy().setStyle(context.getFont()), WIDTH)) {
      if (line + font.fontHeight > BOTTOM) {
        return BOTTOM;
      }
      graphics.drawText(font, row, x, line, color, false);
      line += font.fontHeight;
    }
    return line;
  }
}
