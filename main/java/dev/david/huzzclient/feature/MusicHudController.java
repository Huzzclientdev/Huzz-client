package dev.david.huzzclient.feature;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

public final class MusicHudController {
    private static final char DELIMITER = 0x1F;
    private static final String DELIMITER_STR = Character.toString(DELIMITER);
    private static final long POLL_INTERVAL_MS = 1500L;
    private static final long COMMAND_TIMEOUT_MS = 3500L;
    private static final long HELPER_EXEC_TIMEOUT_MS = 6000L;
    private static final long HELPER_BUILD_TIMEOUT_MS = 30000L;
    private static final long HELPER_RETRY_DELAY_MS = 60000L;
    private static final int HTTP_CONNECT_TIMEOUT_MS = 2500;
    private static final int HTTP_READ_TIMEOUT_MS = 3500;
    private static final String HTTP_USER_AGENT = "Mozilla/5.0 (HuzzClient MusicHud)";
    private static final String MAC_HELPER_ENV = "HUZZCLIENT_MUSIC_HELPER";
    private static final String MAC_HELPER_VERSION = "2";
    private static final String MAC_HELPER_BUNDLE_NAME = "MusicHudHelper-v" + MAC_HELPER_VERSION + ".app";
    private static final String MAC_HELPER_EXECUTABLE_NAME = "MusicHudHelper";
    private static final String MAC_HELPER_RESOURCE_ROOT = "native/macos/music-hud-helper/";

    private final HuzzConfigManager configManager;
    private static Path macMusicHelperExecutable;
    private static long nextMacMusicHelperBuildAt;
    private CompletableFuture<RawMusicSnapshot> inFlightFetch;
    private long nextPollAt;
    private String coverKey = "";
    private Identifier coverTextureId;
    private int coverWidth;
    private int coverHeight;
    private int coverThemeColor = 0xFF7ED8FF;
    private MusicSnapshot snapshot = MusicSnapshot.empty();

    public MusicHudController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (inFlightFetch != null && inFlightFetch.isDone()) {
            RawMusicSnapshot raw = safelyJoin(inFlightFetch);
            inFlightFetch = null;
            applySnapshot(client, raw);
        }

        if (client.player == null || client.world == null) {
            clear(client);
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isHudMusicEnabled()) {
            clear(client);
            return;
        }

        long now = Util.getMeasuringTimeMs();
        if (now < nextPollAt || inFlightFetch != null) {
            return;
        }

        String previousCoverKey = coverKey;
        boolean previousHasCover = coverTextureId != null;
        HuzzConfig.MusicHudSource source = config.getMusicHudSource();
        inFlightFetch = CompletableFuture.supplyAsync(() -> fetchSnapshot(source, previousCoverKey, previousHasCover));
        nextPollAt = now + POLL_INTERVAL_MS;
    }

    public void clear() {
        clear(MinecraftClient.getInstance());
    }

    public void clear(MinecraftClient client) {
        if (inFlightFetch != null) {
            inFlightFetch.cancel(true);
            inFlightFetch = null;
        }
        nextPollAt = 0L;
        coverKey = "";
        coverWidth = 0;
        coverHeight = 0;
        coverThemeColor = 0xFF7ED8FF;
        snapshot = MusicSnapshot.empty();
        if (coverTextureId != null && client != null) {
            client.getTextureManager().destroyTexture(coverTextureId);
            coverTextureId = null;
        }
    }

    public MusicSnapshot getSnapshot() {
        if (!snapshot.active()) {
            return snapshot;
        }

        long now = Util.getMeasuringTimeMs();
        long progressed = snapshot.positionMs() + Math.max(0L, now - snapshot.updatedAtMs());
        long duration = Math.max(0L, snapshot.durationMs());
        if (duration > 0L) {
            progressed = Math.min(duration, progressed);
        }
        return new MusicSnapshot(
            true,
            snapshot.title(),
            snapshot.artist(),
            snapshot.album(),
            duration,
            progressed,
            snapshot.coverTextureId(),
            snapshot.coverWidth(),
            snapshot.coverHeight(),
            snapshot.coverThemeColor(),
            snapshot.updatedAtMs()
        );
    }

    private void applySnapshot(MinecraftClient client, RawMusicSnapshot raw) {
        debugLog("applySnapshot active={} keepExistingCover={} coverKey={} coverBytes={}",
            raw != null && raw.active(),
            raw != null && raw.keepExistingCover(),
            raw == null ? "" : preview(raw.coverKey()),
            raw == null || raw.coverBytes() == null ? 0 : raw.coverBytes().length
        );
        if (raw == null || !raw.active()) {
            snapshot = MusicSnapshot.empty();
            if (coverTextureId != null) {
                client.getTextureManager().destroyTexture(coverTextureId);
                coverTextureId = null;
                coverKey = "";
                coverWidth = 0;
                coverHeight = 0;
                coverThemeColor = 0xFF7ED8FF;
            }
            return;
        }

        if (!raw.keepExistingCover()) {
            if (coverTextureId != null) {
                client.getTextureManager().destroyTexture(coverTextureId);
                coverTextureId = null;
            }

            coverKey = raw.coverKey();
            if (raw.coverBytes() != null && raw.coverBytes().length > 0) {
                try {
                    NativeImage image = NativeImage.read(new ByteArrayInputStream(raw.coverBytes()));
                    coverWidth = image.getWidth();
                    coverHeight = image.getHeight();
                    coverThemeColor = computeThemeColor(image);
                    debugLog("cover decode success width={} height={} key={}", coverWidth, coverHeight, preview(raw.coverKey()));
                    NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> "musichud_cover", image);
                    coverTextureId = Identifier.of(HuzzClient.MOD_ID, "musichud/" + Integer.toHexString(raw.coverKey().hashCode()));
                    client.getTextureManager().registerTexture(coverTextureId, texture);
                } catch (IOException exception) {
                    debugLog("cover decode failed key={} err={}", preview(raw.coverKey()), preview(exception.getMessage()));
                    coverTextureId = null;
                    coverWidth = 0;
                    coverHeight = 0;
                    coverThemeColor = 0xFF7ED8FF;
                }
            }

            if (coverTextureId == null) {
                // Retry this cover on next poll instead of caching a failed key.
                coverKey = "";
                coverThemeColor = 0xFF7ED8FF;
            }
        }

        snapshot = new MusicSnapshot(
            true,
            raw.title(),
            raw.artist(),
            raw.album(),
            raw.durationMs(),
            raw.positionMs(),
            coverTextureId,
            coverWidth,
            coverHeight,
            coverThemeColor,
            Util.getMeasuringTimeMs()
        );
    }

    private static RawMusicSnapshot fetchSnapshot(HuzzConfig.MusicHudSource source, String previousCoverKey, boolean previousHasCover) {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return switch (source) {
            case MAC_SPOTIFY_APPLESCRIPT -> osName.contains("mac")
                ? fetchMacSpotify(previousCoverKey, previousHasCover)
                : RawMusicSnapshot.inactive();
            case MAC_APPLE_MUSIC_APPLESCRIPT -> osName.contains("mac")
                ? fetchMacAppleMusic(previousCoverKey, previousHasCover)
                : RawMusicSnapshot.inactive();
            case WINDOWS_SMTC -> osName.contains("win")
                ? fetchWindowsSmtc(previousCoverKey, previousHasCover)
                : RawMusicSnapshot.inactive();
        };
    }

    private static RawMusicSnapshot fetchMacSpotify(String previousCoverKey, boolean previousHasCover) {
        String output = runCommand(List.of(
            "osascript",
            "-e", "if application \"Spotify\" is not running then return \"\"",
            "-e", "tell application \"Spotify\"",
            "-e", "if player state is not playing then return \"\"",
            "-e", "set t to current track",
            "-e", "set d to character id 31",
            "-e", "return (name of t as text) & d & (artist of t as text) & d & (album of t as text) & d & (duration of t as text) & d & (player position as text) & d & (artwork url of t as text)",
            "-e", "end tell"
        ));
        if (output.isBlank()) {
            return RawMusicSnapshot.inactive();
        }

        String[] parts = output.split(DELIMITER_STR, 6);
        if (parts.length < 6) {
            return RawMusicSnapshot.inactive();
        }

        String coverRef = sanitizeCoverRef(parts[5]);
        long durationMs = parseDurationToMs(parts[3]);
        long positionMs = parseDurationToMs(parts[4]);
        return createRawSnapshot(parts[0], parts[1], parts[2], durationMs, positionMs, coverRef, previousCoverKey, previousHasCover);
    }

    private static RawMusicSnapshot fetchMacAppleMusic(String previousCoverKey, boolean previousHasCover) {
        RawMusicSnapshot helperSnapshot = fetchMacAppleMusicHelper(previousCoverKey, previousHasCover);
        if (helperSnapshot.active()) {
            if (helperSnapshot.coverKey().isBlank() && !helperSnapshot.keepExistingCover()) {
                RawMusicSnapshot appleScriptSnapshot = fetchMacAppleMusicAppleScript(previousCoverKey, previousHasCover);
                boolean appleScriptCoverUsable = appleScriptSnapshot.active() && (
                    appleScriptSnapshot.keepExistingCover()
                        || (!appleScriptSnapshot.coverKey().isBlank()
                        && appleScriptSnapshot.coverBytes() != null
                        && appleScriptSnapshot.coverBytes().length > 0
                        && canDecodeImage(appleScriptSnapshot.coverBytes()))
                );
                if (appleScriptCoverUsable) {
                    debugLog(
                        "helper had no cover; merged applescript cover key={} keepExistingCover={}",
                        preview(appleScriptSnapshot.coverKey()),
                        appleScriptSnapshot.keepExistingCover()
                    );
                    return new RawMusicSnapshot(
                        true,
                        helperSnapshot.title(),
                        helperSnapshot.artist(),
                        helperSnapshot.album(),
                        helperSnapshot.durationMs(),
                        helperSnapshot.positionMs(),
                        appleScriptSnapshot.coverKey(),
                        appleScriptSnapshot.coverBytes(),
                        appleScriptSnapshot.keepExistingCover()
                    );
                }
                if (appleScriptSnapshot.active() && !appleScriptSnapshot.coverKey().isBlank() && !appleScriptSnapshot.keepExistingCover()) {
                    debugLog("helper had no cover; applescript cover was invalid key={}", preview(appleScriptSnapshot.coverKey()));
                }
                String remoteCoverUrl = queryItunesSearchArtwork(helperSnapshot.title(), helperSnapshot.artist(), helperSnapshot.album());
                if (!remoteCoverUrl.isBlank()) {
                    debugLog("helper had no cover; using itunes artwork url={}", preview(remoteCoverUrl));
                    return createRawSnapshot(
                        helperSnapshot.title(),
                        helperSnapshot.artist(),
                        helperSnapshot.album(),
                        helperSnapshot.durationMs(),
                        helperSnapshot.positionMs(),
                        remoteCoverUrl,
                        previousCoverKey,
                        previousHasCover
                    );
                }
                debugLog("helper had no cover; applescript and itunes search fallbacks unavailable");
            }
            return helperSnapshot;
        }
        RawMusicSnapshot appleScriptSnapshot = fetchMacAppleMusicAppleScript(previousCoverKey, previousHasCover);
        if (appleScriptSnapshot.active() && appleScriptSnapshot.coverKey().isBlank() && !appleScriptSnapshot.keepExistingCover()) {
            String remoteCoverUrl = queryItunesSearchArtwork(appleScriptSnapshot.title(), appleScriptSnapshot.artist(), appleScriptSnapshot.album());
            if (!remoteCoverUrl.isBlank()) {
                debugLog("applescript had no cover; using itunes artwork url={}", preview(remoteCoverUrl));
                return createRawSnapshot(
                    appleScriptSnapshot.title(),
                    appleScriptSnapshot.artist(),
                    appleScriptSnapshot.album(),
                    appleScriptSnapshot.durationMs(),
                    appleScriptSnapshot.positionMs(),
                    remoteCoverUrl,
                    previousCoverKey,
                    previousHasCover
                );
            }
        }
        return appleScriptSnapshot;
    }

    private static RawMusicSnapshot fetchMacAppleMusicAppleScript(String previousCoverKey, boolean previousHasCover) {
        Path coverPath;
        try {
            coverPath = Files.createTempFile("huzzclient-musichud-", ".png");
            Files.deleteIfExists(coverPath);
        } catch (IOException exception) {
            return RawMusicSnapshot.inactive();
        }

        String appleScriptCoverPath = escapeAppleScriptString(coverPath.toAbsolutePath().toString());
        String output = runCommand(List.of(
            "osascript",
            "-e", "if application \"Music\" is not running then return \"\"",
            "-e", "tell application \"Music\"",
            "-e", "set t to current track",
            "-e", "set trackId to persistent ID of t as text",
            "-e", "set coverPath to \"\"",
            "-e", "if (count of artworks of t) > 0 then",
            "-e", "set coverPath to \"" + appleScriptCoverPath + "\"",
            "-e", "try",
            "-e", "save artwork 1 of t in POSIX file coverPath",
            "-e", "on error",
            "-e", "try",
            "-e", "set fileRef to open for access POSIX file coverPath with write permission",
            "-e", "set eof fileRef to 0",
            "-e", "write (raw data of artwork 1 of t) to fileRef",
            "-e", "close access fileRef",
            "-e", "on error",
            "-e", "try",
            "-e", "close access POSIX file coverPath",
            "-e", "end try",
            "-e", "set coverPath to \"\"",
            "-e", "end try",
            "-e", "end try",
            "-e", "end if",
            "-e", "set d to character id 31",
            "-e", "return (name of t as text) & d & (artist of t as text) & d & (album of t as text) & d & (duration of t as text) & d & (player position as text) & d & coverPath & d & trackId",
            "-e", "end tell"
        ));
        if (output.isBlank()) {
            return RawMusicSnapshot.inactive();
        }

        String[] parts = output.split(DELIMITER_STR, 7);
        if (parts.length < 7) {
            return RawMusicSnapshot.inactive();
        }

        String coverRef = sanitizeCoverRef(parts[5]);
        long durationMs = parseDurationToMs(parts[3]);
        long positionMs = parseDurationToMs(parts[4]);
        String trackId = sanitize(parts[6]);
        String coverKeyRef = coverRef.isEmpty() ? "" : coverRef + "#" + trackId;
        return createRawSnapshot(parts[0], parts[1], parts[2], durationMs, positionMs, coverKeyRef, previousCoverKey, previousHasCover);
    }

    private static RawMusicSnapshot fetchMacAppleMusicHelper(String previousCoverKey, boolean previousHasCover) {
        Path helperExecutable = resolveMacMusicHelperExecutable();
        if (helperExecutable == null) {
            debugLog("helper executable unavailable");
            return RawMusicSnapshot.inactive();
        }

        CommandResult result = runCommandDetailed(
            List.of(helperExecutable.toString(), "--source", "apple-music"),
            HELPER_EXEC_TIMEOUT_MS,
            true
        );
        debugLog(
            "helper exec={} exit={} stdout={} stderr={}",
            helperExecutable,
            result == null ? -1 : result.exitCode(),
            result == null ? "" : preview(result.stdout().replace(DELIMITER, '|')),
            result == null ? "" : preview(result.stderr())
        );
        if (result == null || result.exitCode() != 0 || result.stdout().isBlank()) {
            return RawMusicSnapshot.inactive();
        }

        String[] parts = result.stdout().split(DELIMITER_STR, 7);
        debugLog("helper parts count={} p0={} p1={} p2={} p3={} p4={} p5={} p6={}",
            parts.length,
            part(parts, 0),
            part(parts, 1),
            part(parts, 2),
            part(parts, 3),
            part(parts, 4),
            part(parts, 5),
            part(parts, 6)
        );
        if (parts.length < 7) {
            return RawMusicSnapshot.inactive();
        }

        String title = sanitize(parts[0]);
        if (title.isEmpty()) {
            return RawMusicSnapshot.inactive();
        }

        String coverRef = sanitizeCoverRef(parts[5]);
        String trackId = sanitize(parts[6]);
        long durationMs = Math.max(0L, parseLong(parts[3]));
        long positionMs = Math.max(0L, parseLong(parts[4]));
        debugLog("helper parsed title={} artist={} album={} durationMs={} positionMs={} coverRef={} trackId={}",
            preview(title), preview(parts[1]), preview(parts[2]), durationMs, positionMs, preview(coverRef), preview(trackId)
        );
        String coverKeyRef = coverRef.isEmpty() ? "" : coverRef + "#" + trackId;
        return createRawSnapshot(title, parts[1], parts[2], durationMs, positionMs, coverKeyRef, previousCoverKey, previousHasCover);
    }

    private static RawMusicSnapshot fetchWindowsSmtc(String previousCoverKey, boolean previousHasCover) {
        String script = """
            $ErrorActionPreference='Stop'
            $winmd=Join-Path $env:windir 'System32\\WinMetadata\\Windows.winmd'
            $framework=if([Environment]::Is64BitProcess){Join-Path $env:windir 'Microsoft.NET\\Framework64\\v4.0.30319\\System.Runtime.WindowsRuntime.dll'}else{Join-Path $env:windir 'Microsoft.NET\\Framework\\v4.0.30319\\System.Runtime.WindowsRuntime.dll'}
            $refs=@()
            if(Test-Path $framework){$refs += $framework}else{$refs += 'System.Runtime.WindowsRuntime'}
            if(Test-Path $winmd){$refs += $winmd}
            [void][System.Reflection.Assembly]::LoadWithPartialName('System.Runtime.WindowsRuntime')
            $source=@"
            using System;
            using System.Runtime.InteropServices.WindowsRuntime;
            using Windows.Media.Control;
            using Windows.Storage.Streams;

            public static class HuzzSmtcBridge {
                public static string Fetch() {
                    try {
                        string d = ((char)31).ToString();
                        var manager = GlobalSystemMediaTransportControlsSessionManager.RequestAsync().AsTask().GetAwaiter().GetResult();
                        if (manager == null) return "";
                        var session = manager.GetCurrentSession();
                        if (session == null) return "";
                        var media = session.TryGetMediaPropertiesAsync().AsTask().GetAwaiter().GetResult();
                        if (media == null) return "";
                        var timeline = session.GetTimelineProperties();
                        string thumb = "";
                        if (media.Thumbnail != null) {
                            using (var stream = media.Thumbnail.OpenReadAsync().AsTask().GetAwaiter().GetResult()) {
                                if (stream != null && stream.Size > 0) {
                                    using (var input = stream.GetInputStreamAt(0))
                                    using (var reader = new DataReader(input)) {
                                        reader.LoadAsync((uint)stream.Size).AsTask().GetAwaiter().GetResult();
                                        var bytes = new byte[(int)stream.Size];
                                        reader.ReadBytes(bytes);
                                        thumb = Convert.ToBase64String(bytes);
                                    }
                                }
                            }
                        }
                        return Clean(media.Title) + d + Clean(media.Artist) + d + Clean(media.AlbumTitle) + d
                            + ToMilliseconds(timeline.EndTime) + d
                            + ToMilliseconds(timeline.Position) + d + thumb;
                    } catch {
                        return "";
                    }
                }

                private static string ToMilliseconds(TimeSpan value) {
                    return Math.Max(0L, (long)value.TotalMilliseconds).ToString();
                }

                private static string Clean(string value) {
                    if (string.IsNullOrWhiteSpace(value)) return "";
                    return value.Replace("\\r", " ").Replace("\\n", " ").Replace(((char)31).ToString(), " ").Trim();
                }
            }
            "@
            Add-Type -Language CSharp -ReferencedAssemblies $refs -TypeDefinition $source | Out-Null
            [HuzzSmtcBridge]::Fetch()
            """;

        String output = runPowerShellScript(script);
        if (output.isBlank()) {
            return RawMusicSnapshot.inactive();
        }

        String[] parts = output.split(DELIMITER_STR, 6);
        if (parts.length < 6) {
            return RawMusicSnapshot.inactive();
        }

        String base64 = parts[5].trim();
        String coverRef = base64.isEmpty() ? "" : "base64:" + Integer.toHexString(base64.hashCode()) + ":" + base64.length();
        boolean keepCover = previousHasCover && !coverRef.isEmpty() && coverRef.equals(previousCoverKey);
        byte[] coverBytes = null;
        if (!keepCover && !base64.isBlank()) {
            try {
                coverBytes = Base64.getMimeDecoder().decode(base64);
            } catch (IllegalArgumentException ignored) {
                coverBytes = null;
            }
        }

        return new RawMusicSnapshot(
            true,
            sanitize(parts[0]),
            sanitize(parts[1]),
            sanitize(parts[2]),
            Math.max(0L, parseLong(parts[3])),
            Math.max(0L, parseLong(parts[4])),
            coverRef,
            coverBytes,
            keepCover
        );
    }

    private static RawMusicSnapshot createRawSnapshot(
        String title,
        String artist,
        String album,
        long durationMs,
        long positionMs,
        String coverRef,
        String previousCoverKey,
        boolean previousHasCover
    ) {
        String normalizedCoverRef = sanitizeCoverRef(coverRef);
        boolean keepCover = previousHasCover && !normalizedCoverRef.isEmpty() && normalizedCoverRef.equals(previousCoverKey);
        debugLog("createRawSnapshot title={} durationMs={} positionMs={} coverRef={} keepCover={} previousHasCover={}",
            preview(title), durationMs, positionMs, preview(normalizedCoverRef), keepCover, previousHasCover
        );
        byte[] coverBytes = null;
        if (!keepCover && !normalizedCoverRef.isEmpty()) {
            coverBytes = loadCoverBytes(normalizedCoverRef);
        }

        return new RawMusicSnapshot(
            true,
            sanitize(title),
            sanitize(artist),
            sanitize(album),
            Math.max(0L, durationMs),
            Math.max(0L, positionMs),
            normalizedCoverRef,
            coverBytes,
            keepCover
        );
    }

    private static synchronized Path resolveMacMusicHelperExecutable() {
        Path configured = configuredMacMusicHelperExecutable();
        if (configured != null) {
            macMusicHelperExecutable = configured;
            return configured;
        }

        if (macMusicHelperExecutable != null && Files.isRegularFile(macMusicHelperExecutable)) {
            return macMusicHelperExecutable;
        }

        Path generatedExecutable = defaultMacMusicHelperExecutable();
        if (Files.isRegularFile(generatedExecutable)) {
            macMusicHelperExecutable = generatedExecutable;
            return generatedExecutable;
        }

        long now = System.currentTimeMillis();
        if (now < nextMacMusicHelperBuildAt) {
            return null;
        }
        nextMacMusicHelperBuildAt = now + HELPER_RETRY_DELAY_MS;

        if (buildMacMusicHelperApp(generatedExecutable)) {
            macMusicHelperExecutable = generatedExecutable;
            return generatedExecutable;
        }
        return null;
    }

    private static Path configuredMacMusicHelperExecutable() {
        String configured = System.getenv(MAC_HELPER_ENV);
        if (configured == null || configured.isBlank()) {
            configured = System.getProperty(MAC_HELPER_ENV);
        }
        if (configured == null || configured.isBlank()) {
            return null;
        }

        Path path = Path.of(configured.trim());
        if (Files.isDirectory(path) && path.getFileName() != null && path.getFileName().toString().endsWith(".app")) {
            path = path.resolve("Contents").resolve("MacOS").resolve(MAC_HELPER_EXECUTABLE_NAME);
        }
        return Files.isRegularFile(path) ? path : null;
    }

    private static Path defaultMacMusicHelperExecutable() {
        return macMusicHelperAppRoot().resolve(MAC_HELPER_BUNDLE_NAME).resolve("Contents").resolve("MacOS").resolve(MAC_HELPER_EXECUTABLE_NAME);
    }

    private static Path macMusicHelperAppRoot() {
        String home = System.getProperty("user.home", "");
        if (home.isBlank()) {
            return Path.of(System.getProperty("java.io.tmpdir", "."), HuzzClient.MOD_ID, "music-helper");
        }
        return Path.of(home, "Library", "Application Support", HuzzClient.MOD_ID, "music-helper-v" + MAC_HELPER_VERSION);
    }

    private static boolean buildMacMusicHelperApp(Path executablePath) {
        try {
            Path helperRoot = macMusicHelperAppRoot();
            Path appDir = helperRoot.resolve(MAC_HELPER_BUNDLE_NAME);
            Path contentsDir = appDir.resolve("Contents");
            Path macOsDir = contentsDir.resolve("MacOS");
            Path resourcesDir = contentsDir.resolve("Resources");
            Path sourceDir = helperRoot.resolve("source");
            Path sourceFile = sourceDir.resolve("main.m");
            Path plistPath = contentsDir.resolve("Info.plist");

            Files.createDirectories(macOsDir);
            Files.createDirectories(resourcesDir);
            Files.createDirectories(sourceDir);
            writeResourceText(MAC_HELPER_RESOURCE_ROOT + "main.m", sourceFile);
            writeResourceText(MAC_HELPER_RESOURCE_ROOT + "Info.plist", plistPath);

            CommandResult compile = runCommandDetailed(
                List.of(
                    "clang",
                    "-fobjc-arc",
                    sourceFile.toString(),
                    "-o", executablePath.toString(),
                    "-framework", "Foundation",
                    "-framework", "AppKit",
                    "-framework", "iTunesLibrary"
                ),
                HELPER_BUILD_TIMEOUT_MS,
                false
            );
            if (compile == null || compile.exitCode() != 0 || !Files.isRegularFile(executablePath)) {
                logHelperBuildFailure("compile", compile);
                return false;
            }

            runCommandDetailed(List.of("chmod", "+x", executablePath.toString()), COMMAND_TIMEOUT_MS, false);
            CommandResult sign = runCommandDetailed(
                List.of("codesign", "--force", "--deep", "--sign", "-", appDir.toString()),
                HELPER_BUILD_TIMEOUT_MS,
                false
            );
            if (sign == null || sign.exitCode() != 0) {
                logHelperBuildFailure("codesign", sign);
                return false;
            }
            return true;
        } catch (IOException exception) {
            HuzzClient.LOGGER.warn("Failed to prepare macOS Music HUD helper", exception);
            return false;
        }
    }

    private static void writeResourceText(String resourcePath, Path outputPath) throws IOException {
        try (InputStream inputStream = MusicHudController.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Missing resource " + resourcePath);
            }
            Files.writeString(outputPath, new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        }
    }

    private static void logHelperBuildFailure(String stage, CommandResult result) {
        if (result == null) {
            HuzzClient.LOGGER.warn("macOS Music HUD helper {} failed without process result", stage);
            return;
        }
        String stderr = sanitize(result.stderr());
        if (stderr.isEmpty()) {
            stderr = sanitize(result.stdout());
        }
        HuzzClient.LOGGER.warn(
            "macOS Music HUD helper {} failed with exit code {}{}",
            stage,
            result.exitCode(),
            stderr.isEmpty() ? "" : ": " + stderr
        );
    }

    private static byte[] loadCoverBytes(String coverRef) {
        try {
            String normalized = normalizeCoverReference(coverRef);
            debugLog("loadCoverBytes ref={} normalized={}", preview(coverRef), preview(normalized));
            if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
                URL url = new URL(normalized);
                var connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(HTTP_READ_TIMEOUT_MS);
                connection.setRequestProperty("User-Agent", HTTP_USER_AGENT);
                try (var inputStream = connection.getInputStream()) {
                    byte[] bytes = inputStream.readAllBytes();
                    debugLog("loadCoverBytes http bytes={} url={}", bytes.length, preview(normalized));
                    byte[] normalizedBytes = normalizeCoverBytes(bytes);
                    debugLog("loadCoverBytes http normalizedBytes={} url={}", normalizedBytes == null ? 0 : normalizedBytes.length, preview(normalized));
                    return normalizedBytes;
                }
            }

            if (normalized.startsWith("file://")) {
                normalized = normalized.substring("file://".length());
            }

            if (normalized.startsWith("~/")) {
                normalized = System.getProperty("user.home", "") + normalized.substring(1);
            }

            Path path = Path.of(normalized);
            if (Files.exists(path)) {
                byte[] bytes = Files.readAllBytes(path);
                debugLog("loadCoverBytes file path={} exists=true size={}", path, bytes.length);
                byte[] normalizedBytes = normalizeCoverBytes(bytes);
                if (normalizedBytes != null && canDecodeImage(normalizedBytes)) {
                    debugLog("loadCoverBytes file decode=direct-or-normalized-ok path={} size={}", path, normalizedBytes.length);
                    return normalizedBytes;
                }

                byte[] converted = convertImageFileToPngBytes(path);
                debugLog("loadCoverBytes file decode=convert attempted success={} path={}", converted != null, path);
                return converted;
            }
            debugLog("loadCoverBytes file path={} exists=false", path);
        } catch (IOException ignored) {
            debugLog("loadCoverBytes failed ref={} err={}", preview(coverRef), preview(ignored.getMessage()));
            return null;
        }
        return null;
    }

    private static byte[] normalizeCoverBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if (canDecodeImage(bytes)) {
            return bytes;
        }

        try {
            var image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return null;
            }
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                if (!ImageIO.write(image, "png", output)) {
                    return null;
                }
                byte[] pngBytes = output.toByteArray();
                return canDecodeImage(pngBytes) ? pngBytes : null;
            }
        } catch (IOException exception) {
            return null;
        }
    }

    private static String queryItunesSearchArtwork(String title, String artist, String album) {
        String query = sanitize(title) + " " + sanitize(artist);
        if (query.isBlank()) {
            query = sanitize(album);
        }
        if (query.isBlank()) {
            return "";
        }

        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            URL url = new URL("https://itunes.apple.com/search?media=music&entity=song&limit=5&term=" + encoded);
            var connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(HTTP_READ_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", HTTP_USER_AGENT);
            try (var inputStream = connection.getInputStream()) {
                String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String artwork = extractJsonString(body, "artworkUrl100");
                if (artwork.isBlank()) {
                    artwork = extractJsonString(body, "artworkUrl60");
                }
                if (artwork.isBlank()) {
                    debugLog("itunes artwork search found no artwork query={}", preview(query));
                    return "";
                }

                String normalized = artwork
                    .replace("\\/", "/")
                    .replace("100x100bb", "600x600bb")
                    .replace("60x60bb", "600x600bb");
                debugLog("itunes artwork search query={} url={}", preview(query), preview(normalized));
                return normalized;
            }
        } catch (IOException exception) {
            debugLog("itunes artwork search failed query={} err={}", preview(query), preview(exception.getMessage()));
            return "";
        }
    }

    private static String extractJsonString(String json, String key) {
        if (json == null || json.isBlank() || key == null || key.isBlank()) {
            return "";
        }

        String token = "\"" + key + "\":\"";
        int start = json.indexOf(token);
        if (start < 0) {
            return "";
        }
        start += token.length();

        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                value.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                value.append(c);
                continue;
            }
            if (c == '"') {
                break;
            }
            value.append(c);
        }
        return value.toString();
    }

    private static String runCommand(List<String> command) {
        CommandResult result = runCommandDetailed(command, COMMAND_TIMEOUT_MS, true);
        return result == null || result.exitCode() != 0 ? "" : result.stdout();
    }

    private static CommandResult runCommandDetailed(List<String> command, long timeoutMs, boolean discardError) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (discardError) {
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
        }
        try {
            Process process = processBuilder.start();
            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return null;
            }

            String stdout;
            try (InputStream input = process.getInputStream()) {
                stdout = new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
            }

            String stderr = "";
            if (!discardError) {
                try (InputStream error = process.getErrorStream()) {
                    stderr = new String(error.readAllBytes(), StandardCharsets.UTF_8).trim();
                }
            }

            return new CommandResult(process.exitValue(), stdout, stderr);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException exception) {
            return null;
        }
    }

    private static String runPowerShellScript(String script) {
        String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        for (String executable : List.of("powershell.exe", "pwsh.exe", "pwsh")) {
            String output = runCommand(List.of(
                executable,
                "-NoProfile",
                "-NonInteractive",
                "-ExecutionPolicy", "Bypass",
                "-EncodedCommand", encoded
            ));
            if (!output.isBlank()) {
                return output;
            }
        }
        return "";
    }

    private static long parseDurationToMs(String raw) {
        double value = parseDouble(raw);
        if (value <= 0.0D) {
            return 0L;
        }
        return value > 1000.0D ? Math.round(value) : Duration.ofMillis(Math.round(value * 1000.0D)).toMillis();
    }

    private static double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0.0D;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException exception) {
            return 0.0D;
        }
    }

    private static long parseLong(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\r', ' ').replace('\n', ' ').trim();
    }

    private static String sanitizeCoverRef(String value) {
        String clean = sanitize(value);
        if (clean.equalsIgnoreCase("missing value")) {
            return "";
        }
        return clean;
    }

    private static String escapeAppleScriptString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String normalizeCoverReference(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim();
        int suffixIndex = normalized.lastIndexOf('#');
        if (suffixIndex > 0 && !normalized.startsWith("http://") && !normalized.startsWith("https://") && !normalized.startsWith("spotify:image:")) {
            normalized = normalized.substring(0, suffixIndex);
        }
        if (normalized.startsWith("spotify:image:")) {
            String imageId = normalized.substring("spotify:image:".length()).trim();
            if (!imageId.isEmpty()) {
                return "https://i.scdn.co/image/" + imageId;
            }
        }
        return normalized;
    }

    private static boolean canDecodeImage(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        try (NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes))) {
            return image != null;
        } catch (IOException exception) {
            return false;
        }
    }

    private static byte[] convertImageFileToPngBytes(Path inputPath) {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!osName.contains("mac")) {
            return null;
        }

        try {
            Path outputPath = Files.createTempFile("huzzclient-musichud-converted-", ".png");
            try {
                runCommand(List.of("sips", "-s", "format", "png", inputPath.toString(), "--out", outputPath.toString()));
                if (Files.exists(outputPath) && Files.size(outputPath) > 0L) {
                    byte[] bytes = Files.readAllBytes(outputPath);
                    return canDecodeImage(bytes) ? bytes : null;
                }
            } finally {
                Files.deleteIfExists(outputPath);
            }
        } catch (IOException ignored) {
            return null;
        }
        return null;
    }

    private static RawMusicSnapshot safelyJoin(CompletableFuture<RawMusicSnapshot> future) {
        try {
            return future.getNow(RawMusicSnapshot.inactive());
        } catch (Exception exception) {
            return RawMusicSnapshot.inactive();
        }
    }

    public record MusicSnapshot(
        boolean active,
        String title,
        String artist,
        String album,
        long durationMs,
        long positionMs,
        Identifier coverTextureId,
        int coverWidth,
        int coverHeight,
        int coverThemeColor,
        long updatedAtMs
    ) {
        public static MusicSnapshot empty() {
            return new MusicSnapshot(false, "", "", "", 0L, 0L, null, 0, 0, 0xFF7ED8FF, 0L);
        }
    }

    private record RawMusicSnapshot(
        boolean active,
        String title,
        String artist,
        String album,
        long durationMs,
        long positionMs,
        String coverKey,
        byte[] coverBytes,
        boolean keepExistingCover
    ) {
        static RawMusicSnapshot inactive() {
            return new RawMusicSnapshot(false, "", "", "", 0L, 0L, "", null, false);
        }
    }

    private record CommandResult(int exitCode, String stdout, String stderr) {
    }

    private static int computeThemeColor(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= 0 || height <= 0) {
            return 0xFF7ED8FF;
        }

        int stepX = Math.max(1, width / 24);
        int stepY = Math.max(1, height / 24);
        int maxSamples = ((height + stepY - 1) / stepY) * ((width + stepX - 1) / stepX);
        int[] reds = new int[maxSamples];
        int[] greens = new int[maxSamples];
        int[] blues = new int[maxSamples];
        double[] weights = new double[maxSamples];
        int count = 0;

        for (int y = 0; y < height; y += stepY) {
            for (int x = 0; x < width; x += stepX) {
                int argb = image.getColorArgb(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha < 24) {
                    continue;
                }
                reds[count] = (argb >>> 16) & 0xFF;
                greens[count] = (argb >>> 8) & 0xFF;
                blues[count] = argb & 0xFF;
                weights[count] = sampleWeight(reds[count], greens[count], blues[count]);
                count++;
            }
        }

        if (count <= 0) {
            return 0xFF7ED8FF;
        }

        int red = weightedTruncatedMean(reds, weights, count);
        int green = weightedTruncatedMean(greens, weights, count);
        int blue = weightedTruncatedMean(blues, weights, count);

        red = (red * 5 + vibrantChannelBoost(red)) / 6;
        green = (green * 5 + vibrantChannelBoost(green)) / 6;
        blue = (blue * 5 + vibrantChannelBoost(blue)) / 6;

        return 0xFF000000 | ((clampChannel(red) & 0xFF) << 16) | ((clampChannel(green) & 0xFF) << 8) | (clampChannel(blue) & 0xFF);
    }

    private static int weightedTruncatedMean(int[] values, double[] weights, int count) {
        Integer[] indices = new Integer[count];
        for (int index = 0; index < count; index++) {
            indices[index] = index;
        }
        Arrays.sort(indices, (left, right) -> Integer.compare(values[left], values[right]));

        int trim = Math.min((count - 1) / 2, Math.max(0, (int) Math.floor(count * 0.12D)));
        int from = trim;
        int to = count - trim;
        double weightedSum = 0.0D;
        double totalWeight = 0.0D;
        for (int index = from; index < to; index++) {
            int sampleIndex = indices[index];
            weightedSum += values[sampleIndex] * weights[sampleIndex];
            totalWeight += weights[sampleIndex];
        }
        if (totalWeight <= 0.0D) {
            return values[indices[Math.max(0, Math.min(count - 1, count / 2))]];
        }
        return (int) Math.round(weightedSum / totalWeight);
    }

    private static double sampleWeight(int red, int green, int blue) {
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));
        double saturation = max <= 0 ? 0.0D : (max - min) / (double) max;
        double brightness = max / 255.0D;
        return 0.35D + saturation * 1.65D + brightness * 0.55D;
    }

    private static int vibrantChannelBoost(int value) {
        return Math.max(64, Math.min(255, (int) Math.round(value * 1.12D + 18.0D)));
    }

    private static int clampChannel(int value) {
        return Math.max(30, Math.min(255, value));
    }

    private static void debugLog(String template, Object... values) {
        // Temporary debug mode removed.
    }

    private static String preview(String value) {
        if (value == null) {
            return "";
        }
        String clean = sanitize(value).replace(DELIMITER, '|');
        return clean.length() <= 180 ? clean : clean.substring(0, 177) + "...";
    }

    private static String part(String[] values, int index) {
        if (values == null || index < 0 || index >= values.length) {
            return "";
        }
        return preview(values[index]);
    }
}
