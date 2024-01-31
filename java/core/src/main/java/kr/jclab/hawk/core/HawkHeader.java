package kr.jclab.hawk.core;

import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ToString
public class HawkHeader {
    private final String id;
    private final Long ts;
    private final String nonce;
    private final byte[] hash;
    private final String ext;
    private final byte[] mac;
    private final Map<String, String> raw;

    public HawkHeader(String id, Long ts, String nonce, byte[] hash, String ext, byte[] mac, Map<String, String> others) {
        this.id = id;
        this.ts = ts;
        this.nonce = nonce;
        this.hash = hash;
        this.ext = ext;
        this.mac = mac;

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (others != null) {
            map.putAll(others);
        }
        if (id != null) {
            map.put("id", id);
        }
        if (ts != null && ts >= 0) {
            map.put("ts", String.valueOf(ts));
        }
        if (nonce != null) {
            map.put("nonce", nonce);
        }
        if (hash != null) {
            map.put("hash", Base64.getEncoder().encodeToString(hash));
        }
        if (ext != null) {
            map.put("ext", ext);
        }
        map.put("mac", Base64.getEncoder().encodeToString(mac));

        this.raw = Collections.unmodifiableMap(map);
    }

    public HawkHeader(Map<String, String> raw, boolean serverAuthorization) {
        this.raw = Collections.unmodifiableMap(raw);
        if (serverAuthorization) {
            this.id = null;
            this.ts = -1L;
            this.nonce = null;
        } else {
            this.id = ensureNotEmpty("id", raw.get("id"));
            this.ts = Long.parseLong(ensureNotEmpty("ts", raw.get("ts")));
            this.nonce = ensureNotEmpty("nonce", raw.get("nonce"));
        }
        this.hash = base64DecodeOrNull(raw.get("hash"));
        this.mac = base64DecodeOrNull(ensureNotEmpty("mac", raw.get("mac")));
        this.ext = raw.get("ext");
    }

    public String serialize() {
        StringBuffer stringBuffer = new StringBuffer();
        raw.forEach((key, value) -> {
            if (stringBuffer.length() > 0) {
                stringBuffer.append(", ");
            }
            stringBuffer.append(key);
            stringBuffer.append("=");
            stringBuffer.append("\"");
            stringBuffer.append(value);
            stringBuffer.append("\"");
        });
        return "Hawk " + stringBuffer.toString();
    }

    static final Pattern PATTERN_KEY_VALUE = Pattern.compile("(\\w+)=(?:\"([^\"]+)\"|([^,\"]+))\\s*(?:,|$)");
    public static HawkHeader parse(String headerValue, boolean serverAuthorization) throws HawkParseException {
        Map.Entry<String, String> first = splitBy(headerValue, ' ');
        if (!"hawk".equals(first.getKey().toLowerCase())) {
            throw new HawkParseException("no hawk authentication: input=" + first.getKey());
        }

        HashMap<String, String> map = new HashMap<>();
        Matcher matcher = PATTERN_KEY_VALUE.matcher(first.getValue());
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            map.put(key, value);
        }

        return new HawkHeader(map, serverAuthorization);
    }

    static Map.Entry<String, String> splitBy(String input, char c) {
        int pos = input.indexOf(c);
        if (pos < 0) {
            return new AbstractMap.SimpleEntry<>(input.trim(), null);
        }
        String a = input.substring(0, pos);
        String b = input.substring(pos + 1);
        return new AbstractMap.SimpleEntry<>(a.trim(), b.trim());
    }

    static String ensureNotEmpty(String name, String s) throws HawkParseException {
        if (s == null || s.isEmpty()) {
            throw new HawkParseException(name + " must be not empty");
        }
        return s;
    }

    static byte[] base64DecodeOrNull(String s) {
        if (s == null) {
            return null;
        }
        return Base64.getDecoder().decode(s);
    }
}
