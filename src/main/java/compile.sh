#!/usr/bin/env bash

if [ -d classes ]; then
    rm -rf classes;
fi
mkdir classes

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home

echo $JAVA_HOME

javac -classpath $JAVA_HOME/lib/tools.jar -sourcepath com/spacex/lombok/test/*.java -d classes/

# another way to compile
#javac -cp $JAVA_HOME/lib/tools.jar com/spacex/lombok/test/Getter* -d classes/

javac -cp classes -d classes -processor com.spacex.lombok.test.GetterProcessor com/spacex/lombok/test/App.java

javap -p classes com/spacex/lombok/test/App.class

java -cp classes com.spacex.lombok.test.App