package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.vampire.Vampires;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class WornStores {

  public record WornStore(ItemStack stack, List<AccessoryHold> holds) {}

  static final String SIPPED = "nightborn.accessory.sipped";

  private WornStores() {}

  public static boolean drinkWhenDry(final ServerPlayerEntity vampire) {
    for (final WornStore worn : worn(vampire)) {
      final ItemStack store = worn.stack();
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

  public static List<WornStore> worn(final LivingEntity wearer) {
    final Faction faction = Vampires.isVampire(wearer) ? Faction.VAMPIRE : Faction.HUMAN;
    final List<WornStore> stores = new ArrayList<>();
    TrinketsApi.getTrinketComponent(wearer)
        .ifPresent(
            component ->
                component.forEach(
                    (slot, stack) ->
                        Accessories.of(wearer.getWorld().getRegistryManager(), stack)
                            .filter(accessory -> !accessory.holds().isEmpty())
                            .filter(accessory -> accessory.worksFor(faction))
                            .ifPresent(
                                accessory -> stores.add(new WornStore(stack, accessory.holds())))));
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
