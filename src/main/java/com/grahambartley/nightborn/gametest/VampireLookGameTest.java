package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.clothing.ClothingItem;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.look.VampireLook;
import com.grahambartley.nightborn.mob.look.VampireSize;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class VampireLookGameTest implements FabricGameTest {

  static final float SHORTEST = 1.5F;
  static final float TALLEST = 2.0F;
  static final float TOLERANCE = 0.001F;

  @GameTest(templateName = VampireMobGameTest.ARENA, tickLimit = 20)
  public void aNewVampireIsDressedInClothingAndSizedWithinRange(final TestContext context) {
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(4, 2, 4));
    vampire.initialize(
        context.getWorld(),
        context.getWorld().getLocalDifficulty(vampire.getBlockPos()),
        SpawnReason.SPAWN_EGG,
        null);
    context.assertTrue(vampire.look().isPresent(), "the vampire has a look from the data");
    final VampireLook look = vampire.look().orElseThrow();
    context.assertTrue(
        vampire.height() >= SHORTEST - TOLERANCE && vampire.height() <= TALLEST + TOLERANCE,
        "height is between 1.5 and 2 blocks: " + vampire.height());
    for (final EquipmentSlot slot :
        List.of(EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
      context.assertTrue(
          vampire.getEquippedStack(slot).getItem() instanceof ClothingItem clothing
              && clothing.piece().type().getEquipmentSlot() == slot,
          look.rig().asString() + " vampire wears craftable clothing in " + slot.getName());
    }
    context.assertTrue(
        Math.abs(vampire.getHeight() - vampire.height()) < TOLERANCE,
        "the hitbox is as tall as the vampire: " + vampire.getHeight());
    context.assertTrue(
        Math.abs(
                vampire.getWidth()
                    - VampireSize.BASE_WIDTH
                        * vampire.width()
                        * VampireSize.scale(vampire.height()))
            < TOLERANCE,
        "the hitbox is as wide as the build: " + vampire.getWidth());
    context.complete();
  }
}
