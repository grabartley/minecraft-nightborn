package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.faction.Faction;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class WornStores {

  static final String SIPPED = "nightborn.accessory.sipped";

  private WornStores() {}

  public static boolean drinkWhenDry(final ServerPlayerEntity vampire) {
    for (final ItemStack store : stores(vampire)) {
      final Optional<ItemStack> held =
          drinkable(AccessoryStore.contents(store)) ? AccessoryStore.take(store) : Optional.empty();
      if (held.isPresent()) {
        drink(vampire, store, held.get());
        return true;
      }
    }
    return false;
  }

  public static boolean drinkable(final BundleContentsComponent contents) {
    return !contents.isEmpty() && contents.get(0).getItem() instanceof BloodBottleItem;
  }

  private static List<ItemStack> stores(final ServerPlayerEntity vampire) {
    final List<ItemStack> stores = new ArrayList<>();
    TrinketsApi.getTrinketComponent(vampire)
        .ifPresent(
            component ->
                component.forEach(
                    (slot, stack) ->
                        Accessories.of(vampire.getRegistryManager(), stack)
                            .filter(accessory -> accessory.holds().isPresent())
                            .filter(accessory -> accessory.worksFor(Faction.VAMPIRE))
                            .ifPresent(accessory -> stores.add(stack))));
    return stores;
  }

  private static void drink(
      final ServerPlayerEntity vampire, final ItemStack store, final ItemStack bottle) {
    final ItemStack left = bottle.finishUsing(vampire.getServerWorld(), vampire);
    if (!left.isEmpty() && !vampire.getInventory().insertStack(left)) {
      vampire.dropItem(left, false);
    }
    vampire.sendMessage(Text.translatable(SIPPED, store.getName()), true);
  }
}
