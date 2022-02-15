#!/bin/bash

# cd $ROOT
# gradle project name, also subfloder
PROJECT=$1
echo "Creating project : $PROJECT under $PWD";

mkdir $PROJECT
cd $PROJECT
#mkdir "lib"
mkdir "src"
mkdir "src/main"
mkdir "src/main/java"
mkdir "src/test"
mkdir "src/test/java"

if [["$PROJECT" == "lphy"]]; then
  echo "Creating SPI for lphy extension ..."
  mkdir "src/main/resources"
  mkdir "src/main/resources/META-INT"
  mkdir "src/main/resources/META-INT/services"
  echo '# META-INF/services must be under src for Intellij
        # Class requires a public no-args constructor
  ' > src/main/resources/META-INT/services/lphy.spi.LPhyExtension

  echo "Creating module-info ..."
  echo '' > src/main/java/module-info.java
fi

echo "Creating Gradle build ..."
echo 'plugins {
          `java-library`
      }

      version = "0.0.1-SNAPSHOT"
      base.archivesName.set("?")

      java {
          sourceCompatibility = JavaVersion.VERSION_16
          targetCompatibility = JavaVersion.VERSION_16
          withSourcesJar()
      }

      dependencies {
          // ...
      }

      tasks.jar {
          manifest {
              // shared attr in the root build
              attributes(
                  "Implementation-Title" to "?",
                  "Implementation-Vendor" to "?",
              )
          }
      }

      tasks.test {
          useJUnit()
          // useJUnitPlatform()
          // set heap size for the test JVM(s)
          minHeapSize = "128m"
          maxHeapSize = "1G"
          // show standard out and standard error of the test JVM(s) on the console
          testLogging.showStandardStreams = true
          //testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
      }
' > build.gradle.kts

echo "Complete. Create 'lib' or 'examples' folders by yourself if they are required.";
ls
cd ..
