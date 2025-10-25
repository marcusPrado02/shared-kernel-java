package com.marcusprado02.sharedkernel.observability.profiling.adapter.async;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class AsyncProfilerAdapter implements ProfilerAdapter {
    private final Path apPath;  // caminho do binário 'profiler.sh'
    private final Path outDir;
    private Process proc;

    public AsyncProfilerAdapter(Path apPath, Path outDir) {
        this.apPath = apPath; this.outDir = outDir;
    }

    @Override public void start(ProfilingContext ctx, EvaluationResult why) {
        try {
            var pid = Long.toString(ProcessHandle.current().pid());
            var cmd = List.of(apPath.toString(), "start", "--event", "cpu", "--pid", pid);
            proc = new ProcessBuilder(cmd).start();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override public void stop(ProfilingContext ctx, EvaluationResult why) {
        try {
            var pid = Long.toString(ProcessHandle.current().pid());
            String out = outDir.resolve("ap_" + System.currentTimeMillis() + ".svg").toString();
            var cmd = List.of(apPath.toString(), "stop", "--pid", pid, "--svg", "--output", out);
            new ProcessBuilder(cmd).inheritIO().start().waitFor();
            if (proc != null) proc.destroy();
        } catch (Exception e) { e.printStackTrace(); }
    }
}