package com.grahambartley.nightborn.lore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class LecternBookProcessorTest {

  private static final BlockPos AT = new BlockPos(3, 64, -5);

  private static StructureBlockInfo block(final String id) {
    final Block block = Registries.BLOCK.get(Identifier.ofVanilla(id));
    return new StructureBlockInfo(AT, block.getDefaultState(), null);
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "lectern, true",
    "bookshelf, false",
    "chiseled_bookshelf, false",
    "chest, false",
    "enchanting_table, false",
    "air, false"
  })
  @DisplayName("only a lectern is given a book")
  void onlyLecternsRead(final String id, final boolean reads) {
    assertEquals(reads, LecternBookProcessor.reads(block(id)));
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("an empty lectern is given the book and keeps its facing")
  void anEmptyLecternIsFilled(final Direction facing) {
    final StructureBlockInfo empty =
        new StructureBlockInfo(
            AT, Blocks.LECTERN.getDefaultState().with(LecternBlock.FACING, facing), null);

    final StructureBlockInfo filled = LecternBookProcessor.shelve(empty, NbtString.of("book"));

    assertEquals(AT, filled.pos());
    assertEquals(facing, filled.state().get(LecternBlock.FACING));
    assertTrue(filled.state().get(LecternBlock.HAS_BOOK));
    assertFalse(filled.state().get(LecternBlock.POWERED));
    assertEquals(NbtString.of("book"), filled.nbt().get("Book"));
    assertEquals(0, filled.nbt().getInt("Page"));
    assertNull(empty.nbt());
  }

  @ParameterizedTest(name = "left open at page {0}")
  @ValueSource(ints = {0, 1, 4})
  @DisplayName("a lectern that came with a book has it replaced and opens at the first page")
  void aTemplateBookIsReplaced(final int page) {
    final NbtCompound template = new NbtCompound();
    template.putString("id", "minecraft:lectern");
    template.put("Book", NbtString.of("blank"));
    template.putInt("Page", page);
    final StructureBlockInfo stocked =
        new StructureBlockInfo(
            AT, Blocks.LECTERN.getDefaultState().with(LecternBlock.HAS_BOOK, true), template);

    final StructureBlockInfo filled = LecternBookProcessor.shelve(stocked, NbtString.of("lore"));

    assertEquals(NbtString.of("lore"), filled.nbt().get("Book"));
    assertEquals(0, filled.nbt().getInt("Page"));
    assertEquals("minecraft:lectern", filled.nbt().getString("id"));
    assertEquals(NbtString.of("blank"), template.get("Book"));
    assertEquals(page, template.getInt("Page"));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"nightborn:crypt", "nightborn:mountain_castle", "other_pack:library"})
  @DisplayName("the processor reads its shelf from data and writes it back")
  void theShelfRoundTrips(final String shelf) {
    final LecternBookProcessor processor =
        LecternBookProcessor.CODEC
            .codec()
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"books\":\"" + shelf + "\"}"))
            .getOrThrow();
    assertEquals(Identifier.of(shelf), processor.books().id());
    assertEquals(LoreBooks.BOOK, processor.books().registry());
    assertEquals(
        "{\"books\":\"" + shelf + "\"}",
        LecternBookProcessor.CODEC
            .codec()
            .encodeStart(JsonOps.INSTANCE, processor)
            .getOrThrow()
            .toString());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"{}", "{\"books\":\"Not An Id\"}", "{\"books\":7}"})
  @DisplayName("a processor with no usable shelf is refused")
  void aBadShelfIsRefused(final String json) {
    assertTrue(
        LecternBookProcessor.CODEC
            .codec()
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .isError());
  }
}
