package io.oreto.gungnir.cli;

import io.oreto.gungnir.cli.prompt.option.OptionPrompt;
import io.oreto.gungnir.cli.prompt.StringPrompt;
import io.oreto.gungnir.cli.prompt.option.YesNo;
import io.oreto.gungnir.cli.util.Coder;
import io.oreto.gungnir.cli.util.FileUtils;
import io.oreto.gungnir.cli.util.Str;
import io.oreto.gungnir.cli.vsc.Git;
import org.joox.JOOX;
import org.joox.Match;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import io.oreto.gungnir.cli.prompt.option.InputOption;

import static org.joox.JOOX.*;

@CommandLine.Command(name = "create", description = "Create new gungnir application")
public class Create implements Callable<Integer> {
    final static String GUNGNIR_LAUNCH_APP_NAME = "gungnir-launch";

    final static String ASCII_LINE = "---------------------------------";

    final String[] SRC_MAIN_JAVA = new String[] { "src", "main", "java" };
    final String[] SRC_MAIN_TMP = new String[] { "src", "main", "tmp" };
    final String[] SRC_TEST_JAVA = new String[] { "src", "test", "java" };

    enum Json implements InputOption {
        gson("https://github.com/google/gson"), jackson("https://github.com/FasterXML/jackson");

        Json(String description) {
           this.description = description;
        }

        private final String description;

        public String description() {
            return description;
        }
    }

    @CommandLine.Parameters(index = "0", defaultValue = "")
    private String appPath;

    private String appName;

    @CommandLine.Option(names = "--package", description = "Specify application package")
    String packageName;

    @CommandLine.Option(names = "--json"
            , description = "choose json type: ${COMPLETION-CANDIDATES}")
    Json json;

    @CommandLine.Option(names = "--database", description = "Add database support")
    Boolean database;

    @CommandLine.Option(names = "--di", description = "Add dependency injection")
    Boolean di;

    @CommandLine.Option(names = "--jte", description = "Add view renderer support")
    Boolean jte;

    /**
     * Computes a result, or throws an exception if unable to do so.
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        if (Str.isEmpty(appPath))
            appPath = new StringPrompt("Enter application name").getInput();

        if (appPath.contains("/"))
            appName = appPath.substring(appPath.lastIndexOf('/') + 1);
        else if (appPath.contains("\\"))
            appName = appPath.substring(appPath.lastIndexOf('\\') + 1);
        else
            appName = appPath;

        if (packageName == null)
            packageName = new StringPrompt("Enter the application package", "a.b.c")
                    .defaultTo("example")
                    .getInput();

        if (json == null)
            json = OptionPrompt.create("Choose json provider", Json.values())
                .defaultTo(Json.gson)
                .numberOptions(true)
                .getInput();

        if (database == null)
            database = OptionPrompt.yesNo("Add database support (jOOQ)?").getInput().toBoolean();

        if (di == null)
            di = OptionPrompt.yesNo("Add dependency injection (Guice)?").getInput().toBoolean();

        if (jte == null)
            jte =  OptionPrompt.yesNo("Add view renderer (jte)?").getInput().toBoolean();

        if (!confirm(appPath, packageName, json, database, di, jte)) {
            call();
        }
        boolean created = create();
        if (created) {
            System.out.printf("%s%nCreated application %s%n", ASCII_LINE, appPath);
            System.out.println(ASCII_LINE);
            System.out.println("Next steps:");
            System.out.printf("cd %s%n", appPath);
            System.out.println("mvn compile exec:java");
        }
        return created ? 0 : 1;
    }

    protected boolean confirm(String appName
            , String packageName
            , Json json
            , boolean database
            , boolean di
            , boolean jte) {
        System.out.printf("%s%n", ASCII_LINE);
        System.out.println("Confirm Selections");
        System.out.println(ASCII_LINE);
        System.out.printf("application name: %s%n", appName);
        System.out.printf("package name: %s%n", packageName);
        System.out.printf("json provider: %s%n", json);
        System.out.printf("jOOQ: %s%n", database);
        System.out.printf("Guice: %s%n", di);
        System.out.printf("Jte: %s%n", jte);

        return OptionPrompt.yesNo("Is this correct?").getInput() == YesNo.y;
    }

    protected boolean create() throws IOException, InterruptedException, SAXException {
        Path appPath = Paths.get(this.appPath);
        if (Files.exists(appPath)) {
            boolean overwrite = OptionPrompt.yesNo(String.format("%s already exists, overwrite?", this.appPath))
                    .defaultTo(YesNo.n)
                    .getInput()
                    .toBoolean();
            if (overwrite) {
                FileUtils.deleteDirectory(appPath);
            } else {
                System.out.println("stopping");
                return false;
            }
        }
        File appDir = Git.cloneGh(GUNGNIR_LAUNCH_APP_NAME + ".git", Paths.get(this.appPath));
        // create app src package
        List<String> srcJava = new ArrayList<>(List.of(SRC_MAIN_TMP));
        List<String> packageList = List.of(packageName.split("\\."));
        srcJava.addAll(packageList);
        Path srcPath = Paths.get(this.appPath, srcJava.toArray(new String[0]));
        Files.createDirectories(srcPath);

        // build app according to specs
        updatePom();
        writeMainClass(srcPath);
        updateReadme();
        Path srcMainJava = Paths.get(this.appPath, SRC_MAIN_JAVA);
        FileUtils.deleteDirectory(srcMainJava);
        Files.move(Paths.get(this.appPath, SRC_MAIN_TMP), srcMainJava);
        FileUtils.packageDirectory(srcMainJava);
        FileUtils.deleteDirectory(Paths.get(this.appPath, ".git"));

        // create app src test package
        srcJava.clear();
        srcJava.addAll(List.of(SRC_TEST_JAVA));
        srcJava.addAll(packageList);
        Files.createDirectories(Paths.get(this.appPath, srcJava.toArray(new String[0])));
        return appDir.exists();
    }

    protected void updateReadme() throws IOException {
        Path readme = Paths.get(appPath, "README.md");
        Files.write(
                readme
                , Str.of(Files.readString(readme)).findAndReplace(GUNGNIR_LAUNCH_APP_NAME, Str.toKebab(appName)).getBytes()
        );
    }

    protected void updatePom() throws IOException, SAXException {
        File pomFile = Paths.get(appPath, "pom.xml").toFile();
        Document document = JOOX.builder().parse(pomFile);
        Match doc = $(document);
        if (json == Json.gson) {
            removeDependency(doc, "com.fasterxml.jackson.core");
        }
        if (!database) {
            removeDependency(doc, "org.jooq");
        }
        if (!di) {
            removeDependency(doc, "com.google.inject");
        }
        if (!jte) {
            removeDependency(doc, "gg.jte");
        }
        updateAppDetails(doc);
        doc.write(pomFile);
    }

    protected void removeDependency(Match doc, String groupId) {
        Match test = doc.find(selector("dependency"))
                .find(tag("groupId"))
                .filter(matchText(groupId.replaceAll("\\.", "\\\\" + ".")));
        if (test.size() == 1)
            test.parent().remove();
    }

    protected void updateAppDetails(Match doc) {
        doc.child(tag("groupId")).text(packageName);
        doc.child(tag("artifactId")).text(Str.toKebab(appName));
        doc.find(selector("mainClass")).text(String.format("%s.%s", packageName, Str.capitalize(appName)));
    }

    protected void writeMainClass(Path srcPath) throws IOException {
        final String GUNGNIR_LAUNCH = "GungnirLaunch";
        Optional<Path> mainClass;
        try(Stream<Path> stream = Files.walk(Paths.get(this.appPath, SRC_MAIN_JAVA))) {
            mainClass = stream
                    .filter(path -> path.getFileName().toString().startsWith(GUNGNIR_LAUNCH + "."))
                    .findFirst();
        }
        if (mainClass.isPresent()) {
           Str str = Str.of(Files.readString(mainClass.get()));
           String mainClassName = Str.capitalize(appName);
           str.replaceFrom("public class ", "extends", false
                   , "public class ", mainClassName, Str.SPACE)
                   .replace(GUNGNIR_LAUNCH, mainClassName);

            if (json == Json.gson) {
                Str tab = Str.of();
                Optional<Integer> index = str.indexOf('{');
                if (index.isPresent()) {
                    for (int i = index.get() + 1; i < str.length(); i++) {
                        char c = str.charAt(i);
                        if (c == '\n' || c == '\r')
                            tab.delete();
                        else if (Character.isWhitespace(c))
                            tab.add(c);
                        else
                            break;
                    }
                }
                writeGsonMapper(str, tab.length());
                FileUtils.copyDirectory(Paths.get(mainClass.get().getParent().toString(), Json.gson.name())
                        , Paths.get(srcPath.toString(), Json.gson.name()));
            }
            Files.write(Paths.get(srcPath.toString(), String.format("%s.java", mainClassName)), str.getBytes());
        }
    }

    protected void writeGsonMapper(Str str, int indentSize) {
        final String GSON_MAPPER = "GsonMapper";
        Optional<Integer> classClose = str.lastIndexOf('}');
        if (classClose.isPresent()) {
            int i = classClose.get() - 1;
            str.insert(i, "\n", "\n");
            i += 2;
            Coder coder = new Coder(indentSize);
            coder.setIndentationLevel(1);
            Coder.Method method = coder.method("jsonMapper", "JsonMapper")
                    .modifiers(Coder.Modifiers.PROTECTED)
                    .annotate(new Coder.Annotation("Override"));
            method.getBody().statement("return ", GSON_MAPPER + ".mapper");
            str.replaceFrom(i, i, true, method.toString());

            str.insert(str.indexOf("import ").orElse(-1) - 1
                    , "import io.javalin.json.JsonMapper;"
                    , "\n"
                    , String.format("import %s.%s.%s;"
                            , packageName
                            , Json.gson.name()
                            , GSON_MAPPER));
        }
    }
}
