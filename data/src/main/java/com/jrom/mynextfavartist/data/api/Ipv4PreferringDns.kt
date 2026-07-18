package com.jrom.mynextfavartist.data.api

import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress

/**
 * Some networks (and notably the Android emulator's virtual network) advertise an IPv6
 * address for a host but can't actually route to it, producing a hard ConnectException
 * instead of falling back to IPv4. Sorting IPv4 addresses first means OkHttp tries those
 * before any unreachable IPv6 address, without giving up IPv6 entirely when it does work.
 */
object Ipv4PreferringDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val addresses = Dns.SYSTEM.lookup(hostname)
        val ipv4Addresses = addresses.filterIsInstance<Inet4Address>()
        return ipv4Addresses.ifEmpty { addresses }
    }
}
