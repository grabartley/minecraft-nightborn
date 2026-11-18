package com.grahambartley.nightborn.client.journal;

import com.grahambartley.nightborn.challenge.ChallengeAttachment;
import com.grahambartley.nightborn.challenge.ChallengeData;
import com.grahambartley.nightborn.challenge.FeatLines;
import com.grahambartley.nightborn.network.ChooseTitlePayload;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

public final class MasteryFeatsComponent implements ICustomComponent {

  record Row(FeatLines.Line line, int top, int bottom) {}

  private transient int x;
  private transient int y;
  private transient List<Row> rows = List.of();

  @Override
  public void build(final int componentX, final int componentY, final int pageNum) {
    this.x = componentX;
    this.y = componentY;
  }

  @Override
  public void render(
      final DrawContext graphics,
      final IComponentRenderContext context,
      final float pticks,
      final int mouseX,
      final int mouseY) {
    final MinecraftClient client = MinecraftClient.getInstance();
    final ClientPlayerEntity player = client.player;
    if (player == null) {
      return;
    }
    final TextRenderer font = client.textRenderer;
    final List<FeatLines.Line> lines =
        FeatLines.of(
            ChallengeAttachment.get(player).feats(),
            ChallengeData.feats(player.getRegistryManager()));
    int top = JournalPageText.title(graphics, font, "nightborn.journal.feats.title", x, y, context);
    final List<Row> drawn = new ArrayList<>();
    if (lines.isEmpty()) {
      JournalPageText.wrapped(
          graphics,
          font,
          Text.translatable("nightborn.journal.feats.none"),
          x,
          top,
          context.getTextColor(),
          context);
      rows = List.of();
      return;
    }
    top =
        JournalPageText.wrapped(
            graphics,
            font,
            Text.translatable("nightborn.journal.feats.hint"),
            x,
            top,
            context.getTextColor(),
            context);
    top += JournalPageText.GAP;
    for (final FeatLines.Line line : lines) {
      final int start = top;
      top =
          JournalPageText.wrapped(
              graphics, font, line.name(), x, top, context.getTextColor(), context);
      top =
          JournalPageText.wrapped(
              graphics, font, line.shownTitle(), x, top, context.getHeaderColor(), context);
      drawn.add(new Row(line, start, top));
      top += JournalPageText.GAP;
      if (top >= JournalPageText.BOTTOM) {
        break;
      }
    }
    rows = List.copyOf(drawn);
  }

  @Override
  public boolean mouseClicked(
      final IComponentRenderContext context,
      final double mouseX,
      final double mouseY,
      final int mouseButton) {
    if (mouseX < x || mouseX > x + JournalPageText.WIDTH) {
      return false;
    }
    for (final Row row : rows) {
      if (mouseY >= row.top() && mouseY < row.bottom()) {
        ClientPlayNetworking.send(new ChooseTitlePayload(row.line().choiceOnClick()));
        MinecraftClient.getInstance().player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        return true;
      }
    }
    return false;
  }

  @Override
  public void onVariablesAvailable(
      final UnaryOperator<IVariable> lookup, final RegistryWrapper.WrapperLookup registries) {}
}
