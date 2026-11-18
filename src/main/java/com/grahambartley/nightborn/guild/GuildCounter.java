package com.grahambartley.nightborn.guild;

import java.util.Locale;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.jetbrains.annotations.Nullable;

public final class GuildCounter implements Merchant {

  public enum Kind {
    GEAR("minecraft:emerald"),
    CONTRACTS("nightborn:hunter_contract");

    private final Identifier icon;

    Kind(final String icon) {
      this.icon = Identifier.of(icon);
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

    public TradeOfferList from(final TradeOfferList all) {
      final TradeOfferList shown = new TradeOfferList();
      for (final TradeOffer offer : all) {
        if (holds(offer.getSellItem().contains(ContractItems.PROGRESS))) {
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
