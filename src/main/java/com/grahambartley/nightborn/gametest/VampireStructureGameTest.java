package com.grahambartley.nightborn.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.config.StructureSettings;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.FogZone;
import com.grahambartley.nightborn.structure.Footing;
import com.grahambartley.nightborn.structure.Household;
import com.grahambartley.nightborn.structure.HouseholdSpawner;
import com.grahambartley.nightborn.structure.StructureZone;
import com.grahambartley.nightborn.structure.StructureZoneTracker;
import com.grahambartley.nightborn.structure.VampireStructure;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
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
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
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
        ConstantIntProvider.create(residents),
        Map.of());
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
          },
          "ambience": {
            "ambient_sound": "nightborn:ambient.castle.loop",
            "additions_sound": { "sound": "nightborn:ambient.castle.additions", "tick_chance": 0.01 },
            "music": { "sound": "nightborn:music.gothic", "min_delay": 600, "max_delay": 2400 }
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
    return decode(world, structureJson(reach, size));
  }

  static DataResult<Structure> decode(final ServerWorld world, final String json) {
    final JsonElement parsed = JsonParser.parseString(json);
    return Structure.STRUCTURE_CODEC.parse(
        RegistryOps.of(JsonOps.INSTANCE, world.getRegistryManager()), parsed);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theCryptAsksForDryLevelGround(final TestContext context) {
    final Structure crypt =
        context
            .getWorld()
            .getRegistryManager()
            .get(RegistryKeys.STRUCTURE)
            .get(Identifier.of(Nightborn.MOD_ID, "crypt"));
    context.assertTrue(
        crypt instanceof VampireStructure vampire
            && vampire.footing().equals(Optional.of(new Footing(true, 4, 8))),
        "the crypt keeps to dry ground that rises 4 blocks or less, got " + crypt);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aNearRuleStartsOnlyWhereTheOtherSetCanStart(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final StructurePlacement placement =
        StructurePlacement.TYPE_CODEC
            .parse(
                RegistryOps.of(JsonOps.INSTANCE, world.getRegistryManager()),
                JsonParser.parseString(
                    """
                    {
                      "type": "nightborn:rare_spread",
                      "rarity": "vampire_structures",
                      "salt": 1,
                      "spacing": 1,
                      "separation": 0,
                      "near": { "other_set": "minecraft:villages", "chunk_count": 1 }
                    }
                    """))
            .getOrThrow();
    final StructurePlacementCalculator calculator =
        world.getChunkManager().getStructurePlacementCalculator();
    final RandomSpreadStructurePlacement villages =
        (RandomSpreadStructurePlacement)
            world
                .getRegistryManager()
                .get(RegistryKeys.STRUCTURE_SET)
                .get(Identifier.ofVanilla("villages"))
                .placement();
    final ChunkPos village = villages.getStartChunk(calculator.getStructureSeed(), 0, 0);
    context.assertTrue(
        placement.shouldGenerate(calculator, village.x, village.z),
        "a start is allowed on the chunk a village can start on");
    context.assertFalse(
        placement.shouldGenerate(calculator, village.x + 2, village.z),
        "a start is refused two chunks from where a village can start");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theCryptFootingAcceptsDryLevelGround(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final Structure crypt =
        world
            .getRegistryManager()
            .get(RegistryKeys.STRUCTURE)
            .get(Identifier.of(Nightborn.MOD_ID, "crypt"));
    final BlockPos origin = context.getAbsolutePos(BlockPos.ORIGIN);
    final Structure.Context generation =
        new Structure.Context(
            world.getRegistryManager(),
            world.getChunkManager().getChunkGenerator(),
            world.getChunkManager().getChunkGenerator().getBiomeSource(),
            world.getChunkManager().getNoiseConfig(),
            world.getStructureTemplateManager(),
            world.getSeed(),
            new ChunkPos(origin),
            world,
            biome -> true);
    final BlockBox churchyard = BlockBox.create(origin, origin.add(37, 20, 22));
    context.assertTrue(
        crypt instanceof VampireStructure vampire
            && vampire.footing().isPresent()
            && vampire
                .footing()
                .get()
                .fits(generation, churchyard, new ColumnPos(origin.getX(), origin.getZ())),
        "the crypt footing accepts the flat dry test ground");
    context.complete();
  }

  private static StructurePlacement decodePlacement(final ServerWorld world) {
    return decodePlacement(world, 40, 12).getOrThrow();
  }

  private static DataResult<StructurePlacement> decodePlacement(
      final ServerWorld world, final int spacing, final int separation) {
    final JsonElement json =
        JsonParser.parseString(
            """
            { "type": "nightborn:rare_spread", "rarity": "vampire_structures", "salt": 1975, "spacing": %d, "separation": %d }
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
    final PlacedStructure placed = PlacedStructure.place(world, structure, heart);
    final StructureStart start = placed.start();
    try {
      final ServerPlayerEntity inside = context.createMockCreativeServerPlayerInWorld();
      final ServerPlayerEntity outside = context.createMockCreativeServerPlayerInWorld();
      final FogZone expected = FogZone.around(heart, start.getBoundingBox());
      inside.refreshPositionAndAngles(
          centre.getX() + 0.5, centre.getY(), centre.getZ() + 0.5, 0, 0);
      outside.refreshPositionAndAngles(
          centre.getX() + 0.5, centre.getY() + expected.reach() + 4, centre.getZ() + 0.5, 0, 0);
      final Optional<StructureZone> insideZone = StructureZoneTracker.zoneAround(inside);
      context.assertTrue(
          insideZone.map(StructureZone::area).equals(Optional.of(expected)),
          "fog centres on the heart, got " + insideZone);
      context.assertTrue(
          insideZone
                  .map(StructureZone::ambience)
                  .equals(Optional.of(((VampireStructure) structure).ambience()))
              && insideZone.get().ambience().loop().isPresent(),
          "the zone carries the structure's ambience, got " + insideZone);
      context.assertTrue(
          StructureZoneTracker.zoneAround(outside).isEmpty(),
          "a second player beyond the structure gets no fog while the first is inside");
      context.assertTrue(
          StructureZoneTracker.zoneAround(inside)
              .map(StructureZone::area)
              .equals(Optional.of(expected)),
          "the first player keeps their fog whatever the second player sees");
    } finally {
      placed.remove();
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void onlySteppingInsideAStructureEarnsHouseCall(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final Structure structure = decodeStructure(world, 64).getOrThrow();
    final BlockBox heart = arena(context);
    final BlockPos centre = heart.getCenter();
    final PlacedStructure placed = PlacedStructure.place(world, structure, heart);
    final StructureStart start = placed.start();
    final ServerPlayerEntity inside = TestPlayers.human(context, new BlockPos(4, 2, 4));
    final ServerPlayerEntity nearby = TestPlayers.human(context, new BlockPos(4, 2, 4));
    final BlockBox whole = start.getBoundingBox();
    nearby.refreshPositionAndAngles(
        whole.getMaxX() + 9.5, centre.getY(), centre.getZ() + 0.5, 0.0F, 0.0F);
    context.runAtTick(
        30,
        () -> {
          try {
            context.assertTrue(
                StructureZoneTracker.zoneAround(nearby).isPresent(),
                "the player beside the structure is in its fog");
            context.assertTrue(
                TestAdvancements.done(context, inside, "house_call"),
                "stepping inside earns House Call");
            context.assertFalse(
                TestAdvancements.done(context, nearby, "house_call"),
                "standing in the fog outside does not");
          } finally {
            placed.remove();
          }
          TestPlayers.finish(context, inside, nearby);
        });
  }
}
