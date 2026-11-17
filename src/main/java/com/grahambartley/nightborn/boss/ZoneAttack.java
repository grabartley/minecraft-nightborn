package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringIdentifiable;

public record ZoneAttack(
    Shape shape,
    float radius,
    float length,
    Anchor anchor,
    Motion motion,
    float lift,
    float damage,
    float knock,
    int lingerTicks,
    int lingerEvery) {

  public enum Shape implements StringIdentifiable {
    CIRCLE("circle"),
    LINE("line");

    public static final Codec<Shape> CODEC = StringIdentifiable.createCodec(Shape::values);

    private final String id;

    Shape(final String id) {
      this.id = id;
    }

    @Override
    public String asString() {
      return id;
    }
  }

  public enum Anchor implements StringIdentifiable {
    FOE("foe"),
    SELF("self"),
    EVERY_FOE("every_foe");

    public static final Codec<Anchor> CODEC = StringIdentifiable.createCodec(Anchor::values);

    private final String id;

    Anchor(final String id) {
      this.id = id;
    }

    @Override
    public String asString() {
      return id;
    }
  }

  public enum Motion implements StringIdentifiable {
    STAY("stay"),
    LEAP("leap"),
    STEP("step");

    public static final Codec<Motion> CODEC = StringIdentifiable.createCodec(Motion::values);

    private final String id;

    Motion(final String id) {
      this.id = id;
    }

    @Override
    public String asString() {
      return id;
    }
  }

  public static final Codec<ZoneAttack> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Shape.CODEC
                          .optionalFieldOf("shape", Shape.CIRCLE)
                          .forGetter(ZoneAttack::shape),
                      Codec.floatRange(0.5F, 16.0F).fieldOf("radius").forGetter(ZoneAttack::radius),
                      Codec.floatRange(0.0F, 64.0F)
                          .optionalFieldOf("length", 0.0F)
                          .forGetter(ZoneAttack::length),
                      Anchor.CODEC
                          .optionalFieldOf("anchor", Anchor.FOE)
                          .forGetter(ZoneAttack::anchor),
                      Motion.CODEC
                          .optionalFieldOf("motion", Motion.STAY)
                          .forGetter(ZoneAttack::motion),
                      Codec.floatRange(0.0F, 16.0F)
                          .optionalFieldOf("lift", 0.0F)
                          .forGetter(ZoneAttack::lift),
                      Codec.floatRange(0.0F, 200.0F)
                          .fieldOf("damage")
                          .forGetter(ZoneAttack::damage),
                      Codec.floatRange(0.0F, 8.0F)
                          .optionalFieldOf("knock", 0.0F)
                          .forGetter(ZoneAttack::knock),
                      Codec.intRange(0, 1200)
                          .optionalFieldOf("linger_ticks", 0)
                          .forGetter(ZoneAttack::lingerTicks),
                      Codec.intRange(1, 200)
                          .optionalFieldOf("linger_every", 10)
                          .forGetter(ZoneAttack::lingerEvery))
                  .apply(instance, ZoneAttack::new));

  public ZoneShape at(
      final double selfX, final double selfZ, final double foeX, final double foeZ) {
    if (shape == Shape.LINE) {
      return ZoneShape.line(selfX, selfZ, foeX, foeZ, radius, length);
    }
    return anchor == Anchor.SELF
        ? ZoneShape.circle(selfX, selfZ, radius)
        : ZoneShape.circle(foeX, foeZ, radius);
  }

  public boolean lingers() {
    return lingerTicks > 0;
  }

  public boolean hurtsAt(final int lingerTick) {
    return lingerTick > 0 && lingerTick % lingerEvery == 0;
  }
}
