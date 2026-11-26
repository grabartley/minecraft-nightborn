package com.grahambartley.nightborn.screen;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
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
    final int wrap = SkillTooltipBox.wrapWidth(context.getScaledWindowWidth());
    final List<OrderedText> title = textRenderer.wrapLines(lines.get(0), wrap);
    final List<OrderedText> wrapped =
        Stream.concat(
                title.stream(),
                lines.stream().skip(1).flatMap(line -> textRenderer.wrapLines(line, wrap).stream()))
            .toList();
    final SkillTooltipBox box =
        SkillTooltipBox.around(
            mouseX,
            mouseY,
            wrapped.stream().map(textRenderer::getWidth).toList(),
            lines.size() > 1 ? title.size() : 0,
            context.getScaledWindowWidth(),
            context.getScaledWindowHeight());
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, Z);
    context.drawGuiTexture(theme.tooltip(), box.x(), box.y(), box.width(), box.height());
    for (int i = 0; i < wrapped.size(); i++) {
      context.drawTextWithShadow(
          textRenderer, wrapped.get(i), box.x() + SkillTooltipBox.PAD, box.lineY(i), theme.text());
    }
    context.getMatrices().pop();
  }
}
