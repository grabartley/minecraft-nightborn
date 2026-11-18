package com.grahambartley.nightborn.guild;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public final class ContractLines {

  public record Line(Text text, Formatting tone) {

    public Text toned() {
      return text.copy().formatted(tone);
    }
  }

  private ContractLines() {}

  public static List<Line> of(
      @Nullable final ContractProgress progress,
      @Nullable final RegistryWrapper.WrapperLookup registries) {
    if (progress == null || registries == null) {
      return List.of(new Line(Text.translatable("nightborn.contract.blank"), Formatting.GRAY));
    }
    return GuildData.contract(registries, progress.contract())
        .map(
            contract ->
                List.of(
                    new Line(contract.description(), Formatting.GRAY),
                    contract.finishedAt(progress.done())
                        ? new Line(
                            Text.translatable("nightborn.contract.finished"), Formatting.GOLD)
                        : new Line(
                            Text.translatable(
                                "nightborn.contract.progress", progress.done(), contract.count()),
                            Formatting.DARK_GRAY)))
        .orElseGet(
            () ->
                List.of(new Line(Text.translatable("nightborn.contract.unknown"), Formatting.RED)));
  }

  public static List<List<Line>> inHand(
      final Iterable<ItemStack> carried, final RegistryWrapper.WrapperLookup registries) {
    final List<List<Line>> contracts = new ArrayList<>();
    for (final ItemStack stack : carried) {
      final ContractProgress progress = stack.get(ContractItems.PROGRESS);
      if (progress != null) {
        contracts.add(of(progress, registries));
      }
    }
    return contracts;
  }
}
