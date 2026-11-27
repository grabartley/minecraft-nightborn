package com.grahambartley.nightborn.lore;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public final class LecternBookProcessor extends StructureProcessor {

  public static final MapCodec<LecternBookProcessor> CODEC =
      TagKey.unprefixedCodec(LoreBooks.BOOK)
          .fieldOf("books")
          .xmap(LecternBookProcessor::new, LecternBookProcessor::books);

  public static final StructureProcessorType<LecternBookProcessor> TYPE =
      Registry.register(
          Registries.STRUCTURE_PROCESSOR,
          Identifier.of(Nightborn.MOD_ID, "lectern_book"),
          () -> CODEC);

  private static final String BOOK = "Book";
  private static final String PAGE = "Page";

  private final TagKey<LoreBook> books;

  public LecternBookProcessor(final TagKey<LoreBook> books) {
    this.books = books;
  }

  public static void register() {}

  public TagKey<LoreBook> books() {
    return books;
  }

  static boolean reads(final StructureBlockInfo block) {
    return block.state().isOf(Blocks.LECTERN);
  }

  static StructureBlockInfo shelve(final StructureBlockInfo lectern, final NbtElement book) {
    final NbtCompound nbt = lectern.nbt() == null ? new NbtCompound() : lectern.nbt().copy();
    nbt.put(BOOK, book);
    nbt.putInt(PAGE, 0);
    return new StructureBlockInfo(
        lectern.pos(), lectern.state().with(LecternBlock.HAS_BOOK, true), nbt);
  }

  @Override
  public StructureBlockInfo process(
      final WorldView world,
      final BlockPos pos,
      final BlockPos pivot,
      final StructureBlockInfo original,
      final StructureBlockInfo current,
      final StructurePlacementData data) {
    if (!reads(current)) {
      return current;
    }
    return LoreShelf.pick(
            LoreBooks.shelf(world.getRegistryManager(), books), data.getRandom(current.pos()))
        .map(book -> shelve(current, book.stack().encode(world.getRegistryManager())))
        .orElse(current);
  }

  @Override
  protected StructureProcessorType<?> getType() {
    return TYPE;
  }
}
