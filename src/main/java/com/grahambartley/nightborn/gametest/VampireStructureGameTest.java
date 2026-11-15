package com.grahambartley.nightborn.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.config.StructureSettings;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.FogZone;
import com.grahambartley.nightborn.structure.Household;
import com.grahambartley.nightborn.structure.HouseholdSpawner;
import com.grahambartley.nightborn.structure.StructureFogTracker;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.StructureStart;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;

public class VampireStructureGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String RARITY_BATCH = "structure-rarity";
  static final int DOUBLED = 200;
  static final RegistryKey<LootTable> LOOT =
      RegistryKey.of(
          RegistryKeys.LOOT_TABLE, Identifier.of(Nightborn.MOD_ID, "chests/vampire_structure"));

  @BeforeBatch(batchId = RARITY_BATCH)
  public void doubleRarity(final ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(StructureSettings.STRUCTURE_RARITY, DOUBLED));
  }

  @AfterBatch(batchId = RARITY_BATCH)
  public void restoreRarity(final ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(
                StructureSettings.STRUCTURE_RARITY,
                StructureSettings.STRUCTURE_RARITY.defaultValue()));
  }

  private static BlockBox arena(final TestContext context) {
    return BlockBox.create(
        context.getAbsolutePos(new BlockPos(0, 1, 0)),
        context.getAbsolutePos(new BlockPos(8, 4, 8)));
  }

  private static Household household(final int residents) {
    return new Household(
        Registries.ENTITY_TYPE.getEntry(NightbornMobs.VAMPIRE),
        ConstantIntProvider.create(residents));
  }

  static String structureJson(final int reach) {
    return structureJson(reach, 2);
  }

  static String structureJson(final int reach, final int size) {
    return """
        {
          "type": "nightborn:vampire_structure",
          "biomes": "#minecraft:is_overworld",
          "step": "surface_structures",
          "spawn_overrides": {},
          "terrain_adaptation": "beard_thin",
          "start_pool": "minecraft:village/plains/town_centers",
          "size": %d,
          "start_height": { "absolute": 0 },
          "project_start_to_heightmap": "WORLD_SURFACE_WG",
          "max_distance_from_center": %d,
          "household": {
            "resident": "nightborn:vampire",
            "residents": { "type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 4 }
          }
        }
        """
        .formatted(size, reach);
  }

  private static DataResult<Structure> decodeStructure(final ServerWorld world, final int reach) {
    return decodeStructure(world, reach, 2);
  }

  private static DataResult<Structure> decodeStructure(
      final ServerWorld world, final int reach, final int size) {
    final JsonElement json = JsonParser.parseString(structureJson(reach, size));
    return Structure.STRUCTURE_CODEC.parse(
        RegistryOps.of(JsonOps.INSTANCE, world.getRegistryManager()), json);
  }

  private static StructurePlacement decodePlacement(final ServerWorld world) {
    return decodePlacement(world, 40, 12).getOrThrow();
  }

  private static DataResult<StructurePlacement> decodePlacement(
      final ServerWorld world, final int spacing, final int separation) {
    final JsonElement json =
        JsonParser.parseString(
            """
            { "type": "nightborn:vampire_structure", "salt": 1975, "spacing": %d, "separation": %d }
            """
                .formatted(spacing, separation));
    return StructurePlacement.TYPE_CODEC.parse(
        RegistryOps.of(JsonOps.INSTANCE, world.getRegistryManager()), json);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aHouseholdSettlesItsResidentsOnTheFloor(final TestContext context) {
    final BlockBox box = arena(context);
    final List<Entity> settled =
        HouseholdSpawner.settle(context.getWorld(), box, box, household(3), Random.create(5L));
    context.assertTrue(settled.size() == 3, "three residents settle, not " + settled.size());
    for (final Entity resident : settled) {
      context.assertTrue(
          resident.getType() == NightbornMobs.VAMPIRE, "the resident is the household's type");
      context.assertTrue(
          resident instanceof MobEntity mob && mob.isPersistent(),
          "a resident never despawns from its home");
      context.assertTrue(
          resident.getBlockY() == box.getMinY() + 1 && box.contains(resident.getBlockPos()),
          "a resident stands on the floor inside its heart, at " + resident.getBlockPos());
      context.assertTrue(resident.isAlive(), "a resident is in the world");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aHouseholdSettlesOnlyFromTheChunkHoldingItsHeart(final TestContext context) {
    final BlockBox heart = arena(context);
    final BlockBox elsewhere = heart.offset(64, 0, 64);
    final List<Entity> settled =
        HouseholdSpawner.settle(
            context.getWorld(), elsewhere, heart, household(3), Random.create(5L));
    context.assertTrue(settled.isEmpty(), "a chunk away from the heart settles no one");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void everyStructureChestHoldsBloodAndGear(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final LootTable table = world.getServer().getReloadableRegistries().getLootTable(LOOT);
    context.assertTrue(table != LootTable.EMPTY, "the structure chest loot table loads");
    final LootContextParameterSet parameters =
        new LootContextParameterSet.Builder(world)
            .add(
                LootContextParameters.ORIGIN,
                Vec3d.ofCenter(context.getAbsolutePos(BlockPos.ORIGIN)))
            .build(LootContextTypes.CHEST);
    for (long seed = 0; seed < 20; seed++) {
      final List<ItemStack> loot = table.generateLoot(parameters, seed);
      context.assertTrue(
          loot.stream().anyMatch(stack -> stack.isOf(BloodItems.BLOOD_BOTTLE)),
          "seed " + seed + " gives at least one Blood Bottle");
      context.assertTrue(loot.size() >= 3, "seed " + seed + " fills at least three slots");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void structureDataRefusesAReachPastTheReferencedChunks(final TestContext context) {
    final ServerWorld world = context.getWorld();
    context.assertTrue(
        decodeStructure(world, 116).isSuccess(), "a reach that fits with its padding loads");
    context.assertTrue(
        decodeStructure(world, 117).isError(),
        "a reach that would spill past the referenced chunks is refused");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void structureDataRefusesALayoutWithNoDepth(final TestContext context) {
    final ServerWorld world = context.getWorld();
    context.assertTrue(decodeStructure(world, 64, 1).isSuccess(), "a depth of one loads");
    context.assertTrue(
        decodeStructure(world, 64, 0).isError(),
        "a depth of zero is refused, because the jigsaw generator then places no pieces at all");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void placementRefusesASeparationAsWideAsItsSpacing(final TestContext context) {
    final ServerWorld world = context.getWorld();
    context.assertTrue(
        decodePlacement(world, 40, 39).isSuccess(), "separation below spacing loads");
    context.assertTrue(
        decodePlacement(world, 40, 40).isError(), "separation equal to spacing is refused");
    context.assertTrue(
        decodePlacement(world, 40, 60).isError(), "separation past spacing is refused");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void placementKeepsItsSpacingAtNormalRarity(final TestContext context) {
    final StructurePlacement placement = decodePlacement(context.getWorld());
    context.assertTrue(
        placement instanceof RandomSpreadStructurePlacement spread
            && spread.getSpacing() == 40
            && spread.getSeparation() == 12,
        "at 100% the placement keeps the spacing in its data");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = RARITY_BATCH, tickLimit = 20)
  public void placementSpreadsFurtherWhenStructuresAreRarer(final TestContext context) {
    final StructurePlacement placement = decodePlacement(context.getWorld());
    context.assertTrue(
        placement instanceof RandomSpreadStructurePlacement spread
            && spread.getSpacing() == 80
            && spread.getSeparation() == 24,
        "at 200% spacing and separation double");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void fogFindsTheHeartOfAStructureAroundAPlayer(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final Structure structure = decodeStructure(world, 64).getOrThrow();
    final BlockBox heart = arena(context);
    final BlockPos centre = heart.getCenter();
    final WorldChunk chunk = world.getWorldChunk(centre);
    final ChunkPos home = chunk.getPos();
    final StructureStart start =
        new StructureStart(structure, home, 0, new StructurePiecesList(List.of(new Marker(heart))));
    final Map<Structure, StructureStart> startsBefore = new HashMap<>(chunk.getStructureStarts());
    final Map<Structure, LongSet> referencesBefore = new HashMap<>(chunk.getStructureReferences());
    chunk.setStructureStart(structure, start);
    chunk.addStructureReference(structure, home.toLong());
    try {
      final ServerPlayerEntity inside = context.createMockCreativeServerPlayerInWorld();
      final ServerPlayerEntity outside = context.createMockCreativeServerPlayerInWorld();
      final FogZone expected = FogZone.around(heart, start.getBoundingBox());
      inside.refreshPositionAndAngles(
          centre.getX() + 0.5, centre.getY(), centre.getZ() + 0.5, 0, 0);
      outside.refreshPositionAndAngles(
          centre.getX() + 0.5, centre.getY() + expected.reach() + 4, centre.getZ() + 0.5, 0, 0);
      final Optional<FogZone> insideZone = StructureFogTracker.zoneAround(inside);
      context.assertTrue(
          insideZone.equals(Optional.of(expected)), "fog centres on the heart, got " + insideZone);
      context.assertTrue(
          StructureFogTracker.zoneAround(outside).isEmpty(),
          "a second player beyond the structure gets no fog while the first is inside");
      context.assertTrue(
          StructureFogTracker.zoneAround(inside).equals(Optional.of(expected)),
          "the first player keeps their fog whatever the second player sees");
    } finally {
      chunk.setStructureStarts(startsBefore);
      chunk.setStructureReferences(referencesBefore);
    }
    context.complete();
  }

  private static final class Marker extends StructurePiece {

    Marker(final BlockBox box) {
      super(StructurePieceType.JIGSAW, 0, box);
    }

    @Override
    protected void writeNbt(final StructureContext context, final NbtCompound nbt) {}

    @Override
    public void generate(
        final StructureWorldAccess world,
        final StructureAccessor structures,
        final ChunkGenerator generator,
        final Random random,
        final BlockBox chunkBox,
        final ChunkPos chunk,
        final BlockPos pivot) {}
  }
}
