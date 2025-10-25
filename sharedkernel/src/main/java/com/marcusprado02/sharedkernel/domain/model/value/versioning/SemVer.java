package com.marcusprado02.sharedkernel.domain.model.value.versioning;



import java.util.Objects;
import java.util.regex.Pattern;

import com.marcusprado02.sharedkernel.domain.model.value.AbstractValueObject;

public final class SemVer extends AbstractValueObject implements Comparable<SemVer> {
    private static final Pattern RE = Pattern.compile(
        "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)" +
        "(?:-([0-9A-Za-z-\\.]+))?(?:\\+([0-9A-Za-z-\\.]+))?$"
    );

    private final int major, minor, patch;
    private final String preRelease;  // opcional
    private final String buildMeta;   // opcional

    private SemVer(int major, int minor, int patch, String preRelease, String buildMeta) {
        this.major = major; this.minor = minor; this.patch = patch;
        this.preRelease = preRelease; this.buildMeta = buildMeta;
    }

    public static SemVer parse(String s) {
        var m = RE.matcher(Objects.requireNonNull(s, "null semver"));
        if (!m.matches()) throw new IllegalArgumentException("invalid semver: " + s);
        return new SemVer(
            Integer.parseInt(m.group(1)),
            Integer.parseInt(m.group(2)),
            Integer.parseInt(m.group(3)),
            m.group(4), m.group(5)
        );
    }

    public String value() {
        return "%d.%d.%d%s%s".formatted(
            major, minor, patch,
            preRelease != null ? "-" + preRelease : "",
            buildMeta  != null ? "+" + buildMeta  : ""
        );
    }

    @Override protected Object[] equalityComponents() {
        return new Object[]{ major, minor, patch, preRelease, buildMeta };
    }

    @Override public int compareTo(SemVer o) {
        if (major != o.major) return Integer.compare(major, o.major);
        if (minor != o.minor) return Integer.compare(minor, o.minor);
        if (patch != o.patch) return Integer.compare(patch, o.patch);
        // Pre-release precedence: absence > presence; otherwise lexicográfico por identificadores
        if (preRelease == null && o.preRelease != null) return 1;
        if (preRelease != null && o.preRelease == null) return -1;
        if (preRelease == null) return 0;
        return preRelease.compareTo(o.preRelease);
    }
}
