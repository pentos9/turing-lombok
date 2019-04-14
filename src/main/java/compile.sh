#!/usr/bin/env bash
if [ -d classes ]; then
    rm -rf classes;
fi
mkdir classes
javac -cp $JAVA_HOME/lib/tools.jar com/spacex/lombok/test/* -d classes/
javac -cp classes -d classes -processor com.spacex.lombok.test.GetterProcessor com/spacex/lombok/test/App.java
javap -p classes com/spacex/lombok/test/App.class
java -cp classes com.spacex.lombok.test.App