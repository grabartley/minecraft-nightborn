package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.mind.CommandPower;
import com.grahambartley.nightborn.mind.DreadPower;
import com.grahambartley.nightborn.mind.GlamourPower;
import com.grahambartley.nightborn.mind.MassGazePower;
import com.grahambartley.nightborn.occult.HexPower;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record PowerMinds(
    Optional<MesmerisePower> mesmerise,
    Optional<MassGazePower> massGaze,
    Optional<CommandPower> command,
    Optional<DreadPower> dread,
    Optional<GlamourPower> glamour,
    Optional<HexPower> hex) {

  public static final PowerMinds NONE =
      new PowerMinds(
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty());

  public static final MapCodec<PowerMinds> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      MesmerisePower.CODEC
                          .optionalFieldOf("mesmerise")
                          .forGetter(PowerMinds::mesmerise),
                      MassGazePower.CODEC
                          .optionalFieldOf("mass_gaze")
                          .forGetter(PowerMinds::massGaze),
                      CommandPower.CODEC.optionalFieldOf("command").forGetter(PowerMinds::command),
                      DreadPower.CODEC.optionalFieldOf("dread").forGetter(PowerMinds::dread),
                      GlamourPower.CODEC.optionalFieldOf("glamour").forGetter(PowerMinds::glamour),
                      HexPower.CODEC.optionalFieldOf("hex").forGetter(PowerMinds::hex))
                  .apply(instance, PowerMinds::new));
}
