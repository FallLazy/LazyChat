# LazyChat | [![Maven Central](https://img.shields.io/maven-central/v/io.github.falllazy/lazychat.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.falllazy/lazychat)

[![LazyChat](https://api.mcbanners.com/banner/resource/hangar/LazyChat/banner.png?background__template=PURPLE_TAUPE)](https://hangar.papermc.io/FallLazy/LazyChat)
[![LazyChat](https://api.mcbanners.com/banner/resource/modrinth/lazychat/banner.png?background__template=MALACHITE_GREEN)](https://modrinth.com/plugin/lazychat)

LazyChat - It's a Chat system, that adds MiniMessage to chat, broadcasts and signs.  
And... it has a lot of things, that can be configured.

[Documentation of plugin](https://falllazy.github.io/LazyChat_docs/) (ukr is outdated)

## For developers

#### Important:
project using `dev.falllazy.lazyChat`, not `io.github.falllazy.lazychat`. `io.github.falllazy.lazychat` is only for Maven Central

### Gradle  
Add `mavenCentral()` to your build.gradle if you don't have it:
```groovy
repositories {
	mavenCentral()
}
```
And, add the dependency
```groovy
dependencies {
    implementation 'io.github.falllazy:lazychat:VERSION'
    // Replace VERSION with that version, that you want
}
```
### Gradle.kts
Add repository to your build.gradle.kts
```kotlin
repositories {
    mavenCentral()
}
```
And, add the dependency
```kotlin
dependencies {
    implementation("io.github.falllazy:lazychat:VERSION")
    // Replace VERSION with that version, that you want
}
```
### Maven
Add the dependency to your project
```xml
<dependency>  
    <groupId>io.github.falllazy</groupId>
    <artifactId>lazychat</artifactId>
    <version>VERSION</version>
</dependency>
```

