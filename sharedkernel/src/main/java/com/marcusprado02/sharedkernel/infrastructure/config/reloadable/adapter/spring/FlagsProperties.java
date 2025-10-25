package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring;


import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sharedkernel.flags")
public class FlagsProperties {

  private Source source = new Source();
  private Watch watch = new Watch();
  private K8s k8s = new K8s();
  private Consul consul = new Consul();

  public Source getSource() { return source; }
  public void setSource(Source source) { this.source = source; }
  public Watch getWatch() { return watch; }
  public void setWatch(Watch watch) { this.watch = watch; }
  public K8s getK8s() { return k8s; }
  public void setK8s(K8s k8s) { this.k8s = k8s; }
  public Consul getConsul() { return consul; }
  public void setConsul(Consul consul) { this.consul = consul; }

  public static class Source {
    public enum Type { FILE, HTTP, K8S, CONSUL }
    private Type type = Type.FILE;
    private String path = "application-flags.yaml"; // para FILE
    private String url;  // para HTTP
    private String auth; // bearer/basic opcional

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getAuth() { return auth; }
    public void setAuth(String auth) { this.auth = auth; }
  }

  public static class Watch {
    private boolean enabled = true;
    private Duration interval = Duration.ofSeconds(2); // para HTTP polling

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Duration getInterval() { return interval; }
    public void setInterval(Duration interval) { this.interval = interval; }
  }

  public static class K8s {
    private String namespace = "default";
    private String configMap;
    private String key = "application-flags.yaml";

    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
    public String getConfigMap() { return configMap; }
    public void setConfigMap(String configMap) { this.configMap = configMap; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
  }

  public static class Consul {
    private String host = "http://127.0.0.1:8500";
    private String key = "configs/application-flags.yaml";
    private String token;

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
  }
}
