# Diffho

This is a simple **diff** utility for Human-Optimized Config Object Notation ([HOCON](https://en.wikipedia.org/wiki/HOCON)) formatted config files.

### Build

To build the project, run `./gradlew clean build`. 

### Usage

```bash
java -jar ./build/libs/diffho-all.jar <configFile1> <configFile2>
```

### GraalVM

This project can use [GraalVM](https://www.graalvm.org/) to compile the tool into a single **native executable file**. As a result, the application can run without Java/JVM installed in the target machine.

The command below will download all of the project and build dependencies including the GraalVM SDK and will create a native executable file named `diffho` in the `build/graal` directory.

```bash
./gradlew nativeImage

```


Usage:
```bash
/build/graal/diffho <configFile1> <configFile2>
```

You can also download the pre-compiled Linux executable here: [diffho](build/graal/diffho)