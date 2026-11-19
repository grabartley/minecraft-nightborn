package com.grahambartley.nightborn.guild.raiding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RaidingPartyTest {

  private static final UUID QUARRY = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
  private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
  private static final GlobalPos HOUSE = GlobalPos.create(World.OVERWORLD, new BlockPos(1, 70, 2));

  private static RaidingParty raid(final boolean housed, final boolean passing) {
    return new RaidingParty(
        QUARRY,
        List.of(A, B),
        2,
        37000L,
        housed ? Optional.of(HOUSE) : Optional.empty(),
        passing ? Optional.of(A) : Optional.empty());
  }

  @ParameterizedTest(name = "losing {0} leaves {1}")
  @CsvSource({
    "00000000-0000-0000-0000-00000000000a, 1",
    "00000000-0000-0000-0000-00000000000b, 1",
    "00000000-0000-0000-0000-00000000000c, 2"
  })
  void losesOnlyTheMemberWhoFell(final UUID fallen, final int left) {
    final RaidingParty after = raid(true, false).without(fallen);
    assertEquals(left, after.members().size());
    assertEquals(2, after.size());
  }

  @ParameterizedTest(name = "housed {0}, passing {1}")
  @CsvSource({"true, false", "false, false", "true, true"})
  void givesOrdersForItsHouseAndKnowsWhoLeadsIt(final boolean housed, final boolean passing) {
    final RaidingParty raid = raid(housed, passing);
    assertEquals(passing, raid.passing());
    assertEquals(
        new RaidOrders(QUARRY, 37000L, housed ? Optional.of(HOUSE.pos()) : Optional.empty()),
        raid.orders());
  }

  @ParameterizedTest(name = "housed {0}, passing {1}")
  @CsvSource({"true, false", "false, true"})
  void survivesASaveAndLoad(final boolean housed, final boolean passing) {
    final RaidingParty raid = raid(housed, passing);
    assertEquals(
        raid,
        RaidingParty.CODEC
            .parse(
                JsonOps.INSTANCE,
                RaidingParty.CODEC.encodeStart(JsonOps.INSTANCE, raid).getOrThrow())
            .getOrThrow());
  }
}
