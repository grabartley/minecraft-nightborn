package com.grahambartley.nightborn.lore;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

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

  static Random building(final BlockPos piece) {
    return Random.create(MathHelper.hashCode(piece));
  }

  static <T> Map<BlockPos, T> deal(
      final List<T> shelf, final Collection<BlockPos> lecterns, final Random random) {
    if (shelf.isEmpty()) {
      return Map.of();
    }
    final List<T> order = new ArrayList<>(shelf);
    Util.shuffle(order, random);
    final List<BlockPos> sorted = lecterns.stream().sorted().toList();
    final Map<BlockPos, T> dealt = new HashMap<>();
    for (int index = 0; index < sorted.size(); index++) {
      dealt.put(sorted.get(index), order.get(index % order.size()));
    }
    return dealt;
  }

  static StructureBlockInfo shelve(final StructureBlockInfo lectern, final NbtElement book) {
    final NbtCompound nbt = lectern.nbt() == null ? new NbtCompound() : lectern.nbt().copy();
    nbt.put(BOOK, book);
    nbt.putInt(PAGE, 0);
    return new StructureBlockInfo(
        lectern.pos(), lectern.state().with(LecternBlock.HAS_BOOK, true), nbt);
  }

  @Override
  public List<StructureBlockInfo> reprocess(
      final ServerWorldAccess world,
      final BlockPos pos,
      final BlockPos pivot,
      final List<StructureBlockInfo> originals,
      final List<StructureBlockInfo> currents,
      final StructurePlacementData data) {
    final List<BlockPos> lecterns =
        currents.stream().filter(LecternBookProcessor::reads).map(StructureBlockInfo::pos).toList();
    if (lecterns.isEmpty()) {
      return currents;
    }
    final DynamicRegistryManager registries = world.getRegistryManager();
    final Map<BlockPos, LoreBook> dealt =
        deal(LoreBooks.shelf(registries, books), lecterns, building(pos));
    return currents.stream()
        .map(
            block ->
                dealt.containsKey(block.pos())
                    ? shelve(block, dealt.get(block.pos()).stack().encode(registries))
                    : block)
        .toList();
  }

  @Override
  protected StructureProcessorType<?> getType() {
    return TYPE;
  }
}
