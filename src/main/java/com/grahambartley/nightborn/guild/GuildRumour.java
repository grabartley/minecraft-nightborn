package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.servant.job.Bearing;
import com.grahambartley.nightborn.servant.job.ScoutChoices;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.NearestStructures;
import com.grahambartley.nightborn.structure.NearestStructures.Found;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import java.util.Optional;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

public final class GuildRumour implements WheelAction<GuildMasterEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "guild_rumour");
  public static final Identifier ICON = Identifier.ofVanilla("compass");
  public static final TagKey<Structure> VAMPIRE_HOUSES =
      TagKey.of(RegistryKeys.STRUCTURE, Identifier.of(Nightborn.MOD_ID, "vampire_houses"));

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return !Vampires.isVampire(player);
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return List.of(WheelEntry.of(ID, ICON, Text.translatable("nightborn.guild.rumour"), false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final GuildMasterEntity master, final WheelEntry chosen) {
    if (!(master.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final BlockPos from = master.getBlockPos();
    final int range = ServerConfigService.current().structures().rumourRange();
    final Optional<Found> nearest =
        ScoutChoices.nearest(
                from, NearestStructures.eachOf(world, VAMPIRE_HOUSES, from, range), range, 1)
            .stream()
            .findFirst();
    master.playSound(SoundEvents.ENTITY_VILLAGER_AMBIENT, 1.0F, master.getSoundPitch());
    player.sendMessage(tale(master.getDisplayName(), from, nearest), false);
  }

  static Text tale(final Text teller, final BlockPos from, final Optional<Found> nearest) {
    return nearest
        .<Text>map(
            house -> {
              final double dx = house.pos().getX() - from.getX();
              final double dz = house.pos().getZ() - from.getZ();
              return Text.translatable(
                  "nightborn.guild.rumour.told",
                  teller,
                  (int) Math.round(Math.sqrt(dx * dx + dz * dz)),
                  Text.translatable(Bearing.of(dx, dz).translationKey()));
            })
        .orElseGet(() -> Text.translatable("nightborn.guild.rumour.none", teller));
  }
}
