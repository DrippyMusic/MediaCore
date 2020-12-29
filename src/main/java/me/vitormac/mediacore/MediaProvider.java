package me.vitormac.mediacore;

import me.vitormac.mediacore.data.Range;
import me.vitormac.mediacore.data.Transformable;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public abstract class MediaProvider implements Transformable<MediaProvider.MediaInfo> {

    protected abstract InputStream getInputStream(MediaInfo info, URLConnection connection, Range range)
            throws IOException;

    public final InputStream stream(MediaInfo info) throws IOException {
        return this.stream(info, new Range());
    }

    public final InputStream stream(MediaInfo info, Range range) throws IOException {
        if (info.getUri() != null && !info.getUri().isEmpty()) {
            URLConnection connection = new URL(info.getUri()).openConnection();
            connection.setRequestProperty("Range", range.toString());

            InputStream stream = this.getInputStream(info, connection, range);
            info.setType(connection.getContentType());

            String header = connection.getHeaderField("Content-Range");
            info.setRange(Range.from(header));

            return IOUtils.buffer(stream);
        }

        throw new IOException("Stream URI is empty");
    }

    public static MediaProvider create(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Provider name can't be null");
        }

        try {
            return Provider.from(name);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid provider name");
        }
    }

    public static void init(String path) {
        System.setProperty("mediacore.execution.path", path);
    }

    public static class MediaInfo {

        private String uri;

        private Range range;

        private String type;

        public String getUri() {
            return uri;
        }

        protected void setUri(String uri) {
            this.uri = uri;
        }

        public Range getRange() {
            return range;
        }

        private void setRange(Range range) {
            this.range = range;
        }

        public String getType() {
            return type;
        }

        private void setType(String type) {
            this.type = type;
        }

    }

    private enum Provider {

        SOUNDCLOUD(new SoundCloud()),
        DEEZER(new Deezer()),
        YOUTUBE_MUSIC(new Youtube());

        private final MediaProvider provider;

        Provider(MediaProvider provider) {
            this.provider = provider;
        }

        public static MediaProvider from(String name) {
            return Provider.valueOf(name).provider;
        }

    }

}
