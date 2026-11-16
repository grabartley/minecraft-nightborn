package com.grahambartley.nightborn.guild;

import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HunterContractItem extends Item {

  public HunterContractItem(final Settings settings) {
    super(settings);
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    final ContractProgress progress = stack.get(ContractItems.PROGRESS);
    final RegistryWrapper.WrapperLookup registries = context.getRegistryLookup();
    if (progress == null || registries == null) {
      tooltip.add(Text.translatable("nightborn.contract.blank").formatted(Formatting.GRAY));
      return;
    }
    GuildData.contract(registries, progress.contract())
        .ifPresentOrElse(
            contract -> {
              tooltip.add(contract.description().copy().formatted(Formatting.GRAY));
              tooltip.add(
                  contract.finishedAt(progress.done())
                      ? Text.translatable("nightborn.contract.finished").formatted(Formatting.GOLD)
                      : Text.translatable(
                              "nightborn.contract.progress", progress.done(), contract.count())
                          .formatted(Formatting.DARK_GRAY));
            },
            () ->
                tooltip.add(
                    Text.translatable("nightborn.contract.unknown").formatted(Formatting.RED)));
  }
}
