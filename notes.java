///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS info.picocli:picocli:4.6.3
//DEPS org.apache.commons:commons-lang3:3.12.0
//DEPS commons-io:commons-io:2.11.0
//DEPS org.slf4j:slf4j-api:2.0.6
//DEPS org.slf4j:slf4j-simple:2.0.6

//RUNTIME_OPTIONS -Dorg.slf4j.simpleLogger.showLogName=false
//RUNTIME_OPTIONS -Dorg.slf4j.simpleLogger.showThreadName=false

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.time.Clock.systemDefaultZone;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.PropertiesDefaultProvider;

@Command(name = "notes",
    mixinStandardHelpOptions = true,
    version = "notes 0.2",
    description = "notes made with jbang",
    defaultValueProvider = PropertiesDefaultProvider.class,
    showDefaultValues = true,
    usageHelpAutoWidth = true,
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
class notes implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(notes.class);

  private static List<String> HEADER = List.of("",
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
  private static final String CONTAINING_MONTH_FOLDER_FORMAT = "yyyy.MM";
  private static final String PROPERTIES_DEFAULT_PROVIDER_DES = "property on '${sys:user.home}${sys:file.separator}.notes.properties'";

  private final Clock clock;

  @Option(names = "--default-workspace",
      required = true,
      descriptionKey = "workspace.default",
      defaultValue = "${notes.workspace.default}",
      description = "The workspace dir. Defaults to `workspace.default` "
          + PROPERTIES_DEFAULT_PROVIDER_DES)
  private File defaultWorkspace;

  @Option(names = "--work-workspace",
      descriptionKey = "workspace.work",
      defaultValue = "${notes.workspace.work}",
      description = "The workspace dir. Defaults to `workspace.work` "
          + PROPERTIES_DEFAULT_PROVIDER_DES)
  private File workWorkspace;

  @Option(names = "--personal-workspace",
      descriptionKey = "workspace.personal",
      defaultValue = "${notes.workspace.personal}",
      description = "The workspace dir. Defaults to `workspace.personal` "
          + PROPERTIES_DEFAULT_PROVIDER_DES)
  private File personalWorkspace;

  @ArgGroup
  private ActiveWorkspace activeWorkspace;

  public static class ActiveWorkspace {

    @Option(names = {"-w", "--work"}, description = "Use the work workspace")
    private Boolean work;

    @Option(names = {"-p", "--personal"}, description = "Use the personal workspace")
    private Boolean personal;
  }

  @Option(names = "--extension",
      required = true,
      descriptionKey = "extension",
      defaultValue = "${notes.extension:-md}",
      description = "The notes file extension")
  public String extension;

  @Option(names = "--skip-header",
      required = true,
      descriptionKey = "header.skip",
      defaultValue = "${notes.header.skip:-false}",
      description = "Skip the file header generation")
  public boolean skipHeader;

  @Option(names = "--header-prefix",
      required = true,
      descriptionKey = "header.prefix",
      defaultValue = "${notes.header.prefix:-#}",
      description = "The char sequence to use for comments")
  public String headerPrefix;

  @Option(names = "--open-editor",
      required = true,
      descriptionKey = "editor.open",
      defaultValue = "${notes.editor.open:-true}",
      description = "Whether or not to open the text editor")
  boolean openEditor;

  @Option(names = "--editor",
      required = true,
      descriptionKey = "editor",
      defaultValue = "${notes.editor:-code}",
      description = "The text code editor")
  String editor;

  @Parameters(index = "*", description = "The note description")
  private List<String> descriptionList;

  public notes(Clock clock) {
    this.clock = clock;
  }

  public static void main(String... args) {
    new CommandLine(new notes(systemDefaultZone())).execute(args);
  }

  @Override
  public void run() {
    printArt();
    File activeWorkspace = determineActiveWorkspace();
    String description = buildDescription();
    printInfo(description, activeWorkspace);
    try {
      File notesFile = buildNoteFile(description, activeWorkspace);
      writeHeader(notesFile);
      openEditor(notesFile.getAbsolutePath());
    } catch (Exception ex) {
      logger.error("Error while creating the note file", ex);
    }
  }

  private static void printArt() {
    logger.info(String.join(lineSeparator(), HEADER));
  }

  private File determineActiveWorkspace() {
    if (activeWorkspace == null) {
      logger.info("No active workspace specified. Using default workspace");
      return defaultWorkspace;
    }

    if (activeWorkspace.work != null && activeWorkspace.work && workWorkspace != null) {
      logger.info("Active workspace is work. Using work workspace");
      return workWorkspace;
    }

    if (activeWorkspace.personal != null && activeWorkspace.personal && personalWorkspace != null) {
      logger.info("Active workspace is personal. Using personal workspace");
      return personalWorkspace;
    }

    logger.warn("No valid workspace specified. Using default workspace");
    return defaultWorkspace;
  }

  private String buildDescription() {
    return Optional.ofNullable(descriptionList)
        .map(s -> s.stream()
            .map(suffix1 -> suffix1.trim().toUpperCase())
            .collect(joining("-")))
        .orElse(null);
  }

  private void printInfo(String description, File activeWorkspace) {
    logger.info("--- ----------------------------------------------------------------------------");
    logger.info("Input parameters:");
    logger.info("workspace: {}", activeWorkspace);
    if (isNotEmpty(description)) {
      logger.info("description: {}", description);
    }
    logger.info("--- ----------------------------------------------------------------------------");
    logger.info("");
  }

  private File buildNoteFile(String suffix, File activeWorkspace) {
    LocalDateTime today = LocalDateTime.now(clock);
    String containingFolder = today.format(
        DateTimeFormatter.ofPattern(CONTAINING_MONTH_FOLDER_FORMAT));
    String fileName = buildFileName(today, suffix);
    return FileUtils.getFile(activeWorkspace, containingFolder,
        format("%s.%s", fileName, extension));
  }

  private String buildFileName(LocalDateTime today, Object suffix) {
    return Optional.ofNullable(suffix)
        .map(s -> format("%s-%s",
            today.format(DateTimeFormatter.ofPattern(FILE_WITH_SUFFIX_NAME_FORMAT)), suffix))
        .orElseGet(() -> today.format(DateTimeFormatter.ofPattern(FILE_NAME_FORMAT)));
  }

  private void writeHeader(File notesFile) throws IOException {
    if (!skipHeader) {
      String header = removeExtension(notesFile.getName());
      writeStringToFile(notesFile, format("%s %s", headerPrefix, header), CHARSET);
    }
  }

  private void openEditor(final String notesFile) throws IOException {
    String filePath = notesFile.contains(" ") ? format("\"%s\"", notesFile) : notesFile;
    String editorCmd = String.join(" ", editor, filePath);

    List<String> cmd = SystemUtils.IS_OS_UNIX ?
        List.of("sh", "-c", editorCmd) :
        List.of("cmd", "/c", editorCmd);

    if (openEditor) {
      logger.debug("Running `{}`", cmd);
      new ProcessBuilder(cmd).start();
    } else {
      logger.info("The editor was not open because `--open-editor` is set as `false`");
    }
  }
}
