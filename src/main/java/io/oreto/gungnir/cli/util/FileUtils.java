package io.oreto.gungnir.cli.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static void copyDirectory(Path sourceDir, Path destinationDir)
            throws IOException {
        int len = sourceDir.toString().length();
        try(Stream<Path> pathStream = Files.walk(sourceDir)) {
            for (Path source : pathStream.toList()) {
                Path destination = Paths.get(destinationDir.toString(), source.toString().substring(len));
                Files.copy(source, destination);
            }
        }
    }

    public static void packageDirectory(Path srcDirectory) throws IOException {
        String src = srcDirectory.toString();
        try(Stream<Path> pathStream = Files.walk(srcDirectory)) {
            for (Path source : pathStream.toList()) {
                if (Files.isRegularFile(source) && source.toString().endsWith(".java")) {
                    Str str = Str.of(Files.readString(source));
                    String packageName = Arrays.stream(source.getParent().toString().replace(src, "")
                            .split("[\\\\/]"))
                            .filter(it -> !it.isEmpty())
                            .collect(Collectors.joining("."));
                    str.replaceFrom("package ", ";", false, "package " + packageName);
                    Files.write(source, str.getBytes());
                }
            }
        }
    }

    public static void deleteDirectory(Path directory) throws IOException {
        try(Stream<Path> stream = Files.walk(directory)) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
