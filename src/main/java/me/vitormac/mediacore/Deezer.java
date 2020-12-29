package me.vitormac.mediacore;

import com.google.gson.JsonObject;
import me.vitormac.mediacore.data.Range;
import org.apache.commons.io.IOUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

final class Deezer extends MediaProvider {

    private static final int CHUNK_SIZE = 2048;

    @Override
    protected InputStream getInputStream(MediaInfo info, URLConnection connection, Range range)
            throws IOException {
        int start = range.getStart() - (range.getStart() % CHUNK_SIZE);

        if (info instanceof DeezerInfo) {
            connection.setRequestProperty("Range",
                    new Range(start, range.getEnd()).toString());

            DecryptStream stream = new DecryptStream(
                    connection.getInputStream(), ((DeezerInfo) info).key
            );

            stream.c = (int) Math.floor(range.getStart() / (double) CHUNK_SIZE);
            return stream;
        }

        throw new IllegalArgumentException("Invalid MediaInfo type");
    }

    @Override
    public DeezerInfo transform(JsonObject object) {
        DeezerInfo info = new DeezerInfo();
        info.setUri(object.get("uri").getAsString());
        info.setKey(object.get("key").getAsString());
        return info;
    }

    private static class DeezerInfo extends MediaInfo {

        private SecretKeySpec key;

        private void setKey(String key) {
            byte[] bytes = Base64.getDecoder().decode(key);
            this.key = new SecretKeySpec(bytes, "Blowfish");
        }

    }

    private static class DecryptStream extends InputStream {

        private int c = 0;
        private final byte[] data = new byte[8192];

        private final InputStream stream;

        private final SecretKeySpec key;
        private final IvParameterSpec spec = new IvParameterSpec(new byte[]{
                0, 1, 2, 3, 4, 5, 6, 7
        });

        DecryptStream(InputStream stream, SecretKeySpec key) {
            this.stream = IOUtils.buffer(stream);
            this.key = key;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int i = 0;

            do {
                int read = this.stream.read(data, i, len - i);

                if (read < 0) {
                    if (i == 0) {
                        return -1;
                    }

                    break;
                }

                i += read;
            } while (i < len);

            for (int pos = 0; pos < len; pos += CHUNK_SIZE, c++) {
                byte[] chunk = Arrays.copyOfRange(data, pos, pos + CHUNK_SIZE);

                if (c % 3 > 0 || i < CHUNK_SIZE) {
                    System.arraycopy(chunk, 0, b, pos, chunk.length);
                    continue;
                }

                try {
                    Cipher cipher = Cipher.getInstance("Blowfish/CBC/NoPadding");
                    cipher.init(Cipher.DECRYPT_MODE, this.key, this.spec);

                    byte[] buffer = cipher.doFinal(chunk);
                    System.arraycopy(buffer, 0, b, pos, buffer.length);
                } catch (NoSuchAlgorithmException
                        | NoSuchPaddingException
                        | InvalidAlgorithmParameterException
                        | InvalidKeyException
                        | BadPaddingException
                        | IllegalBlockSizeException e) {
                    return -1;
                }
            }

            return i;
        }

        @Override
        public int read() {
            return -1;
        }

        @Override
        public void close() throws IOException {
            this.stream.close();
            super.close();
        }

    }

}
