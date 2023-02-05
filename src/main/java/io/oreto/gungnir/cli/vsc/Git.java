package io.oreto.gungnir.cli.vsc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Git {

    public static File cloneGh(String project, Path path) throws InterruptedException, IOException {
        String repo = String.format("https://github.com/ross-oreto/%s", project);
        String command = String.format("git clone %s %s", repo, path);
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        if (p.exitValue() == 0 && Files.exists(path))
            return path.toFile();
        else {
            printProcess(p);
            throw new IOException("error creating application");
        }
    }

    private static void printProcess(Process process) throws IOException {
        System.out.println(Arrays.toString(process.getInputStream().readAllBytes()));
        System.out.println(Arrays.toString(process.getErrorStream().readAllBytes()));
    }
}
