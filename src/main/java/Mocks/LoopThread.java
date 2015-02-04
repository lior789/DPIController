package Mocks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

public class LoopThread extends Thread {

	private String _bpf = "";

	public LoopThread(String _bpf) {
		super();
		this._bpf = _bpf;
	}

	private final class LoopHandler implements PcapPacketHandler<Pcap> {
		@Override
		public void nextPacket(PcapPacket packet, Pcap device) {
			System.out.printf("Received packet at %s - %s \n", new Date(packet
					.getCaptureHeader().timestampInMillis()),
					generatePacketString(packet));

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (device.sendPacket(packet) != Pcap.OK) {
				System.err.println(device.getErr());
			}
		}

		private String generatePacketString(PcapPacket packet) {
			return packet.toString(); // TODO: shoter string
										// (from,to,protocol,vlan)
		}
	}

	@Override
	public void run() {
		List<PcapIf> alldevs = new ArrayList<PcapIf>(); // Will be filled with
														// NICs
		StringBuilder errbuf = new StringBuilder(); // For any error msgs

		int r = Pcap.findAllDevs(alldevs, errbuf);
		if (r == Pcap.ERROR || alldevs.isEmpty()) {
			System.err.printf("Can't read list of devices, error is %s",
					errbuf.toString());
			return;
		}
		// System.out.println(alldevs);
		PcapIf device = alldevs.get(2); // TODO: make this more robust

		int snaplen = 64 * 1024; // Capture all packets, no trucation
		int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
		int timeout = -1; // 10 seconds in millis
		Pcap pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout,
				errbuf);

		if (pcap == null) {
			System.err.printf("Error while opening device for capture: "
					+ errbuf.toString());
			return;
		}

		setFilter(pcap, _bpf);

		PcapPacketHandler<Pcap> jpacketHandler = new LoopHandler();

		pcap.loop(Pcap.LOOP_INTERRUPTED, jpacketHandler, pcap);

		pcap.close();
	}

	private void setFilter(Pcap pcap, String expression) {
		PcapBpfProgram program = new PcapBpfProgram();

		int optimize = 0;
		int netmask = 0xFFFFFFFF;

		if (pcap.compile(program, expression, optimize, netmask) != Pcap.OK) {
			System.err.println(pcap.getErr());
			return;
		}

		if (pcap.setFilter(program) != Pcap.OK) {
			System.err.println(pcap.getErr());
			return;
		}
		pcap.setFilter(program);
	}

}
