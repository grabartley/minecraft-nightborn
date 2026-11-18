package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.boss.Fought;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.util.dynamic.Codecs;

public final class PickUnownedLootFunction extends ConditionalLootFunction {

  public static final MapCodec<PickUnownedLootFunction> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              addConditionsField(instance)
                  .and(
                      Codecs.nonEmptyList(Registries.ITEM.getCodec().listOf())
                          .fieldOf("items")
                          .forGetter(function -> function.pieces))
                  .apply(instance, PickUnownedLootFunction::new));

  private final List<Item> pieces;

  PickUnownedLootFunction(final List<LootCondition> conditions, final List<Item> pieces) {
    super(conditions);
    this.pieces = List.copyOf(pieces);
  }

  @Override
  public LootFunctionType<PickUnownedLootFunction> getType() {
    return AccessoryLoot.PICK_UNOWNED;
  }

  @Override
  protected ItemStack process(final ItemStack stack, final LootContext context) {
    final List<Item> fair = UnownedPick.candidates(this.pieces, owned(context));
    final Item picked = fair.get(context.getRandom().nextInt(fair.size()));
    return new ItemStack(picked, stack.getCount());
  }

  private static List<Set<Item>> owned(final LootContext context) {
    final List<Set<Item>> owned = new ArrayList<>();
    final Entity boss = context.get(LootContextParameters.THIS_ENTITY);
    if (boss instanceof Fought fought && boss.getServer() != null) {
      fought.fighters().stream()
          .map(id -> boss.getServer().getPlayerManager().getPlayer(id))
          .filter(Objects::nonNull)
          .forEach(player -> owned.add(AccessoryOwnership.items(player)));
    }
    final PlayerEntity killer = context.get(LootContextParameters.LAST_DAMAGE_PLAYER);
    if (owned.isEmpty() && killer != null) {
      owned.add(AccessoryOwnership.items(killer));
    }
    return owned;
  }
}
