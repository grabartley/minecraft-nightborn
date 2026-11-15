package com.grahambartley.nightborn.familiar.goal;

import com.grahambartley.nightborn.familiar.ChestMarks;
import com.grahambartley.nightborn.familiar.ErrandRules;
import com.grahambartley.nightborn.familiar.ErrandTransfer;
import com.grahambartley.nightborn.familiar.FamiliarBond;
import com.grahambartley.nightborn.familiar.FamiliarClock;
import com.grahambartley.nightborn.familiar.FamiliarOrders;
import com.grahambartley.nightborn.familiar.FamiliarRegistry;
import com.grahambartley.nightborn.familiar.HumanFamiliarEntity;
import com.grahambartley.nightborn.familiar.TradePick;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.TradeOffer;

public class RunErrandGoal extends Goal {

  static final double SPEED = 0.7;
  static final double REACH = 2.5;
  static final int TRADE_RANGE = 16;
  static final int MOST_TRADES = 16;
  static final int GIVE_UP_TICKS = 1200;
  static final int REST_TICKS = 100;

  enum Leg {
    FETCH,
    TRADE,
    DELIVER
  }

  private final HumanFamiliarEntity familiar;
  private Leg leg;
  private BlockPos fetch;
  private BlockPos deliver;
  private VillagerEntity trader;
  private int ticks;
  private long restUntil;

  public RunErrandGoal(final HumanFamiliarEntity familiar) {
    this.familiar = familiar;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!(familiar.getWorld() instanceof ServerWorld world)
        || world.getTime() < restUntil
        || familiar.getTarget() != null
        || !FamiliarClock.isWorkingHours(world)) {
      return false;
    }
    final Optional<ChestMarks> marks =
        familiar
            .owner()
            .flatMap(owner -> FamiliarRegistry.of(world.getServer()).bondOf(owner))
            .filter(bond -> bond.familiar().equals(familiar.getUuid()))
            .map(FamiliarBond::marks)
            .filter(ChestMarks::complete);
    if (marks.isEmpty()) {
      return false;
    }
    final int range = ServerConfigService.current().familiars().errandRange();
    final GlobalPos from = marks.get().fetch().orElseThrow();
    final GlobalPos to = marks.get().deliver().orElseThrow();
    if (!ErrandRules.inReach(
        from.dimension() == world.getRegistryKey(),
        to.dimension() == world.getRegistryKey(),
        familiar.getBlockPos().getSquaredDistance(from.pos()),
        familiar.getBlockPos().getSquaredDistance(to.pos()),
        range)) {
      return false;
    }
    fetch = from.pos();
    deliver = to.pos();
    return true;
  }

  @Override
  public void start() {
    leg = familiar.getInventory().isEmpty() ? Leg.FETCH : Leg.TRADE;
    ticks = 0;
    trader = null;
  }

  @Override
  public boolean shouldContinue() {
    return leg != null
        && ticks < GIVE_UP_TICKS
        && familiar.getTarget() == null
        && FamiliarClock.isWorkingHours(familiar.getWorld());
  }

  @Override
  public void stop() {
    leg = null;
    trader = null;
    familiar.getNavigation().stop();
    restUntil = familiar.getWorld().getTime() + REST_TICKS;
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    ticks++;
    switch (leg) {
      case FETCH -> fetchLeg();
      case TRADE -> tradeLeg();
      case DELIVER -> deliverLeg();
    }
  }

  private void fetchLeg() {
    if (!reached(fetch)) {
      return;
    }
    if (!stillErrandChest(fetch)) {
      return;
    }
    final Inventory chest = HopperBlockEntity.getInventoryAt(familiar.getWorld(), fetch);
    leg =
        chest != null && ErrandTransfer.fetchOneStack(chest, familiar.getInventory()) > 0
            ? Leg.TRADE
            : null;
  }

  private void tradeLeg() {
    if (trader == null || !trader.isAlive() || trader.hasCustomer()) {
      trader = findTrader();
      if (trader == null) {
        leg = Leg.DELIVER;
        return;
      }
    }
    familiar.getLookControl().lookAt(trader);
    if (familiar.squaredDistanceTo(trader) > REACH * REACH) {
      if (ticks % 20 == 1) {
        familiar.getNavigation().startMovingTo(trader, SPEED);
      }
      return;
    }
    for (int trade = 0; trade < MOST_TRADES; trade++) {
      final Optional<TradeOffer> offer =
          TradePick.affordable(trader.getOffers(), familiar.getInventory());
      if (offer.isEmpty()) {
        break;
      }
      final ItemStack left = TradePick.pay(offer.get(), familiar.getInventory());
      offer.get().use();
      if (!left.isEmpty()) {
        familiar.dropStack(left);
      }
    }
    leg = Leg.DELIVER;
  }

  private void deliverLeg() {
    if (!reached(deliver)) {
      return;
    }
    if (!stillErrandChest(deliver)) {
      return;
    }
    final Inventory chest = HopperBlockEntity.getInventoryAt(familiar.getWorld(), deliver);
    if (chest != null) {
      ErrandTransfer.deliverAll(familiar.getInventory(), chest);
    }
    leg = null;
  }

  private boolean stillErrandChest(final BlockPos chest) {
    if (FamiliarOrders.isErrandChest(familiar.getWorld(), chest)) {
      return true;
    }
    if (familiar.getWorld() instanceof ServerWorld world) {
      familiar
          .owner()
          .ifPresent(
              owner ->
                  FamiliarOrders.forget(
                      world.getServer(), owner, GlobalPos.create(world.getRegistryKey(), chest)));
    }
    leg = null;
    return false;
  }

  private boolean reached(final BlockPos chest) {
    if (familiar.getBlockPos().isWithinDistance(chest, REACH)) {
      return true;
    }
    if (ticks % 20 == 1 || familiar.getNavigation().isIdle()) {
      familiar
          .getNavigation()
          .startMovingTo(chest.getX() + 0.5, chest.getY(), chest.getZ() + 0.5, SPEED);
    }
    return false;
  }

  private VillagerEntity findTrader() {
    return familiar
        .getWorld()
        .getEntitiesByClass(
            VillagerEntity.class,
            familiar.getBoundingBox().expand(TRADE_RANGE),
            villager ->
                villager.isAlive()
                    && !villager.isBaby()
                    && !villager.hasCustomer()
                    && TradePick.affordable(villager.getOffers(), familiar.getInventory())
                        .isPresent())
        .stream()
        .min(Comparator.comparingDouble(familiar::squaredDistanceTo))
        .orElse(null);
  }
}
