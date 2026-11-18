package com.grahambartley.nightborn.client.journal;

import com.google.common.collect.Iterables;
import com.grahambartley.nightborn.guild.ContractLines;
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

public final class ContractsInHandComponent implements ICustomComponent {

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
    final List<List<ContractLines.Line>> contracts =
        ContractLines.inHand(
            Iterables.concat(player.getInventory().main, player.getInventory().offHand),
            player.getRegistryManager());
    int top =
        JournalPageText.title(graphics, font, "nightborn.journal.contracts.title", x, y, context);
    if (contracts.isEmpty()) {
      draw(graphics, font, Text.translatable("nightborn.journal.contracts.none"), top, context);
      return;
    }
    for (final List<ContractLines.Line> contract : contracts) {
      for (final ContractLines.Line line : contract) {
        top = draw(graphics, font, line.text(), top, context);
      }
      top += JournalPageText.GAP;
      if (top >= JournalPageText.BOTTOM) {
        return;
      }
    }
  }

  private int draw(
      final DrawContext graphics,
      final TextRenderer font,
      final Text text,
      final int top,
      final IComponentRenderContext context) {
    return JournalPageText.wrapped(graphics, font, text, x, top, context.getTextColor(), context);
  }

  @Override
  public void onVariablesAvailable(
      final UnaryOperator<IVariable> lookup, final RegistryWrapper.WrapperLookup registries) {}
}
