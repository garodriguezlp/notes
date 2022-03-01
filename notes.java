///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0
//DEPS org.apache.commons:commons-lang3:3.11
//DEPS commons-io:commons-io:2.11.0

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.System.*;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

// Font Name: Basic
@Command(name = "notes",
        mixinStandardHelpOptions = true,
        version = "notes 0.1",
        description = "notes made with jbang",
        defaultValueProvider = PropertiesDefaultProvider.class,
        header = {
                "",
                "d8b   db  .d88b.  d888888b d88888b .d8888.",
                "888o  88 .8P  Y8. `~~88~~' 88'     88'  YP",
                "88V8o 88 88    88    88    88ooooo `8bo.",
                "88 V8o88 88    88    88    88~~~~~   `Y8b.",
                "88  V888 `8b  d8'    88    88.     db   8D",
                "VP   V8P  `Y88P'     YP    Y88888P `8888Y'",
                ""
        })
class notes implements Callable<Integer> {

    private static List<String> HEADER = List.of(
            "",
            "d8b   db  .d88b.  d888888b d88888b .d8888.",
            "888o  88 .8P  Y8. `~~88~~' 88'     88'  YP",
            "88V8o 88 88    88    88    88ooooo `8bo.",
            "88 V8o88 88    88    88    88~~~~~   `Y8b.",
            "88  V888 `8b  d8'    88    88.     db   8D",
            "VP   V8P  `Y88P'     YP    Y88888P `8888Y'",
            "");

    private static final String CHARSET = "UTF-8";
    private static final String FILE_NAME_FORMAt = "yy.MM.dd";
    private static final String FILE_WITH_SUFFIX_NAME_FORMAT = "yyyy.MM.dd'T'HH.mm";
    private static final String MONTH_FOLDER_FORMAT = "yyyy.MM";

    @Option(names = {"-w", "--workspace"},
            required = true,
            paramLabel = "WORKSPACE",
            description = "The workspace dir. Defaults to `workspace` property on '${sys:user.home}${sys:file.separator}.notes.properties'")
    private File workspace;

    @Parameters(index = "*", description = "The issue title")
    private List<String> suffixes;

    @Option(names = {"-e", "--editor"},
            required = true,
            paramLabel = "EDITOR",
            defaultValue = "code",
            description = "The text code editor")
    private String editor;

    public static void main(String... args) {
        int exitCode = new CommandLine(new notes()).execute(args);
        exit(exitCode);
    }

    @Override
    public Integer call() {
        String suffix = buildSuffix();
        printHeader(suffix);
        try {
            File notesFile = buildNoteFile(suffix);
            if (!notesFile.exists()) {
                writeStringToFile(notesFile, format("# %s", removeExtension(notesFile.getName())), CHARSET);
            }
            openEditor(notesFile.getAbsolutePath());
        } catch (Exception ex) {
            err.printf("ERROR: %s%n", ex.getMessage());
        }
        return 0;
    }

    private String buildSuffix() {
        return Optional.ofNullable(suffixes)
                .map(s -> s.stream()
                        .map(suffix1 -> suffix1.trim().toUpperCase())
                        .collect(Collectors.joining("-")))
                .orElse(null);
    }

    private void printHeader(String suffix) {
        out.println(String.join(lineSeparator(), HEADER));
        out.println("--- ----------------------------------------------------------------------------");
        out.println("Input parameters:");
        out.println("workspace: " + workspace);
        if (isNotEmpty(suffix)) {
            out.println("suffix: " + suffix);
        }
        out.println("--- ----------------------------------------------------------------------------");
        out.println("");
    }

    private File buildNoteFile(String suffix) {
        LocalDateTime today = LocalDateTime.now();
        String monthFolder = today.format(DateTimeFormatter.ofPattern(MONTH_FOLDER_FORMAT));
        String fileName = buildFileName(today, suffix);
        return FileUtils.getFile(workspace, monthFolder, format("%s.md", fileName));
    }

    private String buildFileName(LocalDateTime today, Object suffix) {
        return Optional.ofNullable(suffix)
                .map(s -> format("%s-%s", today.format(DateTimeFormatter.ofPattern(FILE_WITH_SUFFIX_NAME_FORMAT)), suffix))
                .orElseGet(() -> today.format(DateTimeFormatter.ofPattern(FILE_NAME_FORMAt)));
    }

    private void openEditor(final String notesFile) throws IOException {
        String[] cmd;
        if (SystemUtils.IS_OS_UNIX) {
            cmd = new String[]{"sh", "-c", editor, notesFile};
        } else {
            cmd = new String[]{"cmd", "/c", editor, notesFile};
        }
        out.println("Running `" + String.join(" ", cmd) + "`");
        new ProcessBuilder(cmd).start();
    }
}
