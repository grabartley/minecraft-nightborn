package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class ContractItems {

  public static final ComponentType<ContractProgress> PROGRESS =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(Nightborn.MOD_ID, "hunter_contract"),
          ComponentType.<ContractProgress>builder()
              .codec(ContractProgress.CODEC)
              .packetCodec(ContractProgress.PACKET_CODEC.cast())
              .build());

  public static final Item HUNTER_CONTRACT =
      Registry.register(
          Registries.ITEM,
          Identifier.of(Nightborn.MOD_ID, "hunter_contract"),
          new HunterContractItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));

  private ContractItems() {}

  static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(entries -> entries.add(HUNTER_CONTRACT));
  }

  public static Optional<ContractCredit.Held> held(
      final ItemStack stack, final RegistryWrapper.WrapperLookup registries) {
    final ContractProgress progress = stack.get(PROGRESS);
    if (progress == null) {
      return Optional.empty();
    }
    return GuildData.contract(registries, progress.contract())
        .map(contract -> new ContractCredit.Held(contract, progress.done()));
  }

  public static Optional<HunterContract> finished(
      final ItemStack stack, final RegistryWrapper.WrapperLookup registries) {
    return held(stack, registries)
        .filter(held -> held.contract().finishedAt(held.done()))
        .map(ContractCredit.Held::contract);
  }
}
