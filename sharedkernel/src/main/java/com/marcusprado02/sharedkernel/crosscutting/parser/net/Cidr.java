package com.marcusprado02.sharedkernel.crosscutting.parser.net;

import java.net.InetAddress;

// net/CidrParser.java   // "192.168.0.0/24"
public record Cidr(InetAddress network, int prefix) {}
