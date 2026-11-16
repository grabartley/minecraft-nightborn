package com.grahambartley.nightborn.tools.structures;

import com.grahambartley.nightborn.tools.structures.camp.HunterCamp;
import com.grahambartley.nightborn.tools.structures.files.GeneratedSync;
import com.grahambartley.nightborn.tools.structures.files.OutputTree;
import com.grahambartley.nightborn.tools.structures.files.ShippedDiff;
import com.grahambartley.nightborn.tools.structures.template.TemplateSaver;
import com.grahambartley.nightborn.tools.structures.vampire.VampireStructures;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class StructureGenerator {

  static final String USAGE =
      "usage: StructureGenerator write <data root> <scratch folder>\n"
          + "       StructureGenerator check <data root> <scratch folder>";
  static final List<String> OWNED_FOLDERS = List.of("structure");

  private StructureGenerator() {}

  public static void main(final String[] args) {
    final List<String> problems = run(args);
    problems.forEach(System.err::println);
    if (!problems.isEmpty()) {
      System.exit(1);
    }
  }

  static List<String> run(final String[] args) {
    if (args.length == 3 && args[0].equals("write")) {
      return write(Path.of(args[1]), Path.of(args[2]));
    }
    if (args.length == 3 && args[0].equals("check")) {
      return check(Path.of(args[1]), Path.of(args[2]));
    }
    return List.of(USAGE);
  }

  static List<String> write(final Path root, final Path scratch) {
    final OutputTree out = generate(scratch);
    final GeneratedSync.Result result =
        GeneratedSync.apply(scratch, out.written(), root, OWNED_FOLDERS);
    System.out.println(summary(out.written().size(), result));
    return List.of();
  }

  static String summary(final int generated, final GeneratedSync.Result result) {
    return "Generated "
        + generated
        + " files, rewrote "
        + result.rewritten()
        + " and deleted "
        + result.deleted()
        + ".";
  }

  static List<String> check(final Path shipped, final Path scratch) {
    final OutputTree out = generate(scratch);
    final List<String> problems =
        ShippedDiff.differences(scratch, out.written(), shipped, OWNED_FOLDERS);
    if (problems.isEmpty()) {
      System.out.println("Generated files match what ships.");
    }
    return problems;
  }

  static OutputTree generate(final Path scratch) {
    clean(scratch);
    final OutputTree out = new OutputTree(scratch);
    TemplateSaver.save(HunterCamp.build(), out);
    VampireStructures.build().forEach(template -> TemplateSaver.save(template, out));
    return out;
  }

  private static void clean(final Path folder) {
    if (!Files.exists(folder)) {
      return;
    }
    try (Stream<Path> walk = Files.walk(folder)) {
      for (final Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
        Files.delete(path);
      }
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
