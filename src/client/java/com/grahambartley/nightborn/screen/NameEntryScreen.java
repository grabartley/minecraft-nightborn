package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.name.NameRules;
import com.grahambartley.nightborn.skill.SkillTree;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

abstract class NameEntryScreen<L extends NameEntryBox> extends Screen {

  private final String suggestion;
  private final Text fieldLabel;
  private SkillTheme theme;
  private L layout;
  private TextFieldWidget field;
  private ButtonWidget confirm;

  protected NameEntryScreen(final Text title, final Text fieldLabel, final String suggestion) {
    super(title);
    this.fieldLabel = fieldLabel;
    this.suggestion = suggestion;
  }

  protected abstract L layout(int screenWidth, int screenHeight);

  protected final L box() {
    return layout;
  }

  protected final SkillTheme theme() {
    return theme;
  }

  protected abstract void submit(String name);

  protected void addChoices() {}

  protected void drawChoices(final DrawContext context) {}

  @Override
  protected void init() {
    theme = SkillTheme.of(SkillTree.VAMPIRE);
    layout = layout(width, height);
    final String typed = field == null ? suggestion : field.getText();
    field =
        new TextFieldWidget(
            textRenderer,
            layout.fieldLeft(),
            layout.fieldTop(),
            layout.fieldWidth(),
            NameEntryBox.FIELD_HEIGHT,
            fieldLabel);
    field.setMaxLength(NameRules.MAX_SENT_LENGTH);
    field.setText(typed);
    field.setChangedListener(text -> refresh());
    addDrawableChild(field);
    setInitialFocus(field);
    addChoices();
    confirm =
        addDrawableChild(
            ButtonWidget.builder(Text.translatable("nightborn.naming.confirm"), button -> send())
                .dimensions(
                    layout.buttonLeft(),
                    layout.buttonTop(),
                    NameEntryBox.BUTTON_WIDTH,
                    NameEntryBox.BUTTON_HEIGHT)
                .build());
    refresh();
  }

  private void refresh() {
    confirm.active = NameRules.check(field.getText()).ok();
  }

  private void send() {
    NameRules.check(field.getText())
        .name()
        .ifPresent(
            name -> {
              submit(name);
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
    drawChoices(context);
  }

  @Override
  public boolean shouldPause() {
    return false;
  }
}
