package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.guild.build.HunterBuilds;
import com.grahambartley.nightborn.guild.build.HunterSkills;
import com.grahambartley.nightborn.guild.party.HunterParty;
import com.grahambartley.nightborn.guild.party.PartyLeader;
import com.grahambartley.nightborn.mob.MobSounds;
import com.grahambartley.nightborn.mob.ShownAction;
import com.grahambartley.nightborn.mob.combat.MobFighter;
import com.grahambartley.nightborn.mob.combat.MobFighting;
import com.grahambartley.nightborn.mob.combat.PowerCasting;
import com.grahambartley.nightborn.name.MobNaming;
import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.rank.GuildRanks;
import com.grahambartley.nightborn.rank.RankBearer;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.skill.SkillHolder;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.wheel.CommandWheel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.StopFollowingCustomerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GuildMasterEntity extends MerchantEntity
    implements GeoEntity,
        MobFighter,
        HunterLooking,
        NameBearer,
        SkillHolder,
        RankBearer,
        GuildFighter,
        PowerShow,
        RangedAttackMob {

  static final String LAST_RESTOCK = "LastRestock";
  static final String OFFER_RANKS = "OfferRanks";
  static final int HEAD_SHAKE_TICKS = 40;
  static final int ANIMATION_BLEND_TICKS = 4;
  static final int TENT_REACH = 3;
  static final float LOOK_TURN = 30.0F;
  static final String PARTY_KEY = "Party";
  static final String KIT_KEY = "Kit";
  static final double CHARGE_SPEED = 0.7;
  @Nullable private HunterParty party;

  private static final TrackedData<Boolean> TRADING =
      DataTracker.registerData(GuildMasterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<String> LOOK =
      DataTracker.registerData(GuildMasterEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Boolean> SWEARING =
      DataTracker.registerData(GuildMasterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<String> RANK =
      DataTracker.registerData(GuildMasterEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Integer> ACTION =
      DataTracker.registerData(GuildMasterEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Boolean> ARMED =
      DataTracker.registerData(GuildMasterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<String> POWER =
      DataTracker.registerData(GuildMasterEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Integer> POWER_SEQUENCE =
      DataTracker.registerData(GuildMasterEntity.class, TrackedDataHandlerRegistry.INTEGER);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final WornLook worn = new WornLook();
  private final HunterTactics<GuildMasterEntity> tactics = new HunterTactics<>(this);
  private final MobFighting fighting =
      MobFighting.movingFirst(PowerCasting.underTell(), this::tickTactics);
  private final ShownHunterAction shown = new ShownHunterAction();
  private final ShownAction shownPower = new ShownAction();
  private final StallWatch stall = new StallWatch();
  private WanderAroundFarGoal wander;
  private String kitId = "";
  @Nullable private HunterKit kit;
  private long lastRestock;
  private List<Integer> offerRanks = new ArrayList<>();
  private int swearTicks;
  @Nullable private ServerPlayerEntity sworn;

  public GuildMasterEntity(final EntityType<? extends MerchantEntity> type, final World world) {
    super(type, world);
    setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
    setEquipmentDropChance(EquipmentSlot.OFFHAND, 0.0F);
  }

  public static DefaultAttributeContainer.Builder createGuildMasterAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
        .add(EntityAttributes.GENERIC_ARMOR, 4.0)
        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(TRADING, false);
    builder.add(LOOK, "");
    builder.add(SWEARING, false);
    builder.add(RANK, "");
    builder.add(ACTION, HunterAction.NONE.ordinal());
    builder.add(ARMED, false);
    builder.add(POWER, "");
    builder.add(POWER_SEQUENCE, 0);
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new SwimGoal(this));
    goalSelector.add(1, new StopFollowingCustomerGoal(this));
    goalSelector.add(1, new LookAtCustomerGoal(this));
    goalSelector.add(2, new CrossbowGoal(this, this::standsOff));
    goalSelector.add(3, new MeleeAttackGoal(this, CHARGE_SPEED, true));
    goalSelector.add(7, new GoToWalkTargetGoal(this, 0.4));
    wander = new WanderAroundFarGoal(this, 0.35);
    goalSelector.add(8, wander);
    goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
    goalSelector.add(10, new LookAroundGoal(this));
    targetSelector.add(0, new KnownFaceTargetGoal(this));
    targetSelector.add(1, new HunterTargetGoal(this));
    targetSelector.add(2, new GuildRevengeGoal(this));
  }

  @Override
  public boolean canTarget(final LivingEntity target) {
    return GuildFoes.fights(this, target) && super.canTarget(target);
  }

  @Override
  public boolean foeOf(final LivingEntity target) {
    return GuildFoes.hunts(target);
  }

  @Override
  public void setTarget(@Nullable final LivingEntity target) {
    super.setTarget(target);
    if (MasterDuty.breaksOath(swearing(), target != null)) {
      swearTicks = 0;
      endOath();
    }
  }

  @Override
  public boolean isInWalkTargetRange(final BlockPos pos) {
    return CampHome.reaches(
        getPositionTargetRange(),
        getPositionTarget().getSquaredDistance(pos),
        HunterEntity.CAMP_REACH,
        getTarget() != null || getAttacker() != null);
  }

  private boolean swearing() {
    return swearTicks > 0;
  }

  @Override
  public Optional<GuildMasterEntity> master() {
    return Optional.of(this);
  }

  @Override
  @Nullable
  public HunterKit carriedKit() {
    return kit;
  }

  @Override
  public boolean armed() {
    return dataTracker.get(ARMED);
  }

  @Override
  public void setArmed(final boolean armed) {
    dataTracker.set(ARMED, armed);
  }

  @Override
  public void show(final HunterAction action) {
    dataTracker.set(ACTION, action.ordinal());
  }

  public HunterAction action() {
    return HunterAction.byIndex(dataTracker.get(ACTION));
  }

  @Override
  public void speak(final HunterVoice voice) {
    playSound(MobSounds.hunter(rig().figure(), voice), getSoundVolume(), getSoundPitch());
  }

  @Override
  public void showPower(final String clip) {
    dataTracker.set(POWER, clip);
    dataTracker.set(POWER_SEQUENCE, dataTracker.get(POWER_SEQUENCE) + 1);
  }

  @Override
  public void shootAt(final LivingEntity target, final float pullProgress) {
    tactics.shoot();
  }

  public boolean standsOff() {
    return kit != null && kit.ranged();
  }

  @Override
  public boolean tryAttack(final Entity target) {
    return HunterCries.strike(this, super.tryAttack(target));
  }

  @Override
  public boolean onKilledOther(final ServerWorld world, final LivingEntity other) {
    HunterCries.killed(this, other);
    return super.onKilledOther(world, other);
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    fight();
    stall.watch(this, wander);
  }

  @Override
  public float getPathfindingFavor(final BlockPos pos, final WorldView world) {
    return Footing.favor(this, pos, world);
  }

  public void fight() {
    if (!swearing()) {
      fighting.tick(
          this,
          MobFighting.Engagement.FIGHTING,
          HunterSkills.thinkTicks(this, shownRank()),
          MobFighting.NO_BLOOD_FLOOR);
    }
  }

  private void tickTactics() {
    if (kit != null) {
      tactics.tick(kit);
    }
  }

  @Override
  public MobFighting mobFighting() {
    return fighting;
  }

  private void arm(final String wanted) {
    final var kits = GuildData.kits(getRegistryManager());
    kitId =
        Optional.ofNullable(Identifier.tryParse(wanted))
            .filter(kits::containsKey)
            .or(
                () ->
                    HunterBuilds.build(getRegistryManager())
                        .masterKit(kits.keySet(), getRandom().nextDouble()))
            .map(Identifier::toString)
            .orElse("");
    kit = kits.get(Identifier.tryParse(kitId));
  }

  @Override
  public ActionResult interactMob(final PlayerEntity player, final Hand hand) {
    if (!isAlive() || hasCustomer()) {
      return super.interactMob(player, hand);
    }
    if (player instanceof ServerPlayerEntity visitor) {
      return visit(visitor, hand);
    }
    return ActionResult.success(getWorld().isClient);
  }

  private ActionResult visit(final ServerPlayerEntity visitor, final Hand hand) {
    final Optional<ItemStack> claim =
        Stream.of(visitor.getMainHandStack(), visitor.getOffHandStack())
            .filter(stack -> ContractItems.finished(stack, getRegistryManager()).isPresent())
            .findFirst();
    switch (GuildVisit.of(Vampires.isVampire(visitor), claim.isPresent())) {
      case REFUSE -> {
        refuse(visitor);
        return ActionResult.CONSUME;
      }
      case CLAIM -> {
        claim.ifPresent(
            stack ->
                ContractItems.finished(stack, getRegistryManager())
                    .ifPresent(
                        contract ->
                            ContractPayout.pay(
                                visitor, stack, contract, ServerConfigService.current().skills())));
        return ActionResult.CONSUME;
      }
      default -> {
        return CommandWheel.use(visitor, hand, this);
      }
    }
  }

  private void refuse(final ServerPlayerEntity visitor) {
    setHeadRollingTimeLeft(HEAD_SHAKE_TICKS);
    playSound(SoundEvents.ENTITY_VILLAGER_NO, getSoundVolume(), getSoundPitch());
    visitor.sendMessage(Text.translatable("nightborn.guild.refuse"), true);
  }

  void openCounter(final ServerPlayerEntity visitor, final GuildCounter.Kind kind) {
    if (hasCustomer()) {
      return;
    }
    restockIfNewDay();
    GuildTill.open(
        new GuildCounter(this, kind.from(getOffers(), offerRanks, GuildRanks.of(visitor))),
        visitor,
        getDisplayName(),
        Text.translatable("nightborn.guild.nothing"));
  }

  private void restockIfNewDay() {
    final long day = GuildRestock.day(getWorld().getTimeOfDay());
    if (offers == null
        || offers.isEmpty()
        || offers.size() != offerRanks.size()
        || GuildRestock.due(lastRestock, day)) {
      offers = null;
      getOffers();
      lastRestock = day;
    }
  }

  @Override
  public void tick() {
    super.tick();
    if (getWorld().isClient()) {
      return;
    }
    if (!hasPositionTarget()) {
      wear(dataTracker.get(LOOK));
      arm(kitId);
    }
    CampHome.keep(this, TENT_REACH);
    if (!HunterSkills.built(this)) {
      HunterSkills.master(this);
    }
    if (shownRank().isEmpty()) {
      dataTracker.set(RANK, RankBearer.write(HunterSkills.rank(this)));
    }
    MobNaming.ensure(this);
    if (party != null && age % PartyLeader.TICKS == 0 && getWorld() instanceof ServerWorld world) {
      party = PartyLeader.tick(world, this, party);
    }
    final PlayerEntity customer = getCustomer();
    if (customer != null
        && MasterDuty.closesCounter(Vampires.isVampire(customer), getTarget() != null)) {
      resetCustomer();
      if (customer instanceof ServerPlayerEntity player) {
        player.closeHandledScreen();
      }
    }
    dataTracker.set(TRADING, hasCustomer());
    keepSwearing();
  }

  @Override
  public SkillTree skillTree() {
    return SkillTree.HUNTER;
  }

  @Override
  public Optional<Identifier> shownRank() {
    return RankBearer.read(dataTracker.get(RANK));
  }

  void swearIn(final ServerPlayerEntity player, final int ticks) {
    swearTicks = ticks;
    sworn = player;
    setTarget(null);
    tactics.sheathe();
    equipStack(EquipmentSlot.OFFHAND, new ItemStack(GearItems.GOLD_CRUCIFIX));
    playSound(SoundEvents.ITEM_ARMOR_EQUIP_GOLD.value(), getSoundVolume(), 1.0F);
    keepSwearing();
  }

  private void keepSwearing() {
    if (swearTicks > 0) {
      swearTicks--;
      if (sworn != null && !sworn.isRemoved()) {
        getLookControl().lookAt(sworn, LOOK_TURN, LOOK_TURN);
      }
      if (swearTicks == 0) {
        endOath();
      }
    }
    dataTracker.set(SWEARING, swearTicks > 0);
  }

  private void endOath() {
    sworn = null;
    lowerCrucifix();
    dataTracker.set(SWEARING, false);
  }

  private void lowerCrucifix() {
    if (getOffHandStack().isOf(GearItems.GOLD_CRUCIFIX)) {
      equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
    }
  }

  @Override
  protected void fillRecipes() {
    offerRanks = new ArrayList<>();
    if (getWorld() instanceof ServerWorld world) {
      final RankLadder ladder = RankLadder.of(world.getRegistryManager());
      for (final GuildOffer offer : GuildData.offers(world.getRegistryManager())) {
        offers.add(offer.toTradeOffer());
        offerRanks.add(offer.needs(ladder));
      }
    }
  }

  @Override
  protected void afterUsing(final TradeOffer offer) {
    playSound(SoundEvents.ENTITY_VILLAGER_YES, getSoundVolume(), getSoundPitch());
    if (offer.getSellItem().isOf(ContractItems.HUNTER_CONTRACT)
        && getCustomer() instanceof ServerPlayerEntity hunter
        && getWorld() instanceof ServerWorld world) {
      ContractMaps.give(world, this, hunter);
    }
  }

  private void wear(final String look) {
    dataTracker.set(LOOK, GuildData.wornLook(look, getRegistryManager(), rig(), getRandom()));
  }

  @Override
  public HunterRig rig() {
    return HunterRig.GUILD_MASTER;
  }

  @Override
  public Identifier texture() {
    return worn.texture(
        rig(), dataTracker.get(LOOK), look -> GuildData.texture(getRegistryManager(), look, rig()));
  }

  @Override
  public boolean isLeveledMerchant() {
    return false;
  }

  @Override
  public SoundEvent getYesSound() {
    return SoundEvents.ENTITY_VILLAGER_YES;
  }

  @Override
  protected SoundEvent getTradingSound(final boolean sold) {
    return sold ? SoundEvents.ENTITY_VILLAGER_YES : SoundEvents.ENTITY_VILLAGER_NO;
  }

  @Override
  @Nullable
  protected SoundEvent getAmbientSound() {
    return hasCustomer()
        ? SoundEvents.ENTITY_VILLAGER_TRADE
        : MobSounds.hunter(rig().figure(), HunterVoice.AMBIENT);
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    return MobSounds.hunter(rig().figure(), HunterVoice.HURT);
  }

  @Override
  protected SoundEvent getDeathSound() {
    return MobSounds.hunter(rig().figure(), HunterVoice.DEATH);
  }

  @Override
  @Nullable
  public PassiveEntity createChild(final ServerWorld world, final PassiveEntity entity) {
    return null;
  }

  @Override
  public boolean cannotDespawn() {
    return true;
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putLong(LAST_RESTOCK, lastRestock);
    nbt.putIntArray(OFFER_RANKS, offerRanks);
    nbt.putString(GuildData.LOOK_KEY, dataTracker.get(LOOK));
    nbt.putString(KIT_KEY, kitId);
    CampHome.write(this, nbt);
    if (party != null) {
      HunterParty.CODEC
          .encodeStart(NbtOps.INSTANCE, party)
          .result()
          .ifPresent(saved -> nbt.put(PARTY_KEY, saved));
    }
  }

  @Nullable
  public HunterParty party() {
    return party;
  }

  public void lead(final HunterParty led) {
    party = led;
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    lastRestock = nbt.getLong(LAST_RESTOCK);
    offerRanks = new ArrayList<>(Arrays.stream(nbt.getIntArray(OFFER_RANKS)).boxed().toList());
    if (!getWorld().isClient()) {
      equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      wear(nbt.getString(GuildData.LOOK_KEY));
      arm(nbt.getString(KIT_KEY));
    }
    CampHome.read(this, nbt, TENT_REACH);
    party =
        nbt.contains(PARTY_KEY)
            ? HunterParty.CODEC.parse(NbtOps.INSTANCE, nbt.get(PARTY_KEY)).result().orElse(null)
            : null;
    lowerCrucifix();
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "pose",
            ANIMATION_BLEND_TICKS,
            state ->
                state.setAndContinue(
                    HunterPose.of(
                            handSwinging,
                            dataTracker.get(TRADING),
                            isSprinting() || isAttacking(),
                            state.getLimbSwingAmount(),
                            armed())
                        .clip())));
    controllers.add(
        new AnimationController<>(
            this,
            "action",
            ANIMATION_BLEND_TICKS,
            state ->
                shown.play(
                    state, dataTracker.get(SWEARING) ? HunterAction.RAISE_CRUCIFIX : action())));
    controllers.add(
        new AnimationController<>(
            this,
            "power",
            ANIMATION_BLEND_TICKS,
            state ->
                shownPower.play(state, dataTracker.get(POWER_SEQUENCE), dataTracker.get(POWER))));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
