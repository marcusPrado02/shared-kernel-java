package com.marcusprado02.sharedkernel.observability.alerts.render;


import java.nio.charset.StandardCharsets;
import java.util.*;

public final class AlertmanagerConfigRenderer {

    public static byte[] render(
            String rootReceiver,
            Map<String, String> defaultLabels, // team->oncall, env->prod
            List<RouteRule> routes,
            List<InhibitRule> inhibits,
            List<Receiver> receivers
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("route:\n")
          .append("  receiver: ").append(rootReceiver).append("\n")
          .append("  group_by: ['alertname','service']\n")
          .append("  group_wait: 30s\n")
          .append("  group_interval: 5m\n")
          .append("  repeat_interval: 2h\n");
        // rotas
        for (var r : routes) {
            sb.append("  routes:\n")
              .append("  - receiver: ").append(r.receiver).append("\n")
              .append("    matchers:\n");
            r.matchers.forEach((k,v)-> sb.append("    - ").append(k).append("=\"").append(v).append("\"\n"));
            if (r.muteTimeInterval!=null) sb.append("    mute_time_intervals: [").append(r.muteTimeInterval).append("]\n");
        }
        // inibição
        if (!inhibits.isEmpty()) {
            sb.append("inhibit_rules:\n");
            for (var ih : inhibits) {
                sb.append("- source_matchers: ['severity=\"").append(ih.sourceSeverity).append("\"']\n")
                  .append("  target_matchers: ['severity=\"").append(ih.targetSeverity).append("\"']\n")
                  .append("  equal: [ 'service','slo' ]\n");
            }
        }
        // receivers
        sb.append("receivers:\n");
        for (var rc : receivers) {
            sb.append("- name: ").append(rc.name).append("\n");
            if (rc.slackWebhookUrl!=null)
                sb.append("  slack_configs:\n")
                  .append("  - api_url: ").append(rc.slackWebhookUrl).append("\n")
                  .append("    channel: ").append(rc.slackChannel).append("\n");
            if (rc.pagerDutyRoutingKey!=null)
                sb.append("  pagerduty_configs:\n")
                  .append("  - routing_key: ").append(rc.pagerDutyRoutingKey).append("\n");
            if (rc.emailTo!=null)
                sb.append("  email_configs:\n")
                  .append("  - to: ").append(rc.emailTo).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static final class RouteRule {
        public final String receiver; public final Map<String,String> matchers; public final String muteTimeInterval;
        public RouteRule(String receiver, Map<String,String> matchers, String muteTimeInterval){
            this.receiver=receiver; this.matchers=matchers; this.muteTimeInterval=muteTimeInterval;
        }
    }
    public static final class InhibitRule {
        public final String sourceSeverity, targetSeverity;
        public InhibitRule(String src, String tgt){ this.sourceSeverity=src; this.targetSeverity=tgt; }
    }
    public static final class Receiver {
        public final String name; public final String slackWebhookUrl; public final String slackChannel;
        public final String pagerDutyRoutingKey; public final String emailTo;
        public Receiver(String name, String slackUrl, String slackChannel, String pdKey, String emailTo){
            this.name=name; this.slackWebhookUrl=slackUrl; this.slackChannel=slackChannel;
            this.pagerDutyRoutingKey=pdKey; this.emailTo=emailTo;
        }
    }
}
