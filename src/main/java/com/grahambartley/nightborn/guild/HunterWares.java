package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.vampire.Vampires;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.jetbrains.annotations.Nullable;

public final class HunterWares implements Merchant, GuildSeller {

  private final HunterEntity hunter;
  private final HunterStock stock = new HunterStock();
  @Nullable private PlayerEntity customer;

  HunterWares(final HunterEntity hunter) {
    this.hunter = hunter;
  }

  public void open(final ServerPlayerEntity visitor) {
    if (!(hunter.getWorld() instanceof ServerWorld world)) {
      return;
    }
    if (customer != null) {
      visitor.sendMessage(
          Text.translatable("nightborn.hunter.busy", hunter.getDisplayName()), true);
      return;
    }
    stock.restock(GuildRestock.day(world.getTimeOfDay()), () -> fresh(world));
    GuildTill.open(
        this,
        visitor,
        hunter.getDisplayName(),
        Text.translatable("nightborn.hunter.nothing", hunter.getDisplayName()));
  }

  void tick() {
    if (customer == null) {
      return;
    }
    if (WaresWatch.closes(
        hunter.isAlive(),
        customer.isAlive() && !customer.isRemoved(),
        Vampires.isVampire(customer),
        hunter.getTarget() != null,
        hunter.squaredDistanceTo(customer))) {
      close();
      return;
    }
    hunter.getNavigation().stop();
    hunter.getLookControl().lookAt(customer);
  }

  void close() {
    final PlayerEntity leaving = customer;
    customer = null;
    if (leaving instanceof ServerPlayerEntity player
        && player.currentScreenHandler instanceof MerchantScreenHandler) {
      player.closeHandledScreen();
    }
  }

  void write(final NbtCompound nbt, final DynamicOps<NbtElement> ops) {
    stock.write(nbt, ops);
  }

  void read(final NbtCompound nbt, final DynamicOps<NbtElement> ops) {
    stock.read(nbt, ops);
  }

  private List<TradeOffer> fresh(final ServerWorld world) {
    final DynamicRegistryManager registries = world.getRegistryManager();
    final TreeProgress progress = SkillAttachment.get(hunter, SkillTree.HUNTER);
    return HunterOffer.forPaths(
            GuildData.hunterOffers(registries),
            progress.lines(),
            OathAttachment.get(hunter).rank(),
            path -> progress.ownedIn(path, skill -> Skills.lineOf(registries, skill)),
            RankLadder.of(registries))
        .stream()
        .map(offer -> offer.offer().toTradeOffer())
        .toList();
  }

  @Override
  public Entity seller() {
    return hunter;
  }

  @Override
  public void setCustomer(@Nullable final PlayerEntity customer) {
    this.customer = customer;
  }

  @Nullable
  @Override
  public PlayerEntity getCustomer() {
    return customer;
  }

  @Override
  public TradeOfferList getOffers() {
    return stock.offers();
  }

  @Override
  public void setOffersFromServer(final TradeOfferList offers) {}

  @Override
  public void trade(final TradeOffer offer) {
    offer.use();
    hunter.playSound(getYesSound(), 1.0F, hunter.getSoundPitch());
  }

  @Override
  public void onSellingItem(final ItemStack stack) {}

  @Override
  public int getExperience() {
    return 0;
  }

  @Override
  public void setExperienceFromServer(final int experience) {}

  @Override
  public boolean isLeveledMerchant() {
    return false;
  }

  @Override
  public SoundEvent getYesSound() {
    return SoundEvents.ENTITY_VILLAGER_YES;
  }

  @Override
  public boolean isClient() {
    return hunter.getWorld().isClient;
  }
}
