package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.house.crest.Crest;
import com.grahambartley.nightborn.house.crest.CrestBannerItem;
import com.grahambartley.nightborn.house.crest.CrestBanners;
import com.grahambartley.nightborn.network.NameHousePayload;
import java.util.function.IntFunction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public final class HouseNamingScreen extends NameEntryScreen<HouseNamingLayout> {

  static final int DYES = DyeColor.values().length;

  private Crest crest;

  public HouseNamingScreen(final String suggestion, final Crest crest) {
    super(
        Text.translatable("nightborn.house.naming.title"),
        Text.translatable("nightborn.house.naming.field"),
        suggestion);
    this.crest = crest;
  }

  @Override
  protected HouseNamingLayout layout(final int screenWidth, final int screenHeight) {
    return HouseNamingLayout.of(screenWidth, screenHeight);
  }

  @Override
  protected void submit(final String name) {
    ClientPlayNetworking.send(new NameHousePayload(name, crest));
  }

  @Override
  protected void addChoices() {
    stepper(0, by -> new Crest(crest.charge().step(by), crest.field(), crest.tincture()));
    stepper(1, by -> new Crest(crest.charge(), dye(crest.field(), by), crest.tincture()));
    stepper(2, by -> new Crest(crest.charge(), crest.field(), dye(crest.tincture(), by)));
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

  @Override
  protected void drawChoices(final DrawContext context) {
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
}
