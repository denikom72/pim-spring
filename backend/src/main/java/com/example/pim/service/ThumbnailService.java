package com.example.pim.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class ThumbnailService {

    public Path generateThumbnail(Path originalImagePath, Path thumbnailDirectory, String filename) throws IOException {
        Path thumbnailPath = thumbnailDirectory.resolve("thumb_" + filename);
        Thumbnails.of(originalImagePath.toFile())
                .size(128, 128) // TODO: Make thumbnail size configurable
                .toFile(thumbnailPath.toFile());
        return thumbnailPath;
    }
}
