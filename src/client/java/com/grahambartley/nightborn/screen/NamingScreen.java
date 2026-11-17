package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.name.NameRules;
import com.grahambartley.nightborn.network.NameServantPayload;
import com.grahambartley.nightborn.skill.SkillTree;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class NamingScreen extends Screen {

  private final int entityId;
  private final String suggestion;
  private SkillTheme theme;
  private NamingLayout layout;
  private TextFieldWidget field;
  private ButtonWidget confirm;

  public NamingScreen(final int entityId, final String suggestion) {
    super(Text.translatable("nightborn.naming.title"));
    this.entityId = entityId;
    this.suggestion = suggestion;
  }

  @Override
  protected void init() {
    theme = SkillTheme.of(SkillTree.VAMPIRE);
    layout = NamingLayout.of(width, height);
    final String typed = field == null ? suggestion : field.getText();
    field =
        new TextFieldWidget(
            textRenderer,
            layout.fieldLeft(),
            layout.fieldTop(),
            layout.fieldWidth(),
            NamingLayout.FIELD_HEIGHT,
            Text.translatable("nightborn.naming.field"));
    field.setMaxLength(NameRules.MAX_SENT_LENGTH);
    field.setText(typed);
    field.setChangedListener(text -> refresh());
    addDrawableChild(field);
    setInitialFocus(field);
    confirm =
        addDrawableChild(
            ButtonWidget.builder(Text.translatable("nightborn.naming.confirm"), button -> send())
                .dimensions(
                    layout.buttonLeft(),
                    layout.buttonTop(),
                    NamingLayout.BUTTON_WIDTH,
                    NamingLayout.BUTTON_HEIGHT)
                .build());
    refresh();
  }

  private void refresh() {
    confirm.active = NameRules.check(field.getText()).ok();
  }

  private void send() {
    final NameRules.Verdict verdict = NameRules.check(field.getText());
    verdict
        .name()
        .ifPresent(
            name -> {
              ClientPlayNetworking.send(new NameServantPayload(entityId, name));
              close();
            });
  }

  @Override
  public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
      send();
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public void renderBackground(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.renderBackground(context, mouseX, mouseY, delta);
    context.drawGuiTexture(
        theme.panel(), layout.left(), layout.top(), layout.width(), layout.height());
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);
    context.drawCenteredTextWithShadow(
        textRenderer, title, layout.centre(), layout.titleTop(), theme.title());
    NameRules.check(field.getText())
        .problem()
        .ifPresentOrElse(
            problem ->
                context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.translatable(problem.translationKey(), NameRules.MAX_LENGTH),
                    layout.centre(),
                    layout.problemTop(),
                    theme.problem()),
            () ->
                context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.translatable("nightborn.naming.hint", NameRules.MAX_LENGTH),
                    layout.centre(),
                    layout.problemTop(),
                    theme.muted()));
  }

  @Override
  public boolean shouldPause() {
    return false;
  }
}
