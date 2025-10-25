package com.marcusprado02.sharedkernel.crosscutting.helpers.net;

import java.net.InetAddress;

public final class IpCidrHelper {
  private IpCidrHelper(){}
  public static boolean contains(String cidr, String ip) {
    String[] parts = cidr.split("/");
    int prefix = Integer.parseInt(parts[1]);
    byte[] net = toBytes(parts[0]);
    byte[] addr = toBytes(ip);
    int full = prefix/8, rem = prefix%8;
    for (int i=0;i<full;i++) if (net[i]!=addr[i]) return false;
    if (rem==0) return true;
    int mask = ~((1 << (8-rem)) - 1) & 0xFF;
    return (net[full] & mask) == (addr[full] & mask);
  }
  private static byte[] toBytes(String ip) {
    try { return InetAddress.getByName(ip).getAddress(); }
    catch (Exception e) { throw new IllegalArgumentException("invalid ip", e); }
  }
}
