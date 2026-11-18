package com.grahambartley.nightborn.guild;

import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

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
    ContractLines.of(stack.get(ContractItems.PROGRESS), context.getRegistryLookup())
        .forEach(line -> tooltip.add(line.toned()));
  }
}
