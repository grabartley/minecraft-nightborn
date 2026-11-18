package com.grahambartley.nightborn.thrall;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.servant.OrderCue;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.Arrays;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ThrallOrders implements WheelAction<ThrallEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "thrall_order");

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final ThrallEntity thrall) {
    return thrall.isOwnedBy(player);
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final ThrallEntity thrall) {
    return Arrays.stream(ThrallMode.values())
        .map(
            mode ->
                new WheelEntry(
                    ID,
                    mode.ordinal(),
                    mode.icon(),
                    Text.translatable(mode.labelKey()),
                    current(thrall.mode(), thrall.job().isPresent(), mode)))
        .toList();
  }

  public static boolean current(
      final ThrallMode mode, final boolean onAJob, final ThrallMode shown) {
    return !onAJob && mode == shown;
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final ThrallEntity thrall, final WheelEntry chosen) {
    final ThrallMode mode = ThrallMode.byIndex(chosen.argument());
    thrall.order(mode);
    OrderCue.obey(
        thrall, player, Text.translatable(mode.translationKey(), thrall.getDisplayName()));
  }
}
