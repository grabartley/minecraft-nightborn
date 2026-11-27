package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public final class LoreBookGameTest implements FabricGameTest {

  private static final String ROOM = "nightborn:decor_room";
  private static final BlockPos FIRST = new BlockPos(1, 2, 2);
  private static final int LECTERNS = 3;
  private static final int IN_FIRST_PASS = 1;
  private static final List<String> KINDS =
      List.of("castle", "crypt", "lair", "manor", "mountain_castle");
  private static final int FEWEST_PAGES = 2;
  private static final int MOST_PAGES = 6;

  @CustomTestProvider
  public Collection<TestFunction> everyKindShelvesItsLecterns() {
    return KINDS.stream()
        .map(
            kind ->
                new TestFunction(
                    "defaultBatch",
                    "lorebookgametest.apieceplacedintwopasses." + kind,
                    ROOM,
                    20,
                    0L,
                    true,
                    context -> aPiecePlacedInTwoPassesHoldsDifferentBooks(context, kind)))
        .toList();
  }

  private static void aPiecePlacedInTwoPassesHoldsDifferentBooks(
      final TestContext context, final String kind) {
    final ServerWorld world = context.getWorld();
    final StructureProcessorList processors =
        world
            .getRegistryManager()
            .get(RegistryKeys.PROCESSOR_LIST)
            .get(Identifier.of(Nightborn.MOD_ID, "vampire/" + kind));
    context.assertTrue(processors != null, kind + " has a processor list");
    final StructureTemplate piece = rowOfEmptyLecterns(world);
    final BlockPos at = context.getAbsolutePos(FIRST);

    place(world, piece, processors, at, BlockBox.create(at, at.east(IN_FIRST_PASS - 1)));
    place(world, piece, processors, at, BlockBox.create(at.east(IN_FIRST_PASS), at.east(LECTERNS)));

    final Set<String> books = new HashSet<>();
    for (int step = 0; step < LECTERNS; step++) {
      final LecternBlockEntity lectern = context.getBlockEntity(FIRST.east(step));
      final ItemStack book = lectern.getBook();
      context.assertTrue(book.isOf(Items.WRITTEN_BOOK), kind + " lectern holds a written book");
      context.assertTrue(book.contains(DataComponentTypes.ITEM_NAME), kind + " book has a title");
      final WrittenBookContentComponent content = book.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
      context.assertFalse(content.author().isBlank(), kind + " book has an author");
      final int pages = content.pages().size();
      context.assertTrue(
          pages >= FEWEST_PAGES && pages <= MOST_PAGES,
          kind + " book has 2 to 6 pages, not " + pages);
      books.add(book.getName().getString());
    }
    context.assertTrue(
        books.size() == LECTERNS,
        kind + " gives each lectern of a piece a different book across both passes: " + books);
    context.complete();
  }

  private static void place(
      final ServerWorld world,
      final StructureTemplate piece,
      final StructureProcessorList processors,
      final BlockPos at,
      final BlockBox pass) {
    final StructurePlacementData placement = new StructurePlacementData().setBoundingBox(pass);
    processors.getList().forEach(placement::addProcessor);
    piece.place(world, at, at, placement, world.getRandom(), Block.NOTIFY_LISTENERS);
  }

  private static StructureTemplate rowOfEmptyLecterns(final ServerWorld world) {
    final NbtCompound lectern = new NbtCompound();
    lectern.putString("Name", "minecraft:lectern");
    final NbtList palette = new NbtList();
    palette.add(lectern);
    final NbtList blocks = new NbtList();
    for (int step = 0; step < LECTERNS; step++) {
      final NbtCompound block = new NbtCompound();
      block.put("pos", ints(step, 0, 0));
      block.putInt("state", 0);
      blocks.add(block);
    }
    final NbtCompound nbt = new NbtCompound();
    nbt.put("size", ints(LECTERNS, 1, 1));
    nbt.put("palette", palette);
    nbt.put("blocks", blocks);
    nbt.put("entities", new NbtList());
    final StructureTemplate piece = new StructureTemplate();
    piece.readNbt(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt);
    return piece;
  }

  private static NbtList ints(final int x, final int y, final int z) {
    final NbtList list = new NbtList();
    list.add(NbtInt.of(x));
    list.add(NbtInt.of(y));
    list.add(NbtInt.of(z));
    return list;
  }
}
