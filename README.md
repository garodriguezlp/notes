# notes

> [jbang](https://www.jbang.dev/) powered ⚡ script for automating the creation of a blank canvas (a Markdown file) for taking notes

```
d8b   db  .d88b.  d888888b d88888b .d8888.
888o  88 .8P  Y8. `~~88~~' 88'     88'  YP
88V8o 88 88    88    88    88ooooo `8bo.
88 V8o88 88    88    88    88~~~~~   `Y8b.
88  V888 `8b  d8'    88    88.     db   8D
VP   V8P  `Y88P'     YP    Y88888P `8888Y'
```

## Want to give it a try?

-  Bash:

    ```
    curl -Ls https://sh.jbang.dev | bash -s - notes@garodriguezlp/notes --help
    ```

- Windows Powershell:

    ```
    iex "& { $(iwr -useb https://ps.jbang.dev) } notes@garodriguezlp/notes --help"
    ```

- Install as a Jbang app

    ```
    jbang app install notes@garodriguezlp/notes
    ```
## Configuration

Create a `.notes.properties` file on your `home` directory to set the default value of the `notes` `WORKSPACE` directory.

> Here's an example of the file content for setting the `workspace` config in `Windows` OS:

    workspace = C:\\Users\\garodriguezlp\\notes-workspace

## Why notes?

I needed an "organized" way of managing my daily notes
