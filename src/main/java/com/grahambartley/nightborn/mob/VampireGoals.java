package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.house.BringBloodHomeGoal;
import com.grahambartley.nightborn.house.MemberStance;
import com.grahambartley.nightborn.mob.goal.BatFlightGoal;
import com.grahambartley.nightborn.mob.goal.FleeWhileEscapingGoal;
import com.grahambartley.nightborn.mob.goal.FollowBaitGoal;
import com.grahambartley.nightborn.mob.goal.HouseholdLifeGoal;
import com.grahambartley.nightborn.mob.goal.LieDownedGoal;
import com.grahambartley.nightborn.mob.goal.RetreatGoal;
import com.grahambartley.nightborn.mob.goal.SeekShadeGoal;
import com.grahambartley.nightborn.mob.goal.SleepInCoffinGoal;
import com.grahambartley.nightborn.mob.goal.WatchStrangerGoal;
import com.grahambartley.nightborn.raid.HouseGuardGoal;
import com.grahambartley.nightborn.raid.HouseWatch;
import com.grahambartley.nightborn.servant.goal.StanceFollowGoal;
import com.grahambartley.nightborn.servant.goal.StancePostGoal;
import com.grahambartley.nightborn.servant.goal.StanceTargetGoal;
import com.grahambartley.nightborn.wild.WatchFromAfarGoal;
import com.grahambartley.nightborn.wild.WildTravelGoal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AvoidSunlightGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.player.PlayerEntity;

final class VampireGoals {

  private VampireGoals() {}

  record Escapes(RetreatGoal retreat, SeekShadeGoal shade) {

    boolean active() {
      return retreat.active() || shade.seeking();
    }
  }

  static Escapes addTo(
      final VampireEntity vampire, final GoalSelector goals, final GoalSelector targets) {
    goals.add(0, new LieDownedGoal(vampire));
    goals.add(0, new BatFlightGoal(vampire));
    goals.add(1, new SwimGoal(vampire));
    goals.add(1, new WatchFromAfarGoal(vampire));
    DoorPassage.allow(vampire);
    goals.add(1, new FleeWhileEscapingGoal(vampire, 1.4));
    final SeekShadeGoal shade = new SeekShadeGoal(vampire, 1.3);
    goals.add(2, shade);
    goals.add(3, new AvoidSunlightGoal(vampire));
    goals.add(3, new SleepInCoffinGoal(vampire));
    final RetreatGoal retreat =
        new RetreatGoal(vampire, () -> vampire.stage().combat().retreatHealth());
    goals.add(3, retreat);
    goals.add(4, new MeleeAttackGoal(vampire, 1.15, false));
    final MemberStance memberStance = new MemberStance(vampire);
    goals.add(4, new StancePostGoal(vampire, memberStance));
    goals.add(4, new StanceFollowGoal(vampire, memberStance));
    goals.add(5, new FollowBaitGoal(vampire));
    goals.add(5, new GoToWalkTargetGoal(vampire, 1.0));
    goals.add(6, new BringBloodHomeGoal(vampire));
    goals.add(6, new WatchStrangerGoal(vampire));
    goals.add(7, new HouseholdLifeGoal(vampire));
    goals.add(7, new WildTravelGoal(vampire));
    goals.add(8, new WanderAroundFarGoal(vampire, 0.8));
    goals.add(9, new LookAtEntityGoal(vampire, PlayerEntity.class, 8.0F));
    goals.add(9, new LookAroundGoal(vampire));
    targets.add(1, new RevengeGoal(vampire));
    targets.add(2, new StanceTargetGoal(vampire, memberStance));
    targets.add(
        2,
        new HouseGuardGoal(
            vampire,
            () -> vampire.homeLife().home(),
            () -> HouseWatch.onWatch(vampire),
            vampire::hiss));
    targets.add(
        2, new ActiveTargetGoal<>(vampire, LivingEntity.class, 10, true, false, vampire::hunts));
    return new Escapes(retreat, shade);
  }
}
