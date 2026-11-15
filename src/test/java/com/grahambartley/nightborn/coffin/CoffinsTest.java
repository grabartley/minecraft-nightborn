package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.BedItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoffinsTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(CoffinWood.class)
  @DisplayName("each wood registers a coffin block placed by a single-stack bed item")
  void registersEachWood(final CoffinWood wood) {
    final Block block = Coffins.get(wood);
    assertEquals(Identifier.of("nightborn", wood.id()), Registries.BLOCK.getId(block));
    assertInstanceOf(CoffinBlock.class, block);
    assertInstanceOf(BedItem.class, block.asItem());
    assertEquals(1, block.asItem().getMaxCount());
    assertTrue(Coffins.all().contains(block));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CoffinWood.class)
  @DisplayName("every coffin can hold the lid's block entity")
  void sharesOneBlockEntityType(final CoffinWood wood) {
    assertTrue(Coffins.BLOCK_ENTITY.supports(Coffins.get(wood).getDefaultState()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CoffinWood.class)
  @DisplayName("coffins catch fire exactly as their planks do, so crimson never burns")
  void burnsLikeItsPlanks(final CoffinWood wood) {
    final FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
    assertEquals(fire.get(wood.planks()), fire.get(Coffins.get(wood)));
  }
}
