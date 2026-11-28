package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import java.util.Locale;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.jetbrains.annotations.Nullable;

public final class GuildCounter implements Merchant, GuildSeller {

  public enum Kind {
    GEAR("trade"),
    CONTRACTS("contracts");

    private final Identifier icon;

    Kind(final String icon) {
      this.icon = WheelEntry.icon(icon);
    }

    public boolean openTo(final boolean vampire, final boolean hunter) {
      return this == CONTRACTS ? hunter : !vampire;
    }

    public Identifier icon() {
      return icon;
    }

    public boolean holds(final boolean contract) {
      return (this == CONTRACTS) == contract;
    }

    public String translationKey() {
      return "nightborn.guild." + name().toLowerCase(Locale.ROOT);
    }

    public TradeOfferList from(
        final TradeOfferList all, final List<Integer> needs, final int guildRank) {
      final TradeOfferList shown = new TradeOfferList();
      for (int index = 0; index < all.size(); index++) {
        final TradeOffer offer = all.get(index);
        if (index < needs.size()
            && needs.get(index) <= guildRank
            && holds(offer.getSellItem().contains(ContractItems.PROGRESS))) {
          shown.add(offer);
        }
      }
      return shown;
    }
  }

  private final GuildMasterEntity master;
  private final TradeOfferList offers;

  GuildCounter(final GuildMasterEntity master, final TradeOfferList offers) {
    this.master = master;
    this.offers = offers;
  }

  @Override
  public Entity seller() {
    return master;
  }

  @Override
  public void setCustomer(@Nullable final PlayerEntity customer) {
    master.setCustomer(customer);
  }

  @Nullable
  @Override
  public PlayerEntity getCustomer() {
    return master.getCustomer();
  }

  @Override
  public TradeOfferList getOffers() {
    return offers;
  }

  @Override
  public void setOffersFromServer(final TradeOfferList offers) {}

  @Override
  public void trade(final TradeOffer offer) {
    master.trade(offer);
  }

  @Override
  public void onSellingItem(final ItemStack stack) {
    master.onSellingItem(stack);
  }

  @Override
  public int getExperience() {
    return master.getExperience();
  }

  @Override
  public void setExperienceFromServer(final int experience) {}

  @Override
  public boolean isLeveledMerchant() {
    return master.isLeveledMerchant();
  }

  @Override
  public SoundEvent getYesSound() {
    return master.getYesSound();
  }

  @Override
  public boolean isClient() {
    return master.getWorld().isClient;
  }
}
