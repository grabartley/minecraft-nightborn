package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.name.Epithets;
import com.grahambartley.nightborn.name.NamePools;
import com.grahambartley.nightborn.name.NameStyle;
import com.grahambartley.nightborn.name.ServantNaming;
import com.grahambartley.nightborn.occult.FamiliarPower;
import com.grahambartley.nightborn.occult.GhostlyFamiliarEntity;
import com.grahambartley.nightborn.occult.GhostlyFamiliars;
import com.grahambartley.nightborn.thrall.ThrallEntity;
import com.grahambartley.nightborn.thrall.Thralls;
import com.grahambartley.nightborn.thrall.VillagerMemory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class NameGameTest implements FabricGameTest {

  static final String ARENA = VampireMobGameTest.ARENA;
  static final int NAMED = 3;
  static final Identifier ELDER = Identifier.of("nightborn", "elder");

  static boolean inPool(final TestContext context, final Identifier pool, final String name) {
    return NamePools.pool(context.getWorld().getRegistryManager(), pool).names().contains(name);
  }

  static boolean inFamiliarPools(final TestContext context, final String name) {
    return inPool(context, NameStyle.FAMILIAR.men(), name)
        || inPool(context, NameStyle.FAMILIAR.women(), name);
  }

  private static <T extends MobEntity> T still(final T mob) {
    mob.setAiDisabled(true);
    return mob;
  }

  private static VampireEntity vampire(final TestContext context, final BlockPos at) {
    return still(context.spawnEntity(NightbornMobs.VAMPIRE, at));
  }

  private static String given(final VampireEntity vampire) {
    return Epithets.given(vampire.getName().getString(), vampire.epithet()).orElseThrow();
  }

  @GameTest(templateName = ARENA, tickLimit = NAMED + 5)
  public void aSpawnedVampireTakesAGivenNameAndAnEpithet(final TestContext context) {
    final VampireEntity vampire = vampire(context, new BlockPos(3, 2, 3));
    context.runAtTick(
        NAMED,
        () -> {
          context.assertTrue(vampire.hasCustomName(), "the vampire has a name");
          context.assertTrue(vampire.isCustomNameVisible(), "the name is shown above it");
          context.assertTrue(
              inPool(
                  context,
                  vampire.bearsWomansName() ? NameStyle.VAMPIRE.women() : NameStyle.VAMPIRE.men(),
                  given(vampire)),
              "the given name comes from the vampire pool for its sex");
          context.assertTrue(
              inPool(context, NameStyle.VAMPIRE.epithets().orElseThrow(), vampire.epithet()),
              "the epithet comes from the 500");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = NAMED + 5)
  public void theEldestIsAlwaysTheEldest(final TestContext context) {
    final EldestEntity eldest =
        still(context.spawnEntity(NightbornMobs.ELDEST, new BlockPos(3, 2, 3)));
    context.runAtTick(
        NAMED,
        () -> {
          context.assertEquals(eldest.epithet(), "the Eldest", "the Eldest's epithet");
          context.assertTrue(
              eldest.getName().getString().endsWith(" the Eldest"),
              "the full name ends in the Eldest");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = NAMED + 5)
  public void housematesNeverShareAGivenNameOrAnEpithet(final TestContext context) {
    final List<VampireEntity> household =
        List.of(
            vampire(context, new BlockPos(2, 2, 2)),
            vampire(context, new BlockPos(4, 2, 2)),
            vampire(context, new BlockPos(2, 2, 4)),
            vampire(context, new BlockPos(4, 2, 4)));
    context.runAtTick(
        NAMED,
        () -> {
          final Set<String> givens =
              household.stream().map(NameGameTest::given).collect(Collectors.toSet());
          final Set<String> epithets =
              household.stream().map(VampireEntity::epithet).collect(Collectors.toSet());
          context.assertEquals(givens.size(), household.size(), "different given names");
          context.assertEquals(epithets.size(), household.size(), "different epithets");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = NAMED + 5)
  public void anElderShowsItsNameInGold(final TestContext context) {
    final VampireEntity vampire = vampire(context, new BlockPos(3, 2, 3));
    vampire.becomeStage(ELDER);
    context.runAtTick(
        NAMED,
        () -> {
          context.assertEquals(
              vampire.getDisplayName().getSiblings().getFirst().getStyle().getColor(),
              TextColor.fromFormatting(Formatting.GOLD),
              "an elder's name is gold");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = NAMED + 5)
  public void huntersAndGuildMastersAreNamedInBlueWithNoEpithet(final TestContext context) {
    final HunterEntity hunter = still(context.spawnEntity(GuildMobs.HUNTER, new BlockPos(2, 2, 3)));
    final GuildMasterEntity master =
        still(context.spawnEntity(GuildMobs.GUILD_MASTER, new BlockPos(4, 2, 3)));
    context.runAtTick(
        NAMED,
        () -> {
          for (final MobEntity mob : List.<MobEntity>of(hunter, master)) {
            final String name = mob.getName().getString();
            context.assertTrue(
                inPool(context, NameStyle.HUNTER.men(), name)
                    || inPool(context, NameStyle.HUNTER.women(), name),
                "a hunter's name is a plain given name from the hunter pools: " + name);
          }
          context.assertTrue(
              inPool(context, NameStyle.HUNTER.men(), master.getName().getString()),
              "the Guild Master draws from the men's pool");
          context.assertEquals(
              hunter.getDisplayName().getSiblings().getFirst().getStyle().getColor(),
              TextColor.fromFormatting(Formatting.BLUE),
              "a hunter's name is blue");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = NAMED + 5)
  public void aSelectorFindsAVampireByItsFullEnglishName(final TestContext context) {
    final VampireEntity vampire = vampire(context, new BlockPos(3, 2, 3));
    context.runAtTick(
        NAMED,
        () -> {
          final MinecraftServer server = context.getWorld().getServer();
          server
              .getCommandManager()
              .executeWithPrefix(
                  server.getCommandSource().withLevel(4).withSilent(),
                  "kill @e[name=\"" + vampire.getName().getString() + "\"]");
          context.assertFalse(vampire.isAlive(), "the selector found the vampire by name");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = NAMED + 5)
  public void aNameTagRenamesAVampireWholeAndTheColourStays(final TestContext context) {
    final VampireEntity vampire = vampire(context, new BlockPos(3, 2, 3));
    vampire.becomeStage(ELDER);
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    context.runAtTick(
        NAMED,
        () -> {
          final ItemStack tag = new ItemStack(Items.NAME_TAG, 2);
          tag.set(DataComponentTypes.CUSTOM_NAME, Text.literal("  Old Wren  "));
          context.assertEquals(
              tag.useOnEntity(player, vampire, Hand.MAIN_HAND),
              ActionResult.CONSUME,
              "the tag names the vampire");
          context.assertEquals(vampire.getName().getString(), "Old Wren", "the whole name");
          context.assertEquals(vampire.epithet(), "", "the epithet goes with the old name");
          context.assertEquals(
              vampire.getDisplayName().getSiblings().getFirst().getStyle().getColor(),
              TextColor.fromFormatting(Formatting.GOLD),
              "an elder keeps its gold name");
          context.assertEquals(tag.getCount(), 1, "a good tag is used up");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = NAMED + 5)
  public void aNameTagWithABadNameDoesNothingAndIsKept(final TestContext context) {
    final HunterEntity hunter = still(context.spawnEntity(GuildMobs.HUNTER, new BlockPos(3, 2, 3)));
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    context.runAtTick(
        NAMED,
        () -> {
          final String before = hunter.getName().getString();
          final ItemStack tag = new ItemStack(Items.NAME_TAG);
          tag.set(DataComponentTypes.CUSTOM_NAME, Text.literal("    "));
          context.assertEquals(
              tag.useOnEntity(player, hunter, Hand.MAIN_HAND), ActionResult.PASS, "refused");
          context.assertEquals(hunter.getName().getString(), before, "the name is unchanged");
          context.assertEquals(tag.getCount(), 1, "the tag is not used up");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = NAMED + 5)
  public void aNameAndEpithetSurviveAReload(final TestContext context) {
    final VampireEntity vampire = vampire(context, new BlockPos(3, 2, 3));
    context.runAtTick(
        NAMED,
        () -> {
          final NbtCompound saved = vampire.writeNbt(new NbtCompound());
          final VampireEntity loaded = NightbornMobs.VAMPIRE.create(context.getWorld());
          loaded.readNbt(saved);
          context.assertEquals(
              loaded.getName().getString(), vampire.getName().getString(), "the name loads");
          context.assertEquals(loaded.epithet(), vampire.epithet(), "the epithet loads");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = NAMED + 5)
  public void onlyTheOwnerNamesTheirThrall(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final ServerPlayerEntity stranger = context.createMockCreativeServerPlayerInWorld();
    final ThrallEntity thrall = still(context.spawnEntity(Thralls.THRALL, new BlockPos(3, 2, 3)));
    thrall.bind(owner.getUuid(), thrall.getVillagerData(), VillagerMemory.NONE);
    context.runAtTick(
        NAMED,
        () -> {
          final String suggested = thrall.getName().getString();
          context.assertTrue(inFamiliarPools(context, suggested), "a thrall is never nameless");
          context.assertFalse(
              ServantNaming.rename(stranger, thrall.getId(), "Hester"), "a stranger is refused");
          context.assertFalse(
              ServantNaming.rename(owner, thrall.getId(), "   "), "a blank name is refused");
          context.assertEquals(thrall.getName().getString(), suggested, "nothing changed");
          context.assertTrue(
              ServantNaming.rename(owner, thrall.getId(), "  Hester "), "the owner names it");
          context.assertEquals(thrall.getName().getString(), "Hester", "trimmed name taken");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = NAMED + 5)
  public void aSummonedGhostlyFamiliarTakesAFamiliarName(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final GhostlyFamiliarEntity ghost =
        context.spawnEntity(GhostlyFamiliars.GHOSTLY_FAMILIAR, new BlockPos(3, 2, 3));
    ghost.summon(owner.getUuid(), new FamiliarPower(200, 10.0F, 3.0F, 24.0F, 200, 8.0F, 200, 9));
    ghost.setAiDisabled(true);
    context.runAtTick(
        NAMED,
        () -> {
          context.assertTrue(
              inFamiliarPools(context, ghost.getName().getString()),
              "the ghost has a name from the familiar pools");
          context.complete();
        });
  }
}
