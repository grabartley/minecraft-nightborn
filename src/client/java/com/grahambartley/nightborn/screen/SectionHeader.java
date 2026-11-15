package com.grahambartley.nightborn.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

final class SectionHeader extends ClickableWidget {

  private static final int COLOUR = 0xC9A24A;

  private final TextRenderer textRenderer;

  SectionHeader(
      final int width, final int height, final Text message, final TextRenderer textRenderer) {
    super(0, 0, width, height, message);
    this.textRenderer = textRenderer;
    this.active = false;
  }

  @Override
  protected void renderWidget(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    context.drawCenteredTextWithShadow(
        textRenderer, getMessage(), getX() + getWidth() / 2, getY() + 2, COLOUR);
  }

  @Override
  protected void appendClickableNarrations(final NarrationMessageBuilder builder) {}
}
