///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS info.picocli:picocli:4.6.3
//DEPS org.apache.commons:commons-lang3:3.12.0
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
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static java.lang.System.*;
import static java.time.Clock.systemDefaultZone;
import static java.util.stream.Collectors.joining;
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
    private static final String FILE_NAME_FORMAT = "yy.MM.dd";
    private static final String FILE_WITH_SUFFIX_NAME_FORMAT = "yyyy.MM.dd'T'HH.mm";
    private static final String MONTH_FOLDER_FORMAT = "yyyy.MM";

    private final Clock clock;

    @Option(names = {"-w", "--workspace"},
            required = true,
            paramLabel = "WORKSPACE",
            defaultValue = "${notes.workspace}",
            description = "The workspace dir. Defaults to `workspace` property on '${sys:user.home}${sys:file.separator}.notes.properties'")
    private File workspace;

    @Parameters(index = "*", description = "The issue title")
    private List<String> suffixes;

    @Option(names = {"-e", "--editor"},
            required = true,
            paramLabel = "EDITOR",
            defaultValue = "${notes.editor:-code}",
            description = "The text code editor. Defaults to `${notes.editor:-code}`")
    private String notesEditor;

    @Option(names = {"-x", "--extension"},
            required = true,
            paramLabel = "EXTENSION",
            defaultValue = "${notes.extension:-md}",
            description = "The notes file extension. Defaults to `${notes.extension:-md}`")
    public String extension;

    @Option(names = {"-s", "--skip-header"},
            required = true,
            paramLabel = "SKIP HEADER",
            defaultValue = "${notes.skipHeader:-false}",
            description = "Skip the file header generation. Defaults to `${notes.skipHeader:-false}`")
    public boolean skipHeader;

    @Option(names = {"-c", "--comment-chars-sequence"},
            required = true,
            paramLabel = "COMMENT_CHARS_SEQUENCE",
            defaultValue = "${notes.commentSequence:-#}",
            description = "The char sequence to use for comments. Defaults to `${notes.commentSequence:-#}`")
    public String commentSequence;

    @Option(names = {"-o", "--open-editor"},
            required = true,
            paramLabel = "OPEN_EDITOR",
            defaultValue = "${notes.openEditor:-true}",
            description = "Whether or not to open the text editor. Defaults to `${notes.openEditor:-#}`")
    public boolean openEditor;

    public notes(Clock clock) {
        this.clock = clock;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new notes(systemDefaultZone())).execute(args);
        exit(exitCode);
    }

    @Override
    public Integer call() {
        String suffix = buildSuffix();
        printHeader(suffix);
        try {
            File notesFile = buildNoteFile(suffix);
            writeDefaultHeader(notesFile);
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
                        .collect(joining("-")))
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
        LocalDateTime today = LocalDateTime.now(clock);
        String monthFolder = today.format(DateTimeFormatter.ofPattern(MONTH_FOLDER_FORMAT));
        String fileName = buildFileName(today, suffix);
        return FileUtils.getFile(workspace, monthFolder, format("%s.%s", fileName, extension));
    }

    private String buildFileName(LocalDateTime today, Object suffix) {
        return Optional.ofNullable(suffix)
                .map(s -> format("%s-%s", today.format(DateTimeFormatter.ofPattern(FILE_WITH_SUFFIX_NAME_FORMAT)), suffix))
                .orElseGet(() -> today.format(DateTimeFormatter.ofPattern(FILE_NAME_FORMAT)));
    }

    private void writeDefaultHeader(File notesFile) throws IOException {
        if (!notesFile.exists() && !skipHeader) {
            writeStringToFile(notesFile, format("%s %s", commentSequence, removeExtension(notesFile.getName())), CHARSET);
        }
    }

    private void openEditor(final String notesFile) throws IOException {
        String filePath = notesFile.contains(" ") ? format("\"%s\"", notesFile) : notesFile;
        String editorCmd = String.join(" ", notesEditor, filePath);

        List<String> cmd = SystemUtils.IS_OS_UNIX ?
                List.of("sh", "-c", editorCmd) :
                List.of("cmd", "/c", editorCmd);

        out.println("Running `" + String.join(" ", cmd) + "`");

        if (openEditor) {
            new ProcessBuilder(cmd).start();
        } else {
            out.println("--open-editor is set as `false`, therefore no editor was open");
        }
    }
}
