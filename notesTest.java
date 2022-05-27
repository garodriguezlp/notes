///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.junit.jupiter:junit-jupiter-api:5.7.2
//DEPS org.junit.jupiter:junit-jupiter-engine:5.7.2
//DEPS org.junit.platform:junit-platform-launcher:1.7.2
//DEPS com.github.stefanbirkner:system-lambda:1.2.1
//DEPS org.assertj:assertj-core:3.22.0

//SOURCES notes.java

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.LoggingListener;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import picocli.CommandLine;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErr;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOutNormalized;
import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class notesTest {

    @Test
    void testDefaultExecution(@TempDir Path tempDir) throws Exception {
        System.setProperty("notes.workspace", tempDir.toFile().getAbsolutePath());
        Path expectedNotesFile = tempDir.resolve("2022.05").resolve("22.05.26.md");

        String errText = tapSystemErr(() -> {
            String outText = tapSystemOutNormalized(() -> buildCommand().execute("--open-editor=false"));
            assertThat(outText).contains("Running", "code " + expectedNotesFile.toAbsolutePath());
        });

        assertThat(errText).isEmpty();
        assertThat(contentOf(expectedNotesFile.toFile())).startsWith("# 22.05.26");
    }

    @Test
    void testExecutionWithSuffix(@TempDir Path tempDir) throws Exception {
        System.setProperty("notes.workspace", tempDir.toFile().getAbsolutePath());
        Path expectedNotesFile = tempDir.resolve("2022.05").resolve("2022.05.26T01.00-FOO.md");

        String errText = tapSystemErr(() -> {
            String outText = tapSystemOutNormalized(() -> buildCommand().execute("--open-editor=false", "foo"));
            assertThat(outText).contains("Running", "code " + expectedNotesFile.toAbsolutePath());
        });

        assertThat(errText).isEmpty();
        assertThat(contentOf(expectedNotesFile.toFile())).startsWith("# 2022.05.26T01.00-FOO");
    }

    @Test
    void testExecutionWithSuffixAndCustomExtension(@TempDir Path tempDir) throws Exception {
        System.setProperty("notes.workspace", tempDir.toFile().getAbsolutePath());
        Path expectedNotesFile = tempDir.resolve("2022.05").resolve("2022.05.26T01.00-FOO.sql");

        String errText = tapSystemErr(() -> {
            String outText = tapSystemOutNormalized(() -> buildCommand().execute("--open-editor=false",
                    "--extension=sql",
                    "--comment-chars-sequence='--'",
                    "foo"));
            assertThat(outText).contains("Running", "code " + expectedNotesFile.toAbsolutePath());
        });

        assertThat(errText).isEmpty();
        assertThat(contentOf(expectedNotesFile.toFile())).startsWith("'--' 2022.05.26T01.00-FOO");
    }

    private CommandLine buildCommand() {
        Clock clock = Clock.fixed(Instant.parse("2022-05-26T01:00:00Z"), ZoneId.of("UTC"));
        CommandLine commandLine = new CommandLine(new notes(clock));
        commandLine.setDefaultValueProvider(null);
        return commandLine;
    }

    // Run all Unit tests with JBang with ./notes.java
    public static void main(final String... args) {
        final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(notesTest.class))
                .build();
        final Launcher launcher = LauncherFactory.create();
        final LoggingListener logListener = LoggingListener.forBiConsumer((t, m) -> {
            out.println(m.get());
            if (t != null) {
                t.printStackTrace();
            }
        });
        final SummaryGeneratingListener execListener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(execListener, logListener);
        launcher.execute(request);
        execListener.getSummary().printTo(new java.io.PrintWriter(out));
    }
}
