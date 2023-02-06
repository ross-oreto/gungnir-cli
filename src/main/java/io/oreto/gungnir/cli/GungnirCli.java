package io.oreto.gungnir.cli;

import picocli.CommandLine;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "gungnir", subcommands = Create.class)
public class GungnirCli implements Callable<Integer> {
    public static void main(String[] args) throws IOException {
        int exitCode = new CommandLine(new GungnirCli()).execute(args);
        System.exit(exitCode);
    }

    GungnirCli() {
    }

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        spec.commandLine().usage(System.out);
        spec.subcommands().forEach((k, v) -> v.usage(System.out));
        return 0;
    }
}
