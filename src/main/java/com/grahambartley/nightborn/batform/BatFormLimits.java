package com.grahambartley.nightborn.batform;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public final class BatFormLimits {

  static final Identifier BEFORE_OTHERS = Identifier.of(Nightborn.MOD_ID, "bat_form_limits");

  private BatFormLimits() {}

  public static void register() {
    UseItemCallback.EVENT.addPhaseOrdering(BEFORE_OTHERS, Event.DEFAULT_PHASE);
    UseBlockCallback.EVENT.addPhaseOrdering(BEFORE_OTHERS, Event.DEFAULT_PHASE);
    UseEntityCallback.EVENT.addPhaseOrdering(BEFORE_OTHERS, Event.DEFAULT_PHASE);
    UseItemCallback.EVENT.register(
        BEFORE_OTHERS,
        (player, world, hand) ->
            new TypedActionResult<>(verdict(BatForm.isBat(player)), player.getStackInHand(hand)));
    UseBlockCallback.EVENT.register(
        BEFORE_OTHERS, (player, world, hand, hit) -> verdict(BatForm.isBat(player)));
    UseEntityCallback.EVENT.register(
        BEFORE_OTHERS, (player, world, hand, entity, hit) -> verdict(BatForm.isBat(player)));
  }

  static ActionResult verdict(final boolean bat) {
    return bat ? ActionResult.FAIL : ActionResult.PASS;
  }
}
