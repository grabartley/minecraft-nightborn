package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterContractItemTest {

  @ParameterizedTest(name = "written: {0}")
  @ValueSource(booleans = {false, true})
  @DisplayName("without registries a contract's tooltip is one grey line, written or not")
  void tooltipWithoutRegistries(final boolean written) {
    final ItemStack stack = new ItemStack(ContractItems.HUNTER_CONTRACT);
    if (written) {
      stack.set(ContractItems.PROGRESS, new ContractProgress(Identifier.of("nightborn", "any"), 1));
    }
    final List<Text> tooltip = new ArrayList<>();
    ContractItems.HUNTER_CONTRACT.appendTooltip(
        stack, Item.TooltipContext.DEFAULT, tooltip, TooltipType.BASIC);
    assertEquals(1, tooltip.size());
    assertEquals(
        "nightborn.contract.blank",
        ((TranslatableTextContent) tooltip.getFirst().getContent()).getKey());
    assertEquals(
        Formatting.GRAY.getColorValue(), tooltip.getFirst().getStyle().getColor().getRgb());
  }
}
