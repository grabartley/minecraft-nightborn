package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.util.Identifier;
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
    int lingerEvery,
    ZoneHit hit,
    ZoneFan fan,
    Optional<Identifier> mark) {

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
                          .forGetter(ZoneAttack::lingerEvery),
                      ZoneHit.CODEC
                          .optionalFieldOf("hit", ZoneHit.PLAIN)
                          .forGetter(ZoneAttack::hit),
                      ZoneFan.CODEC
                          .optionalFieldOf("fan", ZoneFan.SINGLE)
                          .forGetter(ZoneAttack::fan),
                      Identifier.CODEC.optionalFieldOf("mark").forGetter(ZoneAttack::mark))
                  .apply(instance, ZoneAttack::new));

  public List<ZoneShape> fanned(
      final double selfX, final double selfZ, final double foeX, final double foeZ) {
    if (shape != Shape.LINE) {
      return List.of(at(selfX, selfZ, foeX, foeZ));
    }
    final double dx = foeX - selfX;
    final double dz = foeZ - selfZ;
    return IntStream.range(0, fan.count())
        .mapToObj(
            index -> {
              final double turn = fan.turn(index);
              final double cos = Math.cos(turn);
              final double sin = Math.sin(turn);
              return at(selfX, selfZ, selfX + dx * cos - dz * sin, selfZ + dx * sin + dz * cos);
            })
        .toList();
  }

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
