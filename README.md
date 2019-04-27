# AndeClient
A Java 11, Library-independent, Andesite Client implementation.

Licensed under the [Apache 2.0 License](https://github.com/arudiscord/andeclient/blob/master/LICENSE).

### Installation

![Latest Version](https://api.bintray.com/packages/arudiscord/maven/andeclient/images/download.svg)

Using in Gradle:

```gradle
repositories {
  jcenter()
}

dependencies {
  compile 'pw.aru.libs:andeclient:LATEST' // replace LATEST with the version above
}
```

Using in Maven:

```xml
<repositories>
  <repository>
    <id>central</id>
    <name>bintray</name>
    <url>http://jcenter.bintray.com</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>pw.aru.libs</groupId>
    <artifactId>andeclient</artifactId>
    <version>LATEST</version> <!-- replace LATEST with the version above -->
  </dependency>
</dependencies>
```

### Usage

First, create your own instance of `AndeClient`.

```java
AndeClient andeClient = AndeClient.andeClient(botUserId).create();
```

With an instance of `AndeClient`, you can connect to nodes and create players.

```java
AndesiteNode localNode = andeClient.newNode()
    .host("127.0.0.1")
    .password("youshallnotpass")
    .create(); // automatically added to AndeClient after you create it.

AndePlayer myGuildPlayer = andeClient.newPlayer()
    .guildId(myGuildId)
    .andesiteNode(localNode)
    .create(); // automatically added to AndeClient after you create it.
```

For each player, you need to setup a `voice-server-update` handler in your Discord lib and redirect it to the player.

```java
// example with Catnip - https://github.com/mewna/catnip

Catnip catnip = Catnip.catnip("token");

catnip.connect();

catnip.on(DiscordEvent.VOICE_SERVER_UPDATE, voiceServerUpdate -> {
    String sessionId = catnip.cache().voiceState(GUILD_ID, catnip.selfUser().id()).sessionId();
    myGuildPlayer.handleVoiceServerUpdate(sessionId, voiceServerUpdate.token(), voiceServerUpdate.endpoint());
});

catnip.openVoiceConnection(GUILD_ID, VOICE_CHANNEL_ID);
```

Instances of `AndePlayer` offer a `AndePlayer#controls` method which lets you play tracks, pause the player or stop the music.

```java
AudioTrack myTrack = ...; // use AndesiteNode#loadTracksAsync or cache tracks with AudioTrackUtil#fromTrack

// plays a track
myGuildPlayer.controls()
    .play()
        .track(myTrack)
        .execute();

// pauses the player
myGuildPlayer.controls()
    .pause().execute();

// tip: you can chain calls after the execute()
// resumes the player and then sets the volume
myGuildPlayer.controls()
    .resume().execute()
    .volume(20).execute();
```

You can listen to events of a specific type, node, player or listen to all events.

```java
// listen to all events
andeClient.on(event -> System.out.println("new event: " + event));

// listen to newly created nodes
andeClient.on(EventType.NEW_NODE_EVENT, event -> System.out.println("new node v" + event.node().version() + " connected"));

// listen to all events of a node
localNode.on(event -> System.out.println("new event on local node: " + event));

// listen to all events of a player
myGuildPlayer.on(event -> System.out.println("new event on my player: " + event));

// listen to all times the player updates
myGuildPlayer.on(EventType.PLAYER_UPDATE_EVENT, event -> System.out.println("player update: " + event));
```

### Support

Support is given on [Aru's Discord Server](https://discord.gg/URPghxg)

[![Aru's Discord Server](https://discordapp.com/api/guilds/403934661627215882/embed.png?style=banner2)](https://discord.gg/URPghxg)
