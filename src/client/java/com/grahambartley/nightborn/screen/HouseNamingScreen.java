package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.house.crest.Crest;
import com.grahambartley.nightborn.house.crest.CrestBannerItem;
import com.grahambartley.nightborn.house.crest.CrestBanners;
import com.grahambartley.nightborn.name.NameRules;
import com.grahambartley.nightborn.network.NameHousePayload;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.function.IntFunction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.lwjgl.glfw.GLFW;

public final class HouseNamingScreen extends Screen {

  static final int DYES = DyeColor.values().length;

  private final String suggestion;
  private Crest crest;
  private SkillTheme theme;
  private HouseNamingLayout layout;
  private TextFieldWidget field;
  private ButtonWidget confirm;

  public HouseNamingScreen(final String suggestion, final Crest crest) {
    super(Text.translatable("nightborn.house.naming.title"));
    this.suggestion = suggestion;
    this.crest = crest;
  }

  @Override
  protected void init() {
    theme = SkillTheme.of(SkillTree.VAMPIRE);
    layout = HouseNamingLayout.of(width, height);
    final String typed = field == null ? suggestion : field.getText();
    field =
        new TextFieldWidget(
            textRenderer,
            layout.fieldLeft(),
            layout.fieldTop(),
            layout.fieldWidth(),
            HouseNamingLayout.FIELD_HEIGHT,
            Text.translatable("nightborn.house.naming.field"));
    field.setMaxLength(NameRules.MAX_SENT_LENGTH);
    field.setText(typed);
    field.setChangedListener(text -> refresh());
    addDrawableChild(field);
    setInitialFocus(field);
    stepper(0, by -> new Crest(crest.charge().step(by), crest.field(), crest.tincture()));
    stepper(1, by -> new Crest(crest.charge(), dye(crest.field(), by), crest.tincture()));
    stepper(2, by -> new Crest(crest.charge(), crest.field(), dye(crest.tincture(), by)));
    confirm =
        addDrawableChild(
            ButtonWidget.builder(Text.translatable("nightborn.naming.confirm"), button -> send())
                .dimensions(
                    layout.buttonLeft(),
                    layout.buttonTop(),
                    HouseNamingLayout.BUTTON_WIDTH,
                    HouseNamingLayout.BUTTON_HEIGHT)
                .build());
    refresh();
  }

  private void stepper(final int row, final IntFunction<Crest> stepped) {
    addDrawableChild(
        ButtonWidget.builder(Text.literal("<"), button -> crest = stepped.apply(-1))
            .dimensions(
                layout.stepBackLeft(),
                layout.rowTop(row),
                HouseNamingLayout.STEP_WIDTH,
                HouseNamingLayout.ROW_HEIGHT)
            .build());
    addDrawableChild(
        ButtonWidget.builder(Text.literal(">"), button -> crest = stepped.apply(1))
            .dimensions(
                layout.stepOnLeft(),
                layout.rowTop(row),
                HouseNamingLayout.STEP_WIDTH,
                HouseNamingLayout.ROW_HEIGHT)
            .build());
  }

  static DyeColor dye(final DyeColor from, final int by) {
    return DyeColor.byId(Math.floorMod(from.getId() + by, DYES));
  }

  private void refresh() {
    confirm.active = NameRules.check(field.getText()).ok();
  }

  private void send() {
    NameRules.check(field.getText())
        .name()
        .ifPresent(
            name -> {
              ClientPlayNetworking.send(new NameHousePayload(name, crest));
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
    label(context, 0, Text.translatable(crest.charge().translationKey()));
    label(context, 1, Text.translatable(CrestBannerItem.colour(crest.field())));
    label(context, 2, Text.translatable(CrestBannerItem.colour(crest.tincture())));
    preview(context);
  }

  private void label(final DrawContext context, final int row, final Text text) {
    context.drawCenteredTextWithShadow(
        textRenderer,
        text,
        layout.labelCentre(),
        layout.rowTop(row) + (HouseNamingLayout.ROW_HEIGHT - textRenderer.fontHeight) / 2 + 1,
        theme.title());
  }

  private void preview(final DrawContext context) {
    final ItemStack banner = new ItemStack(CrestBanners.ITEM);
    banner.set(CrestBanners.CREST, crest);
    final float scale = HouseNamingLayout.PREVIEW_SIZE / 16.0F;
    context.getMatrices().push();
    context.getMatrices().translate(layout.previewLeft(), layout.previewTop(), 0.0F);
    context.getMatrices().scale(scale, scale, 1.0F);
    context.drawItem(banner, 0, 0);
    context.getMatrices().pop();
  }

  @Override
  public boolean shouldPause() {
    return false;
  }
}
