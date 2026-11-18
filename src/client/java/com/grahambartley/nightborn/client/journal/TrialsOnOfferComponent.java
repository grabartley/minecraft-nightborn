package com.grahambartley.nightborn.client.journal;

import com.grahambartley.nightborn.challenge.ChallengeAttachment;
import com.grahambartley.nightborn.challenge.ChallengeData;
import com.grahambartley.nightborn.challenge.TrialLines;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

public final class TrialsOnOfferComponent implements ICustomComponent {

  private transient int x;
  private transient int y;

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
    final List<TrialLines.Line> lines =
        TrialLines.of(
            ChallengeAttachment.get(player).offer(),
            ChallengeData.trials(player.getRegistryManager()));
    int top =
        JournalPageText.title(graphics, font, "nightborn.journal.trials.title", x, y, context);
    if (lines.isEmpty()) {
      JournalPageText.wrapped(
          graphics,
          font,
          Text.translatable("nightborn.journal.trials.none"),
          x,
          top,
          context.getTextColor(),
          context);
      return;
    }
    for (final TrialLines.Line line : lines) {
      top =
          JournalPageText.wrapped(
              graphics, font, line.status(), x, top, context.getHeaderColor(), context);
      top =
          JournalPageText.wrapped(
              graphics, font, line.description(), x, top, context.getTextColor(), context);
      top += JournalPageText.GAP;
      if (top >= JournalPageText.BOTTOM) {
        return;
      }
    }
  }

  @Override
  public void onVariablesAvailable(
      final UnaryOperator<IVariable> lookup, final RegistryWrapper.WrapperLookup registries) {}
}
