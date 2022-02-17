///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0
//DEPS org.apache.commons:commons-lang3:3.11

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SystemUtils;

@Command(name = "notes", mixinStandardHelpOptions = true, version = "notes 0.1", description = "notes made with jbang", header = {
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

    @Parameters(index = "0", description = "The greeting to print", defaultValue = "World!")
    private String greeting;

    public static void main(String... args) {
        int exitCode = new CommandLine(new notes()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        System.out.println("Hello " + greeting);

        String[] cmd;
        final String editorCommand = "code";
        if (SystemUtils.IS_OS_UNIX) {
            cmd = new String[] { "sh", "-c", editorCommand };
        } else {
            cmd = new String[] { "cmd", "/c", editorCommand };
        }
        System.out.println("Running `" + String.join(" ", cmd) + "`");
        new ProcessBuilder(cmd).start();
        return 0;
    }
}
