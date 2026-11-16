package com.grahambartley.nightborn.thrall;

import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.village.TradeOfferList;

public record VillagerMemory(TradeOfferList offers, NbtElement gossip, int experience) {

  public static final VillagerMemory NONE =
      new VillagerMemory(new TradeOfferList(), new NbtList(), 0);

  static final String OFFERS = "Offers";
  static final String GOSSIP = "Gossips";
  static final String EXPERIENCE = "Xp";

  public static VillagerMemory of(final VillagerEntity villager, final DynamicOps<NbtElement> ops) {
    return new VillagerMemory(
        villager.getOffers().copy(), villager.getGossip().serialize(ops), villager.getExperience());
  }

  public void restore(final VillagerEntity villager) {
    villager.setOffers(offers.copy());
    villager.readGossipDataNbt(gossip);
    villager.setExperience(experience);
  }

  public NbtCompound write(final NbtCompound nbt, final DynamicOps<NbtElement> ops) {
    TradeOfferList.CODEC.encodeStart(ops, offers).result().ifPresent(o -> nbt.put(OFFERS, o));
    nbt.put(GOSSIP, gossip.copy());
    nbt.putInt(EXPERIENCE, experience);
    return nbt;
  }

  public static VillagerMemory read(final NbtCompound nbt, final DynamicOps<NbtElement> ops) {
    final TradeOfferList offers =
        Optional.ofNullable(nbt.get(OFFERS))
            .flatMap(saved -> TradeOfferList.CODEC.parse(ops, saved).result())
            .orElseGet(TradeOfferList::new);
    final NbtElement gossip = nbt.contains(GOSSIP) ? nbt.get(GOSSIP).copy() : new NbtList();
    return new VillagerMemory(offers, gossip, nbt.getInt(EXPERIENCE));
  }
}
