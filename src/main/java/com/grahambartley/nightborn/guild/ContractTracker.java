package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.Housemates;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public final class ContractTracker {

  static final int HOUSEHOLD_REACH = 128;

  private ContractTracker() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(ContractTracker::afterDeath);
  }

  private static void afterDeath(final LivingEntity victim, final DamageSource source) {
    final MinecraftServer server = victim.getServer();
    if (server == null || !Vampires.isVampire(victim) || !Staking.staked(source)) {
      return;
    }
    Downed.hunter(source)
        .map(id -> server.getPlayerManager().getPlayer(id))
        .ifPresent(
            hunter ->
                credit(
                    hunter,
                    new HunterContract.Stake(StageRanks.of(victim), householdCleared(victim))));
  }

  public static void credit(final ServerPlayerEntity hunter, final HunterContract.Stake stake) {
    final PlayerInventory inventory = hunter.getInventory();
    final List<Optional<ContractCredit.Held>> held = new ArrayList<>();
    for (int slot = 0; slot < inventory.size(); slot++) {
      held.add(ContractItems.held(inventory.getStack(slot), hunter.getRegistryManager()));
    }
    ContractCredit.slotToAdvance(held, stake)
        .ifPresent(
            slot -> {
              final ItemStack stack = inventory.getStack(slot);
              stack.apply(ContractItems.PROGRESS, null, ContractProgress::advanced);
              hunter.sendMessage(Text.translatable("nightborn.contract.advanced"), true);
            });
  }

  static boolean householdCleared(final LivingEntity victim) {
    if (!(victim instanceof VampireEntity vampire)) {
      return false;
    }
    final Optional<BlockPos> home = vampire.homeLife().home();
    if (home.isEmpty()) {
      return false;
    }
    return victim
        .getWorld()
        .getEntitiesByClass(
            VampireEntity.class,
            new Box(home.get()).expand(HOUSEHOLD_REACH),
            other ->
                other != victim
                    && other.isAlive()
                    && Housemates.share(home, other.homeLife().home()))
        .isEmpty();
  }
}
