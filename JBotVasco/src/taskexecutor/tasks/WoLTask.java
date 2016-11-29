package taskexecutor.tasks;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import result.Result;
import taskexecutor.tasks.WoLTask.InterfaceInformation.Information;
import tasks.Task;

public class WoLTask extends Task {
	private static final long serialVersionUID = -1060260531177261585L;
	public static final int PORT = 9;
	private ArrayList<String> macAddresses = null;

	public WoLTask(ArrayList<String> macAddresses) {
		this.macAddresses = macAddresses;
	}

	@Override
	public void run() {
		List<Information> interfaceInformations = new InterfaceInformation().getInterfacesInformations();
		Map<InetAddress, InetAddress> addresses = new HashMap<InetAddress, InetAddress>();

		// Get all the possible network broadcast addresses
		for (Information information : interfaceInformations) {
			HashMap<InetAddress, InetAddress> addr = information.getAddresses();
			for (InetAddress inetAddr : addr.keySet()) {
				addresses.put(inetAddr, addr.get(inetAddr));
			}
		}

		// If there are any hosts to sent to, do it so
		if (macAddresses != null && !macAddresses.isEmpty()) {
			// Random random = new Random();
			for (String str : macAddresses) {
				String hwAddress = str.replaceAll(":", "-");

				for (InetAddress addr : addresses.keySet()) {
					sendWoLPacket(addresses.get(addr).toString().replace("/", ""), hwAddress);

					// try {
					// Thread.sleep(250);
					// } catch (InterruptedException e) {
					// }
					// calculatePi(150000000L + random.nextInt(10000));
				}
			}
		}
	}

	private void sendWoLPacket(String ipStr, String macStr) {
		try {
			byte[] macBytes = getMacBytes(macStr);
			byte[] bytes = new byte[6 + 16 * macBytes.length];
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) 0xff;
			}
			for (int i = 6; i < bytes.length; i += macBytes.length) {
				System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
			}

			InetAddress address = InetAddress.getByName(ipStr);
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
			socket.close();

			System.out.printf("[%s] Wake-on-LAN packet sent%n", WoLTask.class.getSimpleName());
		} catch (Exception e) {
			System.out.printf("[%s] Failed to send Wake-on-LAN packet: %s%n", WoLTask.class.getSimpleName(),
					e.getMessage());
		}
	}

	private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
		byte[] bytes = new byte[6];
		String[] hex = macStr.split("(\\:|\\-)");
		if (hex.length != 6) {
			throw new IllegalArgumentException("Invalid MAC address.");
		}
		try {
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) Integer.parseInt(hex[i], 16);
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid hex digit in MAC address.");
		}
		return bytes;
	}

	/*
	 * From here http://www.cs.utsa.edu/~wagner/pi/PiSlow.html It is a slow
	 * method on purpose
	 */
	public double calculatePi(long iterations) {
		double sum = 0.0; // Final sum
		double term; // Term without sign
		double sign = 1.0; // Sign on each term
		for (int k = 0; k < iterations; k++) {
			term = 1.0 / (2.0 * k + 1.0);
			sum = sum + sign * term;
			sign = -sign;
		}

		return sum * 4.0;
	}

	@Override
	public Result getResult() {
		return new Result(getId());
	}

	public int getTotalRuns() {
		return (macAddresses != null) ? macAddresses.size() : 0;
	}

	protected class InterfaceInformation implements Serializable {
		private static final long serialVersionUID = -1435160037901105150L;

		public List<Information> getInterfacesInformations() {
			ArrayList<Information> informations = new ArrayList<Information>();
			Enumeration<NetworkInterface> en;

			try {
				en = NetworkInterface.getNetworkInterfaces();
				while (en.hasMoreElements()) {
					NetworkInterface ni = en.nextElement();

					if (ni.getHardwareAddress() != null && ni.getHardwareAddress().length == 6) {
						byte[] hwAddress = ni.getHardwareAddress();
						String displayName = ni.getDisplayName();

						HashMap<InetAddress, InetAddress> addresses = new HashMap<InetAddress, InetAddress>();
						Iterator<InterfaceAddress> it = ni.getInterfaceAddresses().iterator();
						while (it.hasNext()) {
							InterfaceAddress ia = it.next();
							addresses.put(ia.getAddress(), ia.getBroadcast());
						}

						informations.add(new Information(addresses, hwAddress, displayName));
					}
				}
			} catch (SocketException e) {
				System.err.printf(
						"[%s] Error gattering network interfaces! %s%n" + InterfaceInformation.class.getSimpleName(),
						e.getMessage());
			}

			return informations;
		}

		public class Information implements Serializable {
			private static final long serialVersionUID = 1960127210757446234L;
			private HashMap<InetAddress, InetAddress> addresses = null;
			private byte[] hwAddress = null;
			private String displayName = null;

			public Information(HashMap<InetAddress, InetAddress> addresses, byte[] hwAddress, String displayName) {
				this.addresses = addresses;
				this.hwAddress = hwAddress;
				this.displayName = displayName;
			}

			public HashMap<InetAddress, InetAddress> getAddresses() {
				return addresses;
			}

			public byte[] getHwAddress() {
				return hwAddress;
			}

			public String getDisplayName() {
				return displayName;
			}
		}
	}
}
