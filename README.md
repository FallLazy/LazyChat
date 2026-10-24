# LazyChat | [![](https://jitpack.io/v/FallLazy/LazyChat.svg)](https://jitpack.io/#FallLazy/LazyChat)

LazyChat - It's a Chat system, that adds MiniMessage to chat, broadcasts and signs.  
And... it has a lot of things, that can be configured.

[Documentation of plugin](https://falllazy.github.io/LazyChat_docs/) (ukr is outdated)

## For developers
### Gradle  
Add repository to your build.gradle:
```groovy
repositories {
	mavenCentral()
	maven { url 'https://jitpack.io' }
}
```
And, add the dependency
```groovy
dependencies {
    implementation 'com.github.FallLazy:LazyChat:VERSION'
    // Replace VERSION with that version, that you want
}
```
### Gradle.kts
Add repository to your build.gradle.kts
```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```
And, add the dependency
```kotlin
dependencies {
    implementation("com.github.FallLazy:LazyChat:VERSION")
    // Replace VERSION with that version, that you want
}
```
### Maven
Add repository to your pom.xml
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```
And, add the dependency
```xml
	<dependency>
        <groupId>com.github.FallLazy</groupId>
        <artifactId>LazyChat</artifactId>
        <version>VERSION</version>
    </dependency>
```

