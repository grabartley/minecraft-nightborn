package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.HouseholdTie;
import com.grahambartley.nightborn.mob.look.VampireVoice;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.Arrays;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class MemberOrders implements WheelAction<VampireEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "house_order");

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final VampireEntity member) {
    return MemberLife.houseOf(player.getServer(), member)
        .filter(house -> house.lord().equals(player.getUuid()))
        .isPresent();
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final VampireEntity member) {
    final HouseholdTie tie = member.householdTie();
    return Arrays.stream(MemberOrder.values())
        .map(
            order ->
                new WheelEntry(
                    ID,
                    order.ordinal(),
                    order.icon(),
                    Text.translatable(order.labelKey()),
                    order.current(tie.stance(), tie.provider())))
        .toList();
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final VampireEntity member, final WheelEntry chosen) {
    MemberOrder.byIndex(chosen.argument())
        .ifPresent(
            order -> {
              switch (order) {
                case FOLLOW, STAY, GUARD ->
                    member.householdTie().order(order.stance().orElseThrow(), member.getBlockPos());
                case FEED -> MemberLife.sendOut(member, false);
                case BRING_BLOOD -> MemberLife.sendOut(member, true);
                case LEAVE -> MemberLife.goWild(player.getServerWorld(), member);
              }
              member.getNavigation().stop();
              member.speak(VampireVoice.TALK);
              player.sendMessage(Text.translatable(order.cueKey(), member.getDisplayName()), true);
            });
  }
}
