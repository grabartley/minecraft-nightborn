package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.config.FamiliarSettings;
import com.grahambartley.nightborn.name.ServantNaming;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;

public final class FamiliarHiring {

  private FamiliarHiring() {}

  static void register() {
    UseEntityCallback.EVENT.register(FamiliarHiring::onUseEntity);
  }

  private static ActionResult onUseEntity(
      final PlayerEntity player,
      final World world,
      final Hand hand,
      final Entity entity,
      final EntityHitResult hit) {
    final ItemStack stack = player.getStackInHand(hand);
    if (!stack.isOf(Familiars.CONTRACT)
        || !(entity instanceof VillagerEntity villager)
        || player.isSpectator()) {
      return ActionResult.PASS;
    }
    if (!(player instanceof ServerPlayerEntity vampire)) {
      return ActionResult.SUCCESS;
    }
    hire(vampire, villager, stack);
    return ActionResult.SUCCESS;
  }

  public static HireRules.Outcome hire(
      final ServerPlayerEntity player, final VillagerEntity villager, final ItemStack contract) {
    final ServerWorld world = player.getServerWorld();
    final FamiliarRegistry registry = FamiliarRegistry.of(world.getServer());
    final HireRules.Outcome outcome =
        HireRules.decide(
            Vampires.isVampire(player),
            villager.isAlive() && !villager.isBaby(),
            villager.hasCustomer(),
            registry.bondOf(player.getUuid()).isPresent());
    if (outcome != HireRules.Outcome.HIRE) {
      player.sendMessage(Text.translatable(key(outcome)), true);
      return outcome;
    }
    final VillagerData data = villager.getVillagerData();
    final BlockPos hiredAt = villager.getBlockPos();
    villager.releaseTicketFor(MemoryModuleType.HOME);
    villager.releaseTicketFor(MemoryModuleType.JOB_SITE);
    villager.releaseTicketFor(MemoryModuleType.POTENTIAL_JOB_SITE);
    villager.releaseTicketFor(MemoryModuleType.MEETING_POINT);
    final List<ItemStack> carried = villager.getInventory().clearToList();
    final HumanFamiliarEntity familiar = villager.convertTo(Familiars.HUMAN_FAMILIAR, true);
    if (familiar == null) {
      carried.forEach(villager.getInventory()::addStack);
      return HireRules.Outcome.NOT_HIREABLE;
    }
    final FamiliarSettings settings = ServerConfigService.current().familiars();
    familiar.hire(
        player.getUuid(), data, FamiliarClock.today(world), settings.startLoyalty(), hiredAt);
    for (final ItemStack stack : carried) {
      final ItemStack left = familiar.getInventory().addStack(stack);
      if (!left.isEmpty()) {
        familiar.dropStack(left);
      }
    }
    registry.bind(player.getUuid(), familiar.getUuid());
    ServantNaming.offer(player, familiar);
    contract.decrementUnlessCreative(1, player);
    MilestoneCriterion.reach(player, Milestone.FAMILIAR_HIRED);
    world.playSound(
        null, hiredAt, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0F, 0.8F);
    world.playSound(
        null, hiredAt, SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.NEUTRAL, 1.0F, 0.9F);
    player.sendMessage(Text.translatable(key(outcome), familiar.getDisplayName()), true);
    return outcome;
  }

  static String key(final HireRules.Outcome outcome) {
    return "nightborn.familiar.hire." + outcome.name().toLowerCase(Locale.ROOT);
  }
}
