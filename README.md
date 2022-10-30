# Requirement

- Java >= 8
- A CSV file representing a light curve (usually produced by Tangra) like this :

    FrameNo,Time (UT),Signal (1), Background (1)
    0,[02:47:20.867],33.000,22.000
    1,[02:47:20.907],63.000,0.0000

# Usage

By default, a file with the name **lightCurve.csv** is searched in the directory as the jar file.

By default, we assume that the exposure of the frames is 40 ms.

You can override these default values.

To launch the program, you can use these command lines:

```console
java -jar acquisition-delay.jar
```

or

```console
java -jar acquisition-delay.jar exposureDurationInMs
```

or

```console
java -jar acquisition-delay.jar exposureDurationInMs filename
```

Examples:

```console
java -jar acquisition-delay.jar
```

or

```console
java -jar acquisition-delay.jar 30
```

or

```console
java -jar acquisition-delay.jar 30 myLightCurve.csv
```