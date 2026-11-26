package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.batform.WingedFormPower;
import com.grahambartley.nightborn.faith.PowerFaith;
import com.grahambartley.nightborn.lord.RallyPower;
import com.grahambartley.nightborn.lord.SummonPower;
import com.grahambartley.nightborn.mind.CommandPower;
import com.grahambartley.nightborn.mind.DreadPower;
import com.grahambartley.nightborn.mind.GlamourPower;
import com.grahambartley.nightborn.mind.MassGazePower;
import com.grahambartley.nightborn.occult.CirclePower;
import com.grahambartley.nightborn.occult.FamiliarPower;
import com.grahambartley.nightborn.occult.FogPower;
import com.grahambartley.nightborn.occult.HexPower;
import com.grahambartley.nightborn.occult.MistPower;
import com.grahambartley.nightborn.occult.ShadowStepPower;
import com.grahambartley.nightborn.occult.SwarmPower;
import com.grahambartley.nightborn.seeker.PowerSeek;
import com.grahambartley.nightborn.slayer.DashPower;
import com.grahambartley.nightborn.slayer.LungePower;
import com.grahambartley.nightborn.slayer.PowerFight;
import com.grahambartley.nightborn.trap.BaitPower;
import com.grahambartley.nightborn.trap.FieldPower;
import com.grahambartley.nightborn.trap.NetPower;
import com.grahambartley.nightborn.trap.TrapPower;
import com.grahambartley.nightborn.vanish.VanishPower;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.effect.StatusEffectInstance;

public record SkillPower(
    int blood,
    int cooldownTicks,
    List<StatusEffectInstance> effects,
    Optional<PowerSound> sound,
    PowerMoves moves,
    PowerMinds minds,
    PowerCalls calls,
    PowerSelf self,
    PowerTraps traps,
    PowerFaith faith,
    PowerFight fight,
    PowerSeek seek,
    Optional<MobPowerUse> mob,
    Optional<String> clip) {

  public static final Codec<SkillPower> CODEC =
      RecordCodecBuilder.<SkillPower>create(
              instance ->
                  instance
                      .group(
                          Codec.intRange(0, 20).fieldOf("blood").forGetter(SkillPower::blood),
                          Codec.intRange(0, 72000)
                              .fieldOf("cooldown_ticks")
                              .forGetter(SkillPower::cooldownTicks),
                          StatusEffectInstance.CODEC
                              .listOf()
                              .optionalFieldOf("effects", List.of())
                              .forGetter(SkillPower::effects),
                          PowerSound.CODEC.optionalFieldOf("sound").forGetter(SkillPower::sound),
                          PowerMoves.CODEC.forGetter(SkillPower::moves),
                          PowerMinds.CODEC.forGetter(SkillPower::minds),
                          PowerCalls.CODEC.forGetter(SkillPower::calls),
                          PowerSelf.CODEC.forGetter(SkillPower::self),
                          PowerTraps.CODEC.forGetter(SkillPower::traps),
                          PowerFaith.CODEC.forGetter(SkillPower::faith),
                          PowerFight.CODEC.forGetter(SkillPower::fight),
                          PowerSeek.CODEC.forGetter(SkillPower::seek),
                          MobPowerUse.CODEC.optionalFieldOf("mob").forGetter(SkillPower::mob),
                          Codec.STRING.optionalFieldOf("clip").forGetter(SkillPower::clip))
                      .apply(instance, SkillPower::new))
          .validate(SkillPower::oneMove);

  public SkillPower {
    effects = List.copyOf(effects);
  }

  public Optional<BatFormPower> batForm() {
    return moves.batForm();
  }

  public Optional<LeapPower> leap() {
    return moves.leap();
  }

  public Optional<ShadowStepPower> shadowStep() {
    return moves.shadowStep();
  }

  public Optional<VanishPower> vanish() {
    return moves.vanish();
  }

  public Optional<PouncePower> pounce() {
    return moves.pounce();
  }

  public Optional<WingedFormPower> wingedForm() {
    return moves.wingedForm();
  }

  public Optional<MistPower> mist() {
    return moves.mist();
  }

  public Optional<DashPower> dash() {
    return moves.dash();
  }

  public Optional<LungePower> lunge() {
    return moves.lunge();
  }

  public Optional<MesmerisePower> mesmerise() {
    return minds.mesmerise();
  }

  public Optional<MassGazePower> massGaze() {
    return minds.massGaze();
  }

  public Optional<CommandPower> command() {
    return minds.command();
  }

  public Optional<DreadPower> dread() {
    return minds.dread();
  }

  public Optional<GlamourPower> glamour() {
    return minds.glamour();
  }

  public Optional<HexPower> hex() {
    return minds.hex();
  }

  public Optional<FamiliarPower> familiar() {
    return calls.familiar();
  }

  public Optional<SummonPower> summon() {
    return calls.summon();
  }

  public Optional<RallyPower> rally() {
    return calls.rally();
  }

  public Optional<FogPower> fog() {
    return calls.fog();
  }

  public Optional<CirclePower> circle() {
    return calls.circle();
  }

  public Optional<SwarmPower> swarm() {
    return calls.swarm();
  }

  public Optional<TrackPower> track() {
    return self.track();
  }

  public Optional<HealPower> heal() {
    return self.heal();
  }

  public Optional<SensePower> sense() {
    return self.sense();
  }

  public Optional<BoostPower> boost() {
    return self.boost();
  }

  public Optional<FreePower> frees() {
    return self.frees();
  }

  public Optional<TrapPower> trap() {
    return traps.trap();
  }

  public Optional<NetPower> net() {
    return traps.net();
  }

  public Optional<BaitPower> bait() {
    return traps.bait();
  }

  public Optional<FieldPower> field() {
    return traps.field();
  }

  public boolean leavesTheGround() {
    return batForm().isPresent()
        || wingedForm().isPresent()
        || leap().isPresent()
        || pounce().isPresent();
  }

  private static DataResult<SkillPower> oneMove(final SkillPower power) {
    return power.moves().count() > 1
        ? DataResult.error(() -> "A power moves or changes its caster in one way only")
        : DataResult.success(power);
  }
}
