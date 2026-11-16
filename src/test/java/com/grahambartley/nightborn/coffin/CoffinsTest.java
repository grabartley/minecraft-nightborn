package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
  @EnumSource(CoffinMaterial.class)
  @DisplayName("each material registers a coffin block placed by a single-stack bed item")
  void registersEachMaterial(final CoffinMaterial material) {
    final Block block = Coffins.get(material);
    assertEquals(Identifier.of("nightborn", material.id()), Registries.BLOCK.getId(block));
    assertInstanceOf(CoffinBlock.class, block);
    assertInstanceOf(BedItem.class, block.asItem());
    assertEquals(1, block.asItem().getMaxCount());
    assertTrue(Coffins.all().contains(block));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CoffinMaterial.class)
  @DisplayName("every coffin can hold the lid's block entity")
  void sharesOneBlockEntityType(final CoffinMaterial material) {
    assertTrue(Coffins.BLOCK_ENTITY.supports(Coffins.get(material).getDefaultState()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CoffinMaterial.class)
  @DisplayName(
      "coffins catch fire exactly as their source block does, so crimson and stone never burn")
  void burnsLikeItsSource(final CoffinMaterial material) {
    final FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
    assertEquals(fire.get(material.source()), fire.get(Coffins.get(material)));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CoffinMaterial.class)
  @DisplayName("each coffin block knows the material it was made from")
  void findsTheMaterialOfEachCoffin(final CoffinMaterial material) {
    assertEquals(material, Coffins.material(Coffins.get(material)));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CoffinMaterial.class)
  @DisplayName("a coffin is as hard to break as its source block and needs the same tool")
  void breaksLikeItsSource(final CoffinMaterial material) {
    final BlockState coffin = Coffins.get(material).getDefaultState();
    final BlockState source = material.source().getDefaultState();
    assertEquals(source.getHardness(null, null), coffin.getHardness(null, null));
    assertEquals(source.isToolRequired(), coffin.isToolRequired());
    assertEquals(source.getSoundGroup(), coffin.getSoundGroup());
  }
}
