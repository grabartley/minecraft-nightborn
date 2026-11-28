package com.grahambartley.nightborn.guild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradedItem;

public final class HunterOfferCheck {

  private HunterOfferCheck() {}

  public static List<String> problems(
      final Map<Identifier, HunterOffer> offers,
      final Set<Identifier> paths,
      final Set<Identifier> ranks,
      final List<GuildOffer> master,
      final Predicate<ItemStack> contract) {
    final List<String> problems = new ArrayList<>();
    new TreeMap<>(offers)
        .forEach(
            (id, offer) -> {
              if (!paths.contains(offer.path())) {
                problems.add(id + " names unknown path " + offer.path());
              }
              offer
                  .hunterRank()
                  .filter(rank -> !ranks.contains(rank))
                  .ifPresent(rank -> problems.add(id + " names unknown hunter rank " + rank));
              if (offer.offer().rank().isPresent()) {
                problems.add(id + " asks a guild rank, which only the Guild Master may ask");
              }
              if (contract.test(offer.offer().gives())) {
                problems.add(id + " sells a contract, which only the Guild Master gives");
              }
              master.stream()
                  .filter(kept -> sameGoods(kept.gives(), offer.offer().gives()))
                  .forEach(kept -> problems.addAll(against(id, offer.offer(), kept)));
            });
    return problems;
  }

  static boolean undercuts(final GuildOffer hunter, final GuildOffer master) {
    return wants(master)
        .anyMatch(
            masterWant ->
                wants(hunter)
                    .noneMatch(
                        hunterWant ->
                            hunterWant.item().value() == masterWant.item().value()
                                && (long) hunterWant.count() * master.gives().getCount()
                                    >= (long) masterWant.count() * hunter.gives().getCount()));
  }

  private static List<String> against(
      final Identifier id, final GuildOffer hunter, final GuildOffer master) {
    if (master.rank().isPresent()) {
      return List.of(id + " sells goods the Guild Master keeps for rank");
    }
    return undercuts(hunter, master)
        ? List.of(id + " undercuts the Guild Master on the same goods")
        : List.of();
  }

  private static boolean sameGoods(final ItemStack kept, final ItemStack sold) {
    return ItemStack.areItemsAndComponentsEqual(kept, sold);
  }

  private static Stream<TradedItem> wants(final GuildOffer offer) {
    return Stream.concat(Stream.of(offer.wants()), offer.alsoWants().stream());
  }
}
