package com.grahambartley.nightborn.screen;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

final class SettingsList extends ElementListWidget<SettingsList.Row> {

  private final int rowWidth;

  SettingsList(
      final MinecraftClient client,
      final int width,
      final int height,
      final int top,
      final int rowHeight,
      final int rowWidth) {
    super(client, width, height, top, rowHeight);
    this.rowWidth = rowWidth;
  }

  void addRow(final ClickableWidget widget) {
    addEntry(new Row(widget));
  }

  @Override
  public int getRowWidth() {
    return rowWidth;
  }

  static final class Row extends ElementListWidget.Entry<Row> {

    private final ClickableWidget widget;

    Row(final ClickableWidget widget) {
      this.widget = widget;
    }

    @Override
    public void render(
        final DrawContext context,
        final int index,
        final int y,
        final int x,
        final int entryWidth,
        final int entryHeight,
        final int mouseX,
        final int mouseY,
        final boolean hovered,
        final float tickDelta) {
      widget.setX(x + (entryWidth - widget.getWidth()) / 2);
      widget.setY(y + (entryHeight - widget.getHeight()) / 2);
      widget.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public List<? extends Element> children() {
      return List.of(widget);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
      return List.of(widget);
    }
  }
}
