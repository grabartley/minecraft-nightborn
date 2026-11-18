package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ContractLinesTest {

  private static final Identifier THREE = Identifier.of("nightborn", "stake_three");

  private static RegistryWrapper.WrapperLookup registries() {
    final SimpleRegistry<HunterContract> contracts =
        new SimpleRegistry<>(GuildData.CONTRACT, Lifecycle.stable());
    Registry.register(
        contracts,
        THREE,
        new HunterContract(
            Text.literal("Stake three vampires"),
            ContractGoal.STAKE_VAMPIRES,
            3,
            0,
            RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("nightborn", "contract/test"))));
    contracts.freeze();
    return RegistryWrapper.WrapperLookup.of(Stream.of(contracts.getReadOnlyWrapper()));
  }

  private static String key(final Text text) {
    return text.getContent() instanceof TranslatableTextContent translatable
        ? translatable.getKey()
        : ((PlainTextContent) text.getContent()).string();
  }

  @ParameterizedTest(name = "{0} of 3 done -> {1} in {2}")
  @CsvSource({
    "0, nightborn.contract.progress, DARK_GRAY",
    "2, nightborn.contract.progress, DARK_GRAY",
    "3, nightborn.contract.finished, GOLD"
  })
  @DisplayName("a written contract reads as its task, then its count or that it is finished")
  void written(final int done, final String state, final Formatting tone) {
    final List<ContractLines.Line> lines =
        ContractLines.of(new ContractProgress(THREE, done), registries());
    assertEquals(2, lines.size());
    assertEquals("Stake three vampires", key(lines.get(0).text()));
    assertEquals(Formatting.GRAY, lines.get(0).tone());
    assertEquals(state, key(lines.get(1).text()));
    assertEquals(tone, lines.get(1).tone());
    assertEquals(tone.getColorValue(), lines.get(1).toned().getStyle().getColor().getRgb());
  }

  static Stream<Arguments> unread() {
    return Stream.of(
        Arguments.of(null, true, "nightborn.contract.blank", Formatting.GRAY),
        Arguments.of(
            new ContractProgress(THREE, 0), false, "nightborn.contract.blank", Formatting.GRAY),
        Arguments.of(
            new ContractProgress(Identifier.of("nightborn", "gone"), 0),
            true,
            "nightborn.contract.unknown",
            Formatting.RED));
  }

  @ParameterizedTest(name = "{0}, registries {1} -> {2}")
  @MethodSource("unread")
  @DisplayName("a blank paper, unknown contract or missing registries each read as one line")
  void unreadable(
      final ContractProgress progress,
      final boolean withRegistries,
      final String key,
      final Formatting tone) {
    final List<ContractLines.Line> lines =
        ContractLines.of(progress, withRegistries ? registries() : null);
    assertEquals(1, lines.size());
    assertEquals(key, key(lines.getFirst().text()));
    assertEquals(tone, lines.getFirst().tone());
  }

  @ParameterizedTest(name = "{0} contracts among other items")
  @CsvSource({"0", "1", "3"})
  @DisplayName("the contracts in hand are every carried contract, in order, and nothing else")
  void inHand(final int count) {
    final RegistryWrapper.WrapperLookup registries = registries();
    final List<ItemStack> carried = new java.util.ArrayList<>();
    carried.add(new ItemStack(Items.BREAD));
    for (int done = 0; done < count; done++) {
      final ItemStack contract = new ItemStack(ContractItems.HUNTER_CONTRACT);
      contract.set(ContractItems.PROGRESS, new ContractProgress(THREE, done));
      carried.add(contract);
      carried.add(ItemStack.EMPTY);
    }
    final List<List<ContractLines.Line>> contracts = ContractLines.inHand(carried, registries);
    assertEquals(count, contracts.size());
    for (int done = 0; done < count; done++) {
      assertEquals(
          ContractLines.of(new ContractProgress(THREE, done), registries), contracts.get(done));
    }
  }
}
