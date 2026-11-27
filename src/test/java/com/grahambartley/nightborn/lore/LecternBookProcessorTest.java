package com.grahambartley.nightborn.lore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
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
import net.minecraft.util.math.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class LecternBookProcessorTest {

  private static final BlockPos AT = new BlockPos(3, 64, -5);
  private static final BlockPos AT_FIRST = new BlockPos(-20, 60, 11);

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

  private static List<Integer> shelf(final int size) {
    return IntStream.range(0, size).boxed().toList();
  }

  private static List<BlockPos> lecterns(final int count) {
    return IntStream.range(0, count)
        .mapToObj(index -> new BlockPos(index * 7 - 20, 60 + index % 3, 11 - index * 5))
        .toList();
  }

  @ParameterizedTest(name = "{0} lecterns")
  @ValueSource(ints = {0, 1, 6})
  @DisplayName("an empty shelf deals no books")
  void anEmptyShelfDealsNothing(final int count) {
    assertEquals(
        Map.of(), LecternBookProcessor.deal(List.of(), lecterns(count), Random.create(1L)));
  }

  @ParameterizedTest(name = "{1} lecterns from a shelf of {0}, seed {2}")
  @CsvSource({"7, 6, 0", "7, 7, 5", "9, 1, 3", "9, 2, -8", "8, 8, 41", "20, 12, 7", "1, 1, 0"})
  @DisplayName("a building with no more lecterns than books never shows a book twice")
  void aBuildingNeverRepeatsABook(final int size, final int count, final long seed) {
    final List<Integer> shelf = shelf(size);
    final Map<BlockPos, Integer> dealt =
        LecternBookProcessor.deal(shelf, lecterns(count), Random.create(seed));
    assertEquals(new HashSet<>(lecterns(count)), dealt.keySet());
    assertEquals(count, new HashSet<>(dealt.values()).size());
    assertTrue(shelf.containsAll(dealt.values()));
  }

  @ParameterizedTest(name = "{1} lecterns from a shelf of {0}")
  @CsvSource({"1, 3", "2, 5", "7, 8", "7, 20"})
  @DisplayName("more lecterns than books still fills every lectern and uses the whole shelf")
  void aSmallShelfGoesRound(final int size, final int count) {
    final List<Integer> shelf = shelf(size);
    final Map<BlockPos, Integer> dealt =
        LecternBookProcessor.deal(shelf, lecterns(count), Random.create(9L));
    assertEquals(count, dealt.size());
    assertEquals(new HashSet<>(shelf), new HashSet<>(dealt.values()));
  }

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {0L, 7L, -99L, Long.MAX_VALUE})
  @DisplayName("the same building is dealt the same books whatever order its lecterns are read in")
  void theDealIsStable(final long seed) {
    final List<Integer> shelf = shelf(9);
    final List<BlockPos> forwards = lecterns(6);
    final List<BlockPos> backwards = new ArrayList<>(forwards);
    Collections.reverse(backwards);
    assertEquals(
        LecternBookProcessor.deal(shelf, forwards, Random.create(seed)),
        LecternBookProcessor.deal(shelf, backwards, Random.create(seed)));
  }

  @ParameterizedTest(name = "a shelf of {0}")
  @ValueSource(ints = {7, 8, 9})
  @DisplayName("buildings in different places open on different books")
  void buildingsDiffer(final int size) {
    final List<Integer> shelf = shelf(size);
    final Set<Integer> first = new HashSet<>();
    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        final BlockPos piece = new BlockPos(x * 48 - 400, 64, z * 48 - 400);
        first.add(
            LecternBookProcessor.deal(shelf, lecterns(1), LecternBookProcessor.building(piece, 0L))
                .get(AT_FIRST));
      }
    }
    assertEquals(new HashSet<>(shelf), first);
  }

  @ParameterizedTest(name = "world seeds {0} and {1}")
  @CsvSource({"0, 1", "42, -42", "123456789, 987654321"})
  @DisplayName("the same spot in two worlds is not always dealt the same book")
  void worldsDiffer(final long one, final long other) {
    final List<Integer> shelf = shelf(9);
    int differing = 0;
    for (int x = 0; x < 16; x++) {
      final BlockPos piece = new BlockPos(x * 48, 64, -x * 31);
      if (!LecternBookProcessor.deal(shelf, lecterns(6), LecternBookProcessor.building(piece, one))
          .equals(
              LecternBookProcessor.deal(
                  shelf, lecterns(6), LecternBookProcessor.building(piece, other)))) {
        differing++;
      }
    }
    assertTrue(differing > 8);
  }
}
