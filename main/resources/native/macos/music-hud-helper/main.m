#import <AppKit/AppKit.h>
#import <Foundation/Foundation.h>
#import <iTunesLibrary/iTunesLibrary.h>

static NSString *const Delimiter = @"\x1F";
static NSString *const FallbackBundleId = @"dev.david.huzzclient.musichelper";

static NSString *Sanitize(NSString *value) {
    if (value == nil) {
        return @"";
    }

    NSString *cleaned = [[value stringByReplacingOccurrencesOfString:@"\r" withString:@" "]
        stringByReplacingOccurrencesOfString:@"\n" withString:@" "];
    cleaned = [cleaned stringByReplacingOccurrencesOfString:Delimiter withString:@" "];
    return [cleaned stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
}

static long long ParseLong(NSString *value) {
    return Sanitize(value).longLongValue;
}

static long long ParseDurationToMs(NSString *value) {
    double rawValue = Sanitize(value).doubleValue;
    if (rawValue <= 0.0) {
        return 0;
    }
    return rawValue > 1000.0 ? llround(rawValue) : llround(rawValue * 1000.0);
}

static NSString *HexString(NSNumber *number) {
    return [NSString stringWithFormat:@"%016llX", number.unsignedLongLongValue];
}

static NSString *NormalizePersistentId(NSString *value) {
    NSString *upper = Sanitize(value).uppercaseString;
    NSMutableString *hex = [NSMutableString string];
    for (NSUInteger index = 0; index < upper.length; index++) {
        unichar character = [upper characterAtIndex:index];
        if ((character >= '0' && character <= '9') || (character >= 'A' && character <= 'F')) {
            [hex appendFormat:@"%C", character];
        }
    }

    if (hex.length == 0) {
        return @"";
    }
    if (hex.length >= 16) {
        return [hex substringFromIndex:hex.length - 16];
    }

    NSMutableString *padded = [NSMutableString stringWithCapacity:16];
    for (NSUInteger index = hex.length; index < 16; index++) {
        [padded appendString:@"0"];
    }
    [padded appendString:hex];
    return padded;
}

static NSURL *SupportDirectory(void) {
    NSURL *base = [[[NSFileManager defaultManager] URLsForDirectory:NSApplicationSupportDirectory inDomains:NSUserDomainMask] firstObject];
    if (base == nil) {
        base = [NSURL fileURLWithPath:NSTemporaryDirectory() isDirectory:YES];
    }
    NSString *bundleId = [[NSBundle mainBundle] bundleIdentifier] ?: FallbackBundleId;
    return [base URLByAppendingPathComponent:bundleId isDirectory:YES];
}

static NSData *PngDataForImage(NSImage *image) {
    NSData *tiffData = image.TIFFRepresentation;
    if (tiffData == nil) {
        return nil;
    }

    NSBitmapImageRep *bitmap = [NSBitmapImageRep imageRepWithData:tiffData];
    if (bitmap == nil) {
        return nil;
    }
    return [bitmap representationUsingType:NSBitmapImageFileTypePNG properties:@{}];
}

static NSData *PngDataForArtwork(ITLibArtwork *artwork) {
    NSImage *image = artwork.image;
    if (image == nil && artwork.imageData != nil) {
        image = [[NSImage alloc] initWithData:artwork.imageData];
    }

    NSData *pngData = image != nil ? PngDataForImage(image) : nil;
    return pngData ?: artwork.imageData;
}

static NSString *RunAppleScriptCommand(void) {
    NSTask *task = [[NSTask alloc] init];
    task.launchPath = @"/usr/bin/osascript";
    task.arguments = @[
        @"-e", @"if application \"Music\" is not running then return \"\"",
        @"-e", @"tell application \"Music\"",
        @"-e", @"try",
        @"-e", @"set stateText to (player state as text)",
        @"-e", @"on error",
        @"-e", @"return \"\"",
        @"-e", @"end try",
        @"-e", @"if stateText is \"stopped\" then return \"\"",
        @"-e", @"set d to character id 31",
        @"-e", @"try",
        @"-e", @"set t to current track",
        @"-e", @"return (name of t as text) & d & (artist of t as text) & d & (album of t as text) & d & (duration of t as text) & d & (player position as text) & d & (persistent ID of t as text)",
        @"-e", @"on error",
        @"-e", @"try",
        @"-e", @"set streamTitle to current stream title as text",
        @"-e", @"if streamTitle is \"\" then return \"\"",
        @"-e", @"return streamTitle & d & \"\" & d & \"\" & d & \"0\" & d & \"0\" & d & \"\"",
        @"-e", @"on error",
        @"-e", @"return \"\"",
        @"-e", @"end try",
        @"-e", @"end try",
        @"-e", @"end tell"
    ];

    NSPipe *stdoutPipe = [NSPipe pipe];
    NSPipe *stderrPipe = [NSPipe pipe];
    task.standardOutput = stdoutPipe;
    task.standardError = stderrPipe;

    @try {
        [task launch];
    } @catch (__unused NSException *exception) {
        return @"";
    }

    [task waitUntilExit];
    NSData *stdoutData = [stdoutPipe.fileHandleForReading readDataToEndOfFile];
    if (task.terminationStatus != 0 || stdoutData.length == 0) {
        return @"";
    }

    NSString *output = [[NSString alloc] initWithData:stdoutData encoding:NSUTF8StringEncoding];
    return [output stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
}

static NSDictionary<NSString *, id> *FetchAppleMusicMetadata(void) {
    NSString *output = RunAppleScriptCommand();
    if (output.length == 0) {
        return nil;
    }

    NSArray<NSString *> *parts = [output componentsSeparatedByString:Delimiter];
    if (parts.count < 6) {
        return nil;
    }

    NSString *rawTrackId = Sanitize(parts[5]);
    return @{
        @"trackId": [rawTrackId isEqualToString:@"STREAM"] ? @"" : rawTrackId,
        @"title": Sanitize(parts[0]),
        @"artist": Sanitize(parts[1]),
        @"album": Sanitize(parts[2]),
        @"durationMs": @(ParseDurationToMs(parts[3])),
        @"positionMs": @(ParseDurationToMs(parts[4]))
    };
}

static ITLibMediaItem *LibraryItemForTrackId(NSString *trackId) {
    NSString *normalizedId = NormalizePersistentId(trackId);
    if (normalizedId.length == 0) {
        return nil;
    }

    NSError *error = nil;
    ITLibrary *library = [ITLibrary libraryWithAPIVersion:@"1.0" error:&error];
    if (library == nil || error != nil) {
        return nil;
    }

    for (ITLibMediaItem *item in library.allMediaItems) {
        if ([NormalizePersistentId(HexString(item.persistentID)) isEqualToString:normalizedId]) {
            return item;
        }
    }
    return nil;
}

static BOOL ShouldRewrite(NSURL *url, NSUInteger expectedSize) {
    NSDictionary<NSFileAttributeKey, id> *attributes = [[NSFileManager defaultManager] attributesOfItemAtPath:url.path error:nil];
    NSNumber *size = attributes[NSFileSize];
    return size == nil || size.unsignedIntegerValue != expectedSize;
}

static NSString *ExportArtwork(NSString *trackId) {
    ITLibMediaItem *item = LibraryItemForTrackId(trackId);
    if (item == nil || item.artwork == nil) {
        return @"";
    }

    NSData *artworkData = PngDataForArtwork(item.artwork);
    if (artworkData.length == 0) {
        return @"";
    }

    NSURL *coverDirectory = [SupportDirectory() URLByAppendingPathComponent:@"covers" isDirectory:YES];
    NSError *directoryError = nil;
    if (![[NSFileManager defaultManager] createDirectoryAtURL:coverDirectory withIntermediateDirectories:YES attributes:nil error:&directoryError]) {
        return @"";
    }

    NSURL *outputUrl = [coverDirectory URLByAppendingPathComponent:[NormalizePersistentId(trackId) stringByAppendingString:@".png"]];
    if (ShouldRewrite(outputUrl, artworkData.length)) {
        if (![artworkData writeToURL:outputUrl options:NSDataWritingAtomic error:nil]) {
            return @"";
        }
    }
    return outputUrl.path ?: @"";
}

int main(int argc, const char *argv[]) {
    @autoreleasepool {
        if (argc < 3 || strcmp(argv[1], "--source") != 0 || strcmp(argv[2], "apple-music") != 0) {
            return 0;
        }

        NSDictionary<NSString *, id> *metadata = FetchAppleMusicMetadata();
        if (metadata == nil) {
            return 0;
        }

        NSString *title = metadata[@"title"] ?: @"";
        if (title.length == 0) {
            return 0;
        }

        NSString *trackId = metadata[@"trackId"] ?: @"";
        NSString *coverPath = trackId.length > 0 ? ExportArtwork(trackId) : @"";
        NSArray<NSString *> *fields = @[
            title,
            metadata[@"artist"] ?: @"",
            metadata[@"album"] ?: @"",
            [metadata[@"durationMs"] stringValue] ?: @"0",
            [metadata[@"positionMs"] stringValue] ?: @"0",
            coverPath ?: @"",
            trackId
        ];

        NSMutableArray<NSString *> *sanitizedFields = [NSMutableArray arrayWithCapacity:fields.count];
        for (NSString *field in fields) {
            [sanitizedFields addObject:Sanitize(field)];
        }

        NSString *output = [sanitizedFields componentsJoinedByString:Delimiter];
        fprintf(stdout, "%s\n", output.UTF8String);
    }
    return 0;
}
