package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.network.ChooseWheelPayload;
import com.grahambartley.nightborn.network.OpenWheelPayload;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.wheel.WheelEntry;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class CommandWheelScreen extends Screen {

  static final int BACKDROP = 0x50000000;
  static final int FRAME = 22;
  static final int ICON = 16;
  static final int CAPTION_GAP = 8;
  static final int CAPTION_PAD = 6;
  static final float STEP_DEGREES = 2.5F;

  private final int entityId;
  private final SkillTheme theme;
  private final List<WheelEntry> entries;
  private int hovered = -1;

  public CommandWheelScreen(final OpenWheelPayload payload) {
    super(payload.title());
    this.entityId = payload.entityId();
    this.theme = SkillTheme.of(payload.theme());
    this.entries = payload.entries();
  }

  @Override
  public boolean shouldPause() {
    return false;
  }

  @Override
  public void renderBackground(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    context.fill(0, 0, width, height, BACKDROP);
  }

  @Override
  public void tick() {
    if (client != null && (client.world == null || client.world.getEntityById(entityId) == null)) {
      close();
    }
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);
    hovered = WheelRing.sectorAt(mouseX - width / 2.0, mouseY - height / 2.0, entries.size());
    drawRing(context);
    drawIcons(context);
    drawCaption(context);
  }

  private void drawRing(final DrawContext context) {
    if (client == null) {
      return;
    }
    final float scale = (float) client.getWindow().getScaleFactor();
    context.getMatrices().push();
    context.getMatrices().scale(1.0F / scale, 1.0F / scale, 1.0F);
    final Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.disableCull();
    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    final BufferBuilder buffer =
        Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
    for (int index = 0; index < entries.size(); index++) {
      sector(
          buffer,
          matrix,
          width / 2.0F * scale,
          height / 2.0F * scale,
          WheelRing.INNER * scale,
          WheelRing.OUTER * scale,
          WheelRing.startDegrees(index, entries.size()),
          WheelRing.endDegrees(index, entries.size()),
          index == hovered ? theme.ringHover() : theme.ring());
    }
    BufferRenderer.drawWithGlobalProgram(buffer.end());
    RenderSystem.enableCull();
    RenderSystem.disableBlend();
    context.getMatrices().pop();
  }

  private static void sector(
      final BufferBuilder buffer,
      final Matrix4f matrix,
      final float centreX,
      final float centreY,
      final float inner,
      final float outer,
      final float startDegrees,
      final float endDegrees,
      final int argb) {
    final int steps = Math.max(1, MathHelper.ceil((endDegrees - startDegrees) / STEP_DEGREES));
    for (int step = 0; step < steps; step++) {
      final float from =
          (float) Math.toRadians(startDegrees + (endDegrees - startDegrees) * step / steps);
      final float to =
          (float) Math.toRadians(startDegrees + (endDegrees - startDegrees) * (step + 1) / steps);
      final float cosFrom = MathHelper.cos(from);
      final float sinFrom = MathHelper.sin(from);
      final float cosTo = MathHelper.cos(to);
      final float sinTo = MathHelper.sin(to);
      buffer.vertex(matrix, centreX + outer * cosFrom, centreY + outer * sinFrom, 0).color(argb);
      buffer.vertex(matrix, centreX + inner * cosFrom, centreY + inner * sinFrom, 0).color(argb);
      buffer.vertex(matrix, centreX + inner * cosTo, centreY + inner * sinTo, 0).color(argb);
      buffer.vertex(matrix, centreX + outer * cosTo, centreY + outer * sinTo, 0).color(argb);
    }
  }

  private void drawIcons(final DrawContext context) {
    for (int index = 0; index < entries.size(); index++) {
      final WheelEntry entry = entries.get(index);
      final int x = width / 2 + WheelRing.iconX(index, entries.size());
      final int y = height / 2 + WheelRing.iconY(index, entries.size());
      context.drawGuiTexture(
          theme.frame(
              entry.current() ? SkillPurchase.Verdict.OWNED : SkillPurchase.Verdict.ALLOWED),
          x - FRAME / 2,
          y - FRAME / 2,
          FRAME,
          FRAME);
      context.drawItem(
          new ItemStack(Registries.ITEM.get(entry.icon())), x - ICON / 2, y - ICON / 2);
    }
  }

  private void drawCaption(final DrawContext context) {
    final Text heading = title;
    final Text line =
        hovered >= 0
            ? entries.get(hovered).label()
            : entries.stream()
                .filter(WheelEntry::current)
                .findFirst()
                .map(WheelEntry::label)
                .orElse(Text.empty());
    final int lineHeight = textRenderer.fontHeight + 2;
    final int boxWidth =
        Math.max(textRenderer.getWidth(heading), textRenderer.getWidth(line)) + CAPTION_PAD * 2;
    final int boxHeight = lineHeight * 2 + CAPTION_PAD * 2 - 2;
    final int left = width / 2 - boxWidth / 2;
    final int top = height / 2 + WheelRing.OUTER + CAPTION_GAP;
    context.drawGuiTexture(theme.tooltip(), left, top, boxWidth, boxHeight);
    context.drawCenteredTextWithShadow(
        textRenderer, heading, width / 2, top + CAPTION_PAD, theme.title());
    context.drawCenteredTextWithShadow(
        textRenderer,
        line,
        width / 2,
        top + CAPTION_PAD + lineHeight,
        hovered >= 0 ? theme.text() : theme.muted());
  }

  @Override
  public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
    if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && choose(sectorUnder(mouseX, mouseY))) {
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
      return super.mouseClicked(mouseX, mouseY, button);
    }
    if (!choose(sectorUnder(mouseX, mouseY))) {
      close();
    }
    return true;
  }

  @Override
  public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
    final int index = WheelRing.numberKey(keyCode - GLFW.GLFW_KEY_1, entries.size());
    if (choose(index)) {
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  private int sectorUnder(final double mouseX, final double mouseY) {
    return WheelRing.sectorAt(mouseX - width / 2.0, mouseY - height / 2.0, entries.size());
  }

  private boolean choose(final int index) {
    if (index < 0 || index >= entries.size()) {
      return false;
    }
    final WheelEntry entry = entries.get(index);
    ClientPlayNetworking.send(new ChooseWheelPayload(entityId, entry.action(), entry.argument()));
    if (client != null) {
      client
          .getSoundManager()
          .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
    close();
    return true;
  }
}
