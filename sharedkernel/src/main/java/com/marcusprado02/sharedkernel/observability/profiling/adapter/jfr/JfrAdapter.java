package com.marcusprado02.sharedkernel.observability.profiling.adapter.jfr;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class JfrAdapter implements ProfilerAdapter {
    private final Path outputDir;
    private final String settings; // ex.: "profile" | "default" | path custom
    private final String recordingName;

    public JfrAdapter(Path outputDir, String settings, String recordingName) {
        this.outputDir = outputDir; this.settings = settings; this.recordingName = recordingName;
    }

    @Override public void start(ProfilingContext ctx, EvaluationResult why) {
        // JDK 14+: jcmd start recording
        runJcmd("JFR.start", "name=" + recordingName, "settings=" + settings, "dumponexit=true");
    }
    @Override public void stop(ProfilingContext ctx, EvaluationResult why) {
        String file = outputDir.resolve("jfr_" + recordingName + "_" + System.currentTimeMillis() + ".jfr").toString();
        runJcmd("JFR.stop",  "name=" + recordingName);
        runJcmd("JFR.dump",  "name=" + recordingName, "filename=" + file);
    }
    private void runJcmd(String... args){
        try {
            var pid = ProcessHandle.current().pid();
            var cmd = new ArrayList<String>();
            cmd.add("jcmd"); cmd.add(Long.toString(pid)); cmd.addAll(Arrays.asList(args));
            new ProcessBuilder(cmd).inheritIO().start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
