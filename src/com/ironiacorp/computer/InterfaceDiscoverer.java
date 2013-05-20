/*
Copyright (C) 2011 Marco Aurélio Graciotto Silva <magsilva@ironiacorp.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.ironiacorp.computer;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class InterfaceDiscoverer
{
	private boolean ipv4 = true;

	private boolean ipv6 = false;

	private boolean localhost = false;

	public boolean isIpv4()
	{
		return ipv4;
	}

	public void setIpv4(boolean ipv4)
	{
		this.ipv4 = ipv4;
	}

	public boolean isIpv6()
	{
		return ipv6;
	}

	public void setIpv6(boolean ipv6)
	{
		this.ipv6 = ipv6;
	}

	public boolean isLocalhost()
	{
		return localhost;
	}

	public void setLocalhost(boolean localhost)
	{
		this.localhost = localhost;
	}

	public Set<InetAddress> getAddresses(NetworkInterface ni)
	{
		Enumeration<InetAddress> addresses = ni.getInetAddresses();
		Set<InetAddress> validAddresses = new HashSet<InetAddress>();

		while (addresses.hasMoreElements()) {
			InetAddress address = addresses.nextElement();
			if (address.isLoopbackAddress() && !localhost) {
				continue;
			}
			if (address instanceof Inet6Address && !ipv6) {
				continue;
			}

			if (address instanceof Inet4Address && !ipv4) {
				continue;
			}

			validAddresses.add(address);
		}

		return validAddresses;
	}

	public Map<InetAddress, NetworkInterface> discoverAddresses()
	{
		Map<InetAddress, NetworkInterface> result = new HashMap<InetAddress, NetworkInterface>();
		Enumeration<NetworkInterface> niEnu = null;
		
		try {
			niEnu = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException se) {
			throw new IllegalArgumentException("Cannot list interfaces", se);
		}
		
		while (niEnu.hasMoreElements()) {
			NetworkInterface ni = niEnu.nextElement();
			Enumeration<NetworkInterface> vniEnu = ni.getSubInterfaces(); 
			Set<InetAddress> addresses = getAddresses(ni);
			Iterator<InetAddress> i = addresses.iterator();
			while (i.hasNext()) {
				InetAddress address = i.next();
				result.put(address, ni);
			}
			while (vniEnu.hasMoreElements()) {
				NetworkInterface vni = vniEnu.nextElement();
				addresses = getAddresses(vni);
				i = addresses.iterator();
				while (i.hasNext()) {
					InetAddress address = i.next();
					result.put(address, vni);
				}
			}
		}
		
		return result;
	}
	
	public Set<NetworkInterface> discoverNics()
	{
		Set<NetworkInterface> result = new HashSet<NetworkInterface>();
		Enumeration<NetworkInterface> niEnu = null;
		
		try {
			niEnu = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException se) {
			throw new IllegalArgumentException("Cannot list interfaces", se);
		}
		
		while (niEnu.hasMoreElements()) {
			NetworkInterface ni = niEnu.nextElement();
			result.add(ni);
			Enumeration<NetworkInterface> vniEnu = ni.getSubInterfaces(); 
			while (vniEnu.hasMoreElements()) {
				NetworkInterface vni = vniEnu.nextElement();
				result.add(vni);
			}
		}
		
		return result;
	}
}
