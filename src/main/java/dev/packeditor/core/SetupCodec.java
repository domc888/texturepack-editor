package dev.packeditor.core;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/** Share codes: "PE1-" + base64url(deflate("target=look" lines)). */
public final class SetupCodec {
    public static final String PREFIX = "PE1-";
    private static final int MAX_CODE_CHARS = 16_384;
    private static final int MAX_DECODED_BYTES = 32_768;

    private SetupCodec() {}

    /** "pack:" looks refer to files on your own PC, so they are never put in a code. */
    public static String encode(Setup setup) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, LookSpec> e : setup.entries().entrySet()) {
            if (e.getValue().kind == LookSpec.Kind.PACK) continue;
            sb.append(e.getKey()).append('=').append(e.getValue().spec()).append('\n');
        }
        byte[] raw = sb.toString().getBytes(StandardCharsets.UTF_8);
        Deflater d = new Deflater(Deflater.BEST_COMPRESSION, true);
        d.setInput(raw);
        d.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (!d.finished()) bos.write(buf, 0, d.deflate(buf));
        d.end();
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bos.toByteArray());
    }

    /** @throws IllegalArgumentException with a short readable message on any bad input. */
    public static Setup decode(String code) {
        if (code == null) throw new IllegalArgumentException("nothing to paste");
        String c = code.replaceAll("\\s+", "");
        if (!c.startsWith(PREFIX)) throw new IllegalArgumentException("not a Pack Editor code");
        if (c.length() > MAX_CODE_CHARS) throw new IllegalArgumentException("code is too long");
        byte[] comp;
        try {
            comp = Base64.getUrlDecoder().decode(c.substring(PREFIX.length()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("code is damaged");
        }
        Inflater inf = new Inflater(true);
        inf.setInput(comp);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            while (!inf.finished()) {
                int n = inf.inflate(buf);
                if (n == 0 && (inf.needsInput() || inf.needsDictionary())) break;
                bos.write(buf, 0, n);
                if (bos.size() > MAX_DECODED_BYTES) throw new IllegalArgumentException("code is too large");
            }
            if (!inf.finished()) throw new IllegalArgumentException("code is damaged (cut off?)");
        } catch (DataFormatException e) {
            throw new IllegalArgumentException("code is damaged");
        } finally {
            inf.end();
        }
        Setup out = new Setup();
        for (String line : bos.toString(StandardCharsets.UTF_8).split("\n")) {
            if (line.isBlank()) continue;
            int eq = line.indexOf('=');
            if (eq <= 0) throw new IllegalArgumentException("code is damaged");
            String id = line.substring(0, eq);
            if (Targets.byId(id) == null) continue; // from a newer version: ignore
            LookSpec look = LookSpec.parse(line.substring(eq + 1));
            if (look.kind == LookSpec.Kind.PACK) continue;
            out.set(id, look);
        }
        return out;
    }
}
