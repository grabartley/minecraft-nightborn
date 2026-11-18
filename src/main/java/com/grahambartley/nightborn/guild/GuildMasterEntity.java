package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.guild.party.HunterParty;
import com.grahambartley.nightborn.guild.party.PartyLeader;
import com.grahambartley.nightborn.mob.MobSounds;
import com.grahambartley.nightborn.name.MobNaming;
import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.wheel.CommandWheel;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
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
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GuildMasterEntity extends MerchantEntity
    implements GeoEntity, HunterLooking, NameBearer {

  static final String LAST_RESTOCK = "LastRestock";
  static final int HEAD_SHAKE_TICKS = 40;
  static final int ANIMATION_BLEND_TICKS = 4;
  static final int TENT_REACH = 3;
  static final String PARTY_KEY = "Party";
  @Nullable private HunterParty party;

  private static final TrackedData<Boolean> TRADING =
      DataTracker.registerData(GuildMasterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<String> LOOK =
      DataTracker.registerData(GuildMasterEntity.class, TrackedDataHandlerRegistry.STRING);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final WornLook worn = new WornLook();
  private long lastRestock;

  public GuildMasterEntity(final EntityType<? extends MerchantEntity> type, final World world) {
    super(type, world);
  }

  public static DefaultAttributeContainer.Builder createGuildMasterAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5);
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(TRADING, false);
    builder.add(LOOK, "");
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new SwimGoal(this));
    goalSelector.add(1, new StopFollowingCustomerGoal(this));
    goalSelector.add(1, new LookAtCustomerGoal(this));
    goalSelector.add(7, new GoToWalkTargetGoal(this, 0.4));
    goalSelector.add(8, new WanderAroundFarGoal(this, 0.35));
    goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
    goalSelector.add(10, new LookAroundGoal(this));
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
    final GuildCounter counter = new GuildCounter(this, kind.from(getOffers()));
    if (counter.getOffers().isEmpty()) {
      visitor.sendMessage(Text.translatable("nightborn.guild.nothing"), true);
      return;
    }
    counter.setCustomer(visitor);
    counter.sendOffers(visitor, getDisplayName(), 1);
  }

  private void restockIfNewDay() {
    final long day = GuildRestock.day(getWorld().getTimeOfDay());
    if (offers == null || offers.isEmpty() || GuildRestock.due(lastRestock, day)) {
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
    }
    CampHome.keep(this, TENT_REACH);
    MobNaming.ensure(this);
    if (party != null && age % PartyLeader.TICKS == 0 && getWorld() instanceof ServerWorld world) {
      party = PartyLeader.tick(world, this, party);
    }
    final PlayerEntity customer = getCustomer();
    if (customer != null && Vampires.isVampire(customer)) {
      resetCustomer();
      if (customer instanceof ServerPlayerEntity player) {
        player.closeHandledScreen();
      }
    }
    dataTracker.set(TRADING, hasCustomer());
  }

  @Override
  protected void fillRecipes() {
    if (getWorld() instanceof ServerWorld world) {
      GuildData.offers(world.getRegistryManager()).stream()
          .map(GuildOffer::toTradeOffer)
          .forEach(offers::add);
    }
  }

  @Override
  protected void afterUsing(final TradeOffer offer) {
    playSound(SoundEvents.ENTITY_VILLAGER_YES, getSoundVolume(), getSoundPitch());
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
    nbt.putString(GuildData.LOOK_KEY, dataTracker.get(LOOK));
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
    if (!getWorld().isClient()) {
      wear(nbt.getString(GuildData.LOOK_KEY));
    }
    CampHome.read(this, nbt, TENT_REACH);
    party =
        nbt.contains(PARTY_KEY)
            ? HunterParty.CODEC.parse(NbtOps.INSTANCE, nbt.get(PARTY_KEY)).result().orElse(null)
            : null;
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
                            false,
                            state.getLimbSwingAmount(),
                            false)
                        .clip())));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
