### Gungnir-cli
Command line utility to create gungnir applications

project must use a graalvm jdk
set JAVA_HOME to a graalvm install
```cmd
set JAVA_HOME=java.path
```
```powershell
$env:JAVA_HOME=""
```

Prerequisites:
 - windows: x64 Native Tools Command Prompt (from Visual Studio)
 - MacOS: xcode-select --install
 - Linux: sudo apt-get install build-essential libz-dev zlib1g-dev

graalvm must use native image tool
```
gu install native-image
```

```
mvn package -Pnative
```

