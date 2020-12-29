package me.vitormac.mediacore;

import com.google.gson.JsonObject;
import me.vitormac.mediacore.data.Range;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

final class SoundCloud extends MediaProvider {

    @Override
    protected InputStream getInputStream(MediaInfo info, URLConnection connection, Range range)
            throws IOException {
        return connection.getInputStream();
    }

    @Override
    public MediaInfo transform(JsonObject object) {
        MediaInfo info = new MediaInfo();
        info.setUri(object.get("uri").getAsString());
        return info;
    }

}
