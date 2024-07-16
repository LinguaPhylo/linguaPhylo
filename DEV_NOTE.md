# LPhy Developer Guide 101 

The project is a Maven project. 
Please follow the instruction to load the project as a Maven project into IntelliJ.
The modules and their dependencies suppose to be automatically configured by IntelliJ.

## Setup development environment

1. Install [OpenJDK 17](https://jdk.java.net/17/), or Higher version. Try the following command line in your terminal 
to identify if you have a correct version of JDK.

```bash
java -version
```

2. Install [Git](https://github.com/git-guides/install-git) if you do not have it.

```bash
git -v
```

Here is a simple [tutorial](https://www.w3schools.com/git/).

3. Install the latest version of [IntelliJ](https://www.jetbrains.com/idea/download/)

4. Clone the project from GitHub:

```bash
git clone https://github.com/LinguaPhylo/linguaPhylo.git
```

More projects are using this as a core, such as: 

-[LPhyBeast](https://github.com/LinguaPhylo/LPhyBeast)
- [Phylonco](https://github.com/bioDS/beast-phylonco)
- [LPhyBeastExt](https://github.com/LinguaPhylo/LPhyBeastExt)
- [ToroidalDiffusion](https://github.com/alexeid/toroidalDiffusion)

5. (Optional) install Maven if you want to use it in the terminal. For Mac, recommend to use Homebrew:

```bash
brew install maven
```

## Folder structure

Once you cloned the project, it will look like the following structure in your computer.

**Please note** all LPhy related projects must be stored as sister folders
inside one parent folder.  
In addition, please keep the original folder name when the project is cloned,
otherwise the automatic configuration will be interrupted.

```
home.dir
    ├── WorkSpace
    .     ├── linguaPhylo
    .     .    ├── bin
    .     .    ├── examples
    .     .    ├── IntelliJ
    .     .    ├── lphy
    .     .    .    └── src
    .     .    .         ├── main
    .     .    .         │    ├── java
    .     .    .         │    │     ├── lphy.core.*
    .     .    .         │    │     └── module-info.java
    .     .    .         │    └── resources
    .     .    .         └── test
    .     .    ├── lphy-base
    .     .    .    └── ...
    .     .    ├── lphy-studio 
    .     .    .    └── ...
    .     ├── LPhyBeast
    .     .    .    └── ...
    .     .
    └──...
```

Some concepts:

- `home.dir` represents your [home directory](https://en.wikipedia.org/wiki/Home_directory), which is also known as `~` in Linux or Mac.
- `WorkSpace` is the parent folder to keep all LPhy projects, which can be any names.
- `linguaPhylo` is the project root directory for the linguaPhylo project.
- `lphy`, `lphy-base`, and `lphy-studio` contains the tree modules defined in the linguaPhylo project.
- `LPhyBeast` is the project root directory for the LPhyBeast project.

Here is a tutorial for [Multi-module projects](https://www.jetbrains.com/guide/java/tutorials/marco-codes-maven/multi-module-projects/).

## Maven project 

Here are two tutorials for importing a Maven project to IntelliJ :

- [Importing a Maven project](https://www.jetbrains.com/guide/java/tutorials/working-with-maven/importing-a-project/)
- [Add Maven support to an existing project](https://www.jetbrains.com/help/idea/convert-a-regular-project-into-a-maven-project.html)

**Please note** we provide the project settings in another directory called [IntelliJ](IntelliJ/.idea/). 
After following the instruction below, you can simply load the project by selecting the directory not pom file. 

### First time

We do not share the project settings, so please **do not** change your git settings 
to commit any IntelliJ project settings.
For the first time to set up the project, you need to copy the [settings](IntelliJ/.idea/) 
into the project root directory in order to start the project.

Go to the project root directory, for example, `~/WorkSpace/linguaPhylo` in my Mac.
Use the 1st command to check if the hidden folder `.idea/` exists. 
Only use the 2nd command to delete it, when it exists.

```bash
ls -la 
rm -r .idea/
```

Then, use the 1st command to copy the settings, and 2nd command line to check if it is done.

```bash
cp -r ./IntelliJ/.idea/ .
ls -la .idea/
```

We recommend you to [clear the IntelliJ caches](https://www.jetbrains.com/help/idea/invalidate-caches.html)
by selecting all options before importing the project.
Now you can start the IntelliJ, and click the `Open` button to select the project root directory.
IntelliJ will open it as a Maven project (please go through the above tutorials if you are not familiar with this process).

Wait for IntelliJ to download all dependencies and make indexing, it normally takes about one minute.
After it is done (no progress bar appears on the bottom), your IntelliJ should have the Maven icon on the right side.
Check if there is any red line under any Maven tasks, which is indicating a problem.

If all good, then click the `Rebuild project` from the `Build` menu, and wait until it finishes.

Once the project is successfully imported, it should look like:

<a href="./figs/IntelliJLPhy.png"><img src="./figs/IntelliJLPhy.png" width="700" ></a>

### If you had LPhy previously in IntelliJ

You can skip this section, if you are the first time to set up the project 
or your project has been successfully imported following the instructions above. 

If you had any LPhy related projects previously (below version 1.6.*) in IntelliJ,
or you have a problem during the importing,
you can follow the following steps: 

1. Delete all existing projects in IntelliJ.

2. Delete all cloned LPhy projects, and clone a new copy again.  

3. [Clear the IntelliJ caches](https://www.jetbrains.com/help/idea/invalidate-caches.html)
   by selecting all options.

4. Repeat the process in [the above section](#First-time).

### Project settings




### Working inside IntelliJ



### Dependencies





## Release procedure

1. Make sure all versions not containing the postfix "SNAPSHOT".
Run `./gradlew clean build --no-build-cache`, which will run all unit tests as well.
In the end, it creates a Zip file `lphy-studio-1.x.x.zip` in `$PROJECT_DIR/lphy-studio/distributions`.

2. Run the task `lphyDoc` to generate LPhy docs.
The output will be in the directory [$PROJECT_DIR/lphy/doc](lphy/doc) as default.  

For the extension developer, you need to change the script to set the arguments
passed main method in your lphyDoc from `setArgs(listOf("$version"))`
into `setArgs(listOf("$version", "$EXT_NAME", "$CLS_NAME"))`,
where $EXT_NAME is your extension name appeared in the doc title,
and $CLS_NAME is the full class name with package that implements LPhyExtension,
such as phylonco.lphy.spi.Phylonco in the Phylonco extension.

3. Create a pre-release in Github, and upload the Zip file.
In addition, if you do not publish the jars to the Maven central repository,
you need to provide the jar file and its source jar in the release. 

4. Run `./gradlew publish --info -P...` to publish to the Maven central repository. 
Please note: once published, you will not be able to remove/update/modify the jar.

5. Follow the [instruction](https://central.sonatype.org/publish/release/)
of the releasing deployment to complete publishing at https://s01.oss.sonatype.org/.

For snapshots, check https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/linguaphylo/.
For releases, check https://s01.oss.sonatype.org/content/repositories/releases/io/github/linguaphylo/.

### Tips:

- After final release, it is a good behavior to instantly update your versions
  in the build files into the next version with the postfix "SNAPSHOT".
- After release, the version will immediately appear in the s01.oss,
  but it will take one/two days to synchronise to [Maven Central Repository](https://central.sonatype.com).
  For example, https://repo.maven.apache.org/maven2/io/github/linguaphylo/.

## Useful Links

- [LPhyBEAST developer note](https://github.com/LinguaPhylo/LPhyBeast/blob/master/DEV_NOTE.md)

- [Maven linguaphylo group](https://search.maven.org/search?q=io.github.linguaphylo)
