package com.grahambartley.nightborn.screen;

import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class ThemedTooltip {

  static final float Z = 400.0F;

  private ThemedTooltip() {}

  public static void draw(
      final DrawContext context,
      final TextRenderer textRenderer,
      final SkillTheme theme,
      final List<Text> lines,
      final int mouseX,
      final int mouseY) {
    if (lines.isEmpty()) {
      return;
    }
    int widest = 0;
    for (final Text line : lines) {
      widest = Math.max(widest, textRenderer.getWidth(line));
    }
    final SkillTooltipBox box =
        SkillTooltipBox.around(
            mouseX,
            mouseY,
            widest,
            lines.size(),
            context.getScaledWindowWidth(),
            context.getScaledWindowHeight());
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, Z);
    context.drawGuiTexture(theme.tooltip(), box.x(), box.y(), box.width(), box.height());
    for (int i = 0; i < lines.size(); i++) {
      context.drawTextWithShadow(
          textRenderer, lines.get(i), box.x() + SkillTooltipBox.PAD, box.lineY(i), theme.text());
    }
    context.getMatrices().pop();
  }
}
