package pw.aru.libs.andeclient.test;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.CatnipOptions;
import com.mewna.catnip.entity.misc.Ready;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.voice.VoiceServerUpdate;
import com.mewna.catnip.shard.DiscordEvent;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.entities.AudioLoadResult;
import pw.aru.libs.andeclient.events.EventType;
import pw.aru.libs.andeclient.events.player.PlayerUpdateEvent;
import pw.aru.libs.andeclient.events.player.update.PlayerPauseUpdateEvent;
import pw.aru.libs.andeclient.events.track.TrackEndEvent;
import pw.aru.libs.andeclient.events.track.TrackStartEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class AndeClientTest {
    private static final Logger logger = LoggerFactory.getLogger(AndeClientTest.class);

    private static final String GUILD_ID = "414412064631685131";
    private static final String VOICE_CHANNEL_ID = "414412064631685136";

    private static final String FIRST_TRACK = "https://www.youtube.com/watch?v=O6NvsM49N6w";
    private static final String SECOND_TRACK = "https://www.youtube.com/watch?v=RbY0E3-efgE&list=PLaKcZ4-75kIuJhSy-K3dML9j4G2p-LI4b&index=3&t=0s";
    private static final String THIRD_TRACK = "https://www.youtube.com/watch?v=gkime9M4z34";

    private static Process andesiteProcess;
    private static Catnip catnip;
    private static AndeClient andeClient;
    private static AndePlayer andePlayer;
    private static AndesiteNode andesiteNode;
    private static Thread logThread;

    public static void main(String[] args) {
        logger.info("Welcome to AndeClient Test Suite!");

        logger.info("Setting up test environiment.");

        try {
            setup();

            logger.info("Environiment set-up.");
            logger.info("Starting tests...");

            test1();

            System.out.println("Awaiting 7s for next test...");
            Thread.sleep(7000);

            test2();

            System.out.println("Awaiting 7s for next test...");
            Thread.sleep(7000);

            test3();

            System.out.println("Awaiting 7s for next test...");
            Thread.sleep(7000);

            test4();

            System.out.println("Awaiting 3s for next test...");
            Thread.sleep(3000);

            test5();

            System.out.println("Awaiting 7s for next test...");
            Thread.sleep(7000);

        } catch (AssertionError | Exception e) {
            e.printStackTrace();
            finish();
            System.exit(-1);
            return;
        }

        logger.info("All tests were successful, shutting down.");
        finish();
    }

    private static void test1() {
        System.out.println("[AndeClient Tests] TEST #1 - Play a single track");

        final var audioLoadResult = andesiteNode.loadTracksAsync(FIRST_TRACK).toCompletableFuture().join();

        if (!(audioLoadResult instanceof AudioLoadResult.Track)) {
            throw new AssertionError("TEST #1 FAILED - Identifier FIRST_TRACK must return a single track.");
        }

        var trackStartEvent = new CompletableFuture<TrackStartEvent>();
        try (var ignored = andePlayer.on(EventType.TRACK_START_EVENT, trackStartEvent::complete)) {
            andePlayer.controls().play()
                .track(((AudioLoadResult.Track) audioLoadResult).track())
                .submit().toCompletableFuture().get(7, TimeUnit.SECONDS);

            trackStartEvent.get(7, TimeUnit.SECONDS);
            System.out.println("[Result] SUCCESSFUL!");
        } catch (TimeoutException e) {
            throw new AssertionError("TEST #1 FAILED - Track didn't started playing within 7 seconds.");
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("TEST #1 FAILED - Something went wrong during the tests.", e);
        }
    }

    private static void test2() {
        System.out.println("[AndeClient Tests] TEST #2 - Pause and Resume");

        try {
            var playerPauseEvent = new CompletableFuture<PlayerPauseUpdateEvent>();
            try (var ignored = andePlayer.on(EventType.PLAYER_PAUSE_UPDATE_EVENT, playerPauseEvent::complete)) {
                andePlayer.controls().pause().submit().toCompletableFuture().get(15, TimeUnit.SECONDS);

                playerPauseEvent.get(7, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new AssertionError("TEST #2 FAILED - Track didn't paused within 7 seconds.");
            }

            Thread.sleep(3000);

            var playerResumeEvent = new CompletableFuture<PlayerPauseUpdateEvent>();
            try (var ignored = andePlayer.on(EventType.PLAYER_PAUSE_UPDATE_EVENT, playerResumeEvent::complete)) {
                andePlayer.controls().resume().submit().toCompletableFuture().get(7, TimeUnit.SECONDS);

                playerResumeEvent.get(7, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new AssertionError("TEST #2 FAILED - Track resumed playing within 7 seconds.");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("TEST #2 FAILED - Something went wrong during the tests.", e);
        }

        System.out.println("[Result] SUCCESSFUL!");
    }

    private static void test3() {
        System.out.println("[AndeClient Tests] TEST #3 - Volume");

        final int TARGET_VOLUME = 20;

        var volumeUpdateEvent = new CompletableFuture<PlayerUpdateEvent>();
        try (var ignored = andePlayer.on(EventType.PLAYER_UPDATE_EVENT, event -> {
            if (event.volume() == TARGET_VOLUME) volumeUpdateEvent.complete(event);
        })) {
            andePlayer.controls().volume(TARGET_VOLUME).submit().toCompletableFuture().get(7, TimeUnit.SECONDS);

            volumeUpdateEvent.get(7, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new AssertionError("TEST #2 FAILED - Track didn't changed volume within 7 seconds.");
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("TEST #2 FAILED - Something went wrong during the tests.", e);
        }

        System.out.println("[Result] SUCCESSFUL!");
    }

    private static void test4() {
        System.out.println("[AndeClient Tests] TEST #4 - Stop");

        var trackEndEvent = new CompletableFuture<TrackEndEvent>();
        try (var ignored = andePlayer.on(EventType.TRACK_END_EVENT, trackEndEvent::complete)) {
            andePlayer.controls().stop().submit().toCompletableFuture().get(7, TimeUnit.SECONDS);
            trackEndEvent.get(7, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new AssertionError("TEST #2 FAILED - Track didn't ended within 7 seconds.");
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("TEST #2 FAILED - Something went wrong during the tests.", e);
        }

        System.out.println("[Result] SUCCESSFUL!");
    }

    private static void test5() {
        System.out.println("[AndeClient Tests] TEST #5 - Play the selected track from the playlist with predefined start");

        final var audioLoadResult = andesiteNode.loadTracksAsync(SECOND_TRACK).toCompletableFuture().join();

        //if (!(audioLoadResult instanceof AudioLoadResult.Track)) {
        //    throw new AssertionError("TEST #1 FAILED - Identifier FIRST_TRACK must return a single track.");
        //}

        if (!(audioLoadResult instanceof AudioLoadResult.Playlist)) {
            throw new AssertionError("TEST #1 FAILED - Identifier FIRST_TRACK must return a playlist.");
        }

        var trackStartEvent = new CompletableFuture<TrackStartEvent>();
        try (var ignored = andePlayer.on(EventType.TRACK_START_EVENT, trackStartEvent::complete)) {
            andePlayer.controls().play()
                .track(Objects.requireNonNull(((AudioLoadResult.Playlist) audioLoadResult).selectedTrack()))
                .submit().toCompletableFuture().get(7, TimeUnit.SECONDS);

            trackStartEvent.get(21, TimeUnit.SECONDS);
            System.out.println("[Result] SUCCESSFUL!");
        } catch (TimeoutException e) {
            throw new AssertionError("TEST #5 FAILED - Track didn't started playing within 21 seconds.");
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("TEST #5 FAILED - Something went wrong during the tests.", e);
        }
    }

    private static void finish() {
        try {
            logThread.interrupt();
        } catch (Exception ignored) {}
        try {
            catnip.closeVoiceConnection(GUILD_ID);
        } catch (Exception ignored) {}
        try {
            andePlayer.destroy();
        } catch (Exception ignored) {}
        try {
            andesiteNode.destroy();
        } catch (Exception ignored) {}
        try {
            andeClient.shutdown();
        } catch (Exception ignored) {}
        try {
            andesiteProcess.destroy();
        } catch (Exception ignored) {}
        try {
            catnip.shutdown();
        } catch (Exception ignored) {}
        try {
            new File("andesite.jar").delete();
        } catch (Exception ignored) {}
        System.exit(0);
    }

    private static void setup() throws Exception {
        final var token = Files.readString(Path.of("token.txt"));
        logger.info("Downloading latest Andesite...");
        final var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
        final var jar = new File("andesite.jar");
        jar.delete();

        final var matcher = Pattern.compile("/natanbc/andesite-node/releases/download/.+/andesite-node-.+-all\\.jar")
            .matcher(client.send(
                HttpRequest.newBuilder().GET().uri(URI.create("https://github.com/natanbc/andesite-node/releases/latest")).build(),
                HttpResponse.BodyHandlers.ofString()
            ).body());

        if (!matcher.find()) {
            logger.error("Couldn't find Andesite link");
            throw new Exception("Andesite couldn't be downloaded.");
        }

        client.send(
            HttpRequest.newBuilder().GET().uri(URI.create("https://github.com/" + matcher.group())).build(),
            HttpResponse.BodyHandlers.ofFile(jar.toPath())
        );
        logger.info("Downloaded!");

        logger.info("Starting up Andesite...");
        andesiteProcess = new ProcessBuilder().command("java", "-jar", "andesite.jar").start();

        logThread = new Thread() {
            {
                start();
            }

            @Override
            public void run() {
                final var reader = new BufferedReader(new InputStreamReader(andesiteProcess.getInputStream()));
                while (!interrupted()) {
                    try {
                        final var x = reader.readLine();
                        if (x == null) return;
                        System.out.println(x);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        logger.info("Logging in through Discord using Catnip...");
        catnip = Catnip.catnip(
            new CatnipOptions(token)
                .presence(Presence.of(Presence.OnlineStatus.DND, Presence.Activity.of("AndeClient Test!!!", Presence.ActivityType.PLAYING))),
            Vertx.vertx(
                new VertxOptions()
                    .setAddressResolverOptions(new AddressResolverOptions().addServer("8.8.8.8"))
            )
        );
        final var ready = new CompletableFuture<Ready>();


        final var readySubscription = catnip.on(DiscordEvent.READY, ready::complete);
        catnip.connect();
        ready.join();
        readySubscription.unregister();

        logger.info("Connected!");

        logger.info("Process will wait 3000ms for Catnip and Andesite to fully load.");
        Thread.sleep(3000);

        logger.info("Creating AndeClient...");
        andeClient = AndeClient.andeClient(Objects.requireNonNull(catnip.selfUser(), "catnip.selfUser()").idAsLong()).create();

        logger.info("Connecting to local node...");
        andesiteNode = andeClient.newNode().create();

        logger.info("Creating AndePlayer for guild {}", GUILD_ID);

        andePlayer = andeClient.newPlayer()
            .guildId(Long.parseUnsignedLong(GUILD_ID))
            .andesiteNode(andesiteNode)
            .create();

        logger.info("Opening voice connection for channel {}", VOICE_CHANNEL_ID);
        final var vsu = new CompletableFuture<VoiceServerUpdate>();

        catnip.on(DiscordEvent.VOICE_SERVER_UPDATE, value -> {
            vsu.complete(value);
            andePlayer.handleVoiceServerUpdate(
                Objects.requireNonNull(catnip.cache().voiceState(GUILD_ID, Objects.requireNonNull(catnip.selfUser()).id())).sessionId(),
                value.token(),
                value.endpoint()
            );
        });
        catnip.openVoiceConnection(GUILD_ID, VOICE_CHANNEL_ID);

        vsu.join();
    }
}
