package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.client.SyncedServerConfig;
import com.grahambartley.nightborn.client.gore.SplatterLook;
import com.grahambartley.nightborn.client.state.ClientState;
import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.client.state.GoreLevel;
import com.grahambartley.nightborn.config.BoolSetting;
import com.grahambartley.nightborn.config.IntSetting;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.config.Setting;
import com.grahambartley.nightborn.config.SettingValues;
import com.grahambartley.nightborn.network.EditConfigPayload;
import com.grahambartley.nightborn.server.ConfigEditPermission;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class NightbornSettingsScreen extends Screen {

  static final int ROW_HEIGHT = 24;
  static final int CONTENT_WIDTH = 240;
  static final int WIDGET_WIDTH = 220;
  static final int WIDGET_HEIGHT = 20;
  static final int HEADER_HEIGHT = 12;
  static final int LIST_TOP = 40;
  static final int FOOTER = 32;
  static final int BUTTON_WIDTH = 100;
  static final int BUTTON_GAP = 6;
  static final int BANNER_COLOUR = 0xC9A24A;

  private final Screen parent;
  private final NightbornConfig opened;
  private SettingValues values;
  private ClientState device;

  public NightbornSettingsScreen(@Nullable final Screen parent) {
    super(Text.translatable("nightborn.screen.settings.title"));
    this.parent = parent;
    this.opened =
        access() == EditAccess.NO_WORLD ? NightbornConfig.DEFAULTS : SyncedServerConfig.get();
    this.values = opened.values();
    this.device = ClientStateService.current();
  }

  @Override
  protected void init() {
    final boolean editable = access() == EditAccess.EDITABLE;
    final SettingsList list =
        new SettingsList(
            client, width, height - LIST_TOP - FOOTER, LIST_TOP, ROW_HEIGHT, CONTENT_WIDTH);
    addDeviceSection(list);
    String section = "";
    for (final Setting<?> setting : NightbornConfig.SETTINGS) {
      if (!setting.section().equals(section)) {
        section = setting.section();
        list.addRow(
            new SectionHeader(
                WIDGET_WIDTH,
                HEADER_HEIGHT,
                Text.translatable(setting.sectionKey()),
                textRenderer));
      }
      final ClickableWidget widget = widgetFor(setting);
      widget.setTooltip(Tooltip.of(Text.translatable(setting.tooltipKey())));
      widget.active = editable;
      list.addRow(widget);
    }
    addDrawableChild(list);
    final int left = (width - BUTTON_WIDTH * 2 - BUTTON_GAP) / 2;
    final int y = height - FOOTER + (FOOTER - WIDGET_HEIGHT) / 2;
    final ButtonWidget done =
        ButtonWidget.builder(Text.translatable("gui.done"), button -> saveAndClose())
            .dimensions(left, y, BUTTON_WIDTH, WIDGET_HEIGHT)
            .build();
    addDrawableChild(done);
    addDrawableChild(
        ButtonWidget.builder(Text.translatable("gui.cancel"), button -> close())
            .dimensions(left + BUTTON_WIDTH + BUTTON_GAP, y, BUTTON_WIDTH, WIDGET_HEIGHT)
            .build());
  }

  private void addDeviceSection(final SettingsList list) {
    list.addRow(
        new SectionHeader(
            WIDGET_WIDTH,
            HEADER_HEIGHT,
            Text.translatable("nightborn.section.device"),
            textRenderer));
    final CyclingButtonWidget<GoreLevel> gore =
        CyclingButtonWidget.<GoreLevel>builder(level -> Text.translatable(level.translationKey()))
            .values(GoreLevel.values())
            .initially(device.gore())
            .build(
                0,
                0,
                WIDGET_WIDTH,
                WIDGET_HEIGHT,
                Text.translatable("nightborn.client.gore"),
                (button, level) -> device = device.withGore(level));
    gore.setTooltip(Tooltip.of(Text.translatable("nightborn.client.gore.tooltip")));
    list.addRow(gore);
  }

  private ClickableWidget widgetFor(final Setting<?> setting) {
    return switch (setting) {
      case IntSetting number ->
          new IntSettingSlider(
              WIDGET_WIDTH,
              WIDGET_HEIGHT,
              number,
              values.get(number),
              value -> values = values.with(number, value));
      case BoolSetting flag ->
          CyclingButtonWidget.onOffBuilder(values.get(flag))
              .build(
                  0,
                  0,
                  WIDGET_WIDTH,
                  WIDGET_HEIGHT,
                  Text.translatable(flag.translationKey()),
                  (button, value) -> values = values.with(flag, value));
    };
  }

  private void saveAndClose() {
    final GoreLevel goreBefore = ClientStateService.current().gore();
    if (ClientStateService.update(device)
        && SplatterLook.redrawNeeded(goreBefore, device.gore())
        && client != null) {
      client.worldRenderer.reload();
    }
    if (access() == EditAccess.EDITABLE) {
      ClientPlayNetworking.send(new EditConfigPayload(opened, NightbornConfig.from(values)));
    }
    close();
  }

  @Override
  public void close() {
    MinecraftClient.getInstance().setScreen(parent);
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);
    context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);
    final EditAccess access = access();
    if (access != EditAccess.EDITABLE) {
      context.drawCenteredTextWithShadow(
          textRenderer, Text.translatable(access.bannerKey()), width / 2, 25, BANNER_COLOUR);
    }
  }

  private static EditAccess access() {
    final MinecraftClient client = MinecraftClient.getInstance();
    final ClientPlayerEntity player = client.player;
    return EditAccess.resolve(
        player != null, player != null && ConfigEditPermission.canEdit(player, client.getServer()));
  }
}
