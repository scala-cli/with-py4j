# with-py4j

Helper application to start another app with a [py4j](https://github.com/py4j/py4j) server running in the background

Use like
```text
$ cs launch io.get-coursier:echo:1.0.5 \
    io.github.alexarchambault.py4j:with-py4j:0.1.0 \
    -M withpy4j.WithPy4j \
    -- \
      coursier.echo.Echo aa bb
aa bb
```

This runs the `io.get-coursier:echo:1.0.5` application, whose main class is `coursier.echo.Echo`, with arguments `aa bb`, with a py4j server running in the background.

*with-py4j* sets the following Java properties, so that the launched app can know how to connect to the py4j server:
- `with-py4j.port`
- `with-py4j.secret`

