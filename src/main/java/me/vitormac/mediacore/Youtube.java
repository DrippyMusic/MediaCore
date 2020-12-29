package me.vitormac.mediacore;

import com.google.gson.JsonObject;
import me.vitormac.mediacore.data.Range;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.Charset;

final class Youtube extends MediaProvider {

    @Override
    protected InputStream getInputStream(MediaInfo info, URLConnection connection, Range range)
            throws IOException {
        return connection.getInputStream();
    }

    @Override
    public MediaInfo transform(JsonObject object) {
        MediaInfo info = new MediaInfo();
        String path = Youtube.getRuntimePath();

        try {
            String link = object.get("uri").getAsString();
            Process process = new ProcessBuilder(path + "youtube-dl",
                    "--geo-bypass", "--youtube-skip-dash-manifest", "-f", "140", "-s", "-g", link
            ).start();

            String uri = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());

            info.setUri(uri);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return info;
    }

    private static String getRuntimePath() {
        String path = System.getProperty("mediacore.execution.path");
        if (path != null)
            return path;

        return "";
    }

}
