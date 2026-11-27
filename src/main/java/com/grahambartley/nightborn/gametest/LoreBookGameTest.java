package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.lore.LoreBook;
import java.util.Collection;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class LoreBookGameTest implements FabricGameTest {

  private static final String ROOM = "nightborn:decor_room";
  private static final BlockPos FLOOR = new BlockPos(2, 1, 2);
  private static final BlockPos ON_FLOOR = FLOOR.up();
  private static final List<String> KINDS =
      List.of("castle", "crypt", "lair", "manor", "mountain_castle");
  private static final int FEWEST_PAGES = 2;
  private static final int MOST_PAGES = 6;

  @CustomTestProvider
  public Collection<TestFunction> everyKindShelvesAWrittenBook() {
    return KINDS.stream()
        .map(
            kind ->
                new TestFunction(
                    "defaultBatch",
                    "lorebookgametest.alecterngetsabook." + kind,
                    ROOM,
                    20,
                    0L,
                    true,
                    context -> aLecternGetsABook(context, kind)))
        .toList();
  }

  private static void aLecternGetsABook(final TestContext context, final String kind) {
    final ServerWorld world = context.getWorld();
    final StructureProcessorList processors =
        world
            .getRegistryManager()
            .get(RegistryKeys.PROCESSOR_LIST)
            .get(Identifier.of(Nightborn.MOD_ID, "vampire/" + kind));
    context.assertTrue(processors != null, kind + " has a processor list");
    final BlockPos at = context.getAbsolutePos(ON_FLOOR);
    StructureBlockInfo lectern = new StructureBlockInfo(at, Blocks.LECTERN.getDefaultState(), null);
    for (final StructureProcessor processor : processors.getList()) {
      lectern = processor.process(world, at, at, lectern, lectern, new StructurePlacementData());
    }
    context.assertTrue(lectern.state().get(LecternBlock.HAS_BOOK), kind + " lectern shows a book");
    context.assertTrue(lectern.nbt() != null, kind + " lectern carries its book");

    context.setBlockState(FLOOR, Blocks.STONE);
    context.setBlockState(ON_FLOOR, lectern.state());
    final LecternBlockEntity placed = context.getBlockEntity(ON_FLOOR);
    placed.read(lectern.nbt(), world.getRegistryManager());

    final ItemStack book = placed.getBook();
    context.assertTrue(book.isOf(Items.WRITTEN_BOOK), kind + " lectern holds a written book");
    context.assertTrue(book.contains(DataComponentTypes.ITEM_NAME), kind + " book has a title");
    final WrittenBookContentComponent content = book.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
    context.assertFalse(content.author().isBlank(), kind + " book has an author");
    final int pages = content.pages().size();
    context.assertTrue(
        pages >= FEWEST_PAGES && pages <= MOST_PAGES && pages <= LoreBook.MAX_PAGES,
        kind + " book has 2 to 6 pages, not " + pages);
    context.complete();
  }
}
