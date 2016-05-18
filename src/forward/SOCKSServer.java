package forward;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import com.jcraft.jsch.ChannelDirectTCPIP;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SOCKSServer extends Thread {
	
	private boolean verbose = true;

	private Socket sock;
	private Session sshSession;

	public SOCKSServer(Socket sock, Session sshSession) {
		this.sock = sock;
		this.sshSession = sshSession;
	}
	
	public void log(String output) {
		if(verbose) {
			System.out.println(output);
		}
	}

	public void run() {
		try{
			InputStream in = sock.getInputStream();				
			OutputStream out = sock.getOutputStream();

			int version = in.read();
			//SOCKS5
			if(version == 5) {	
				//read client greeting
				int authNumber = in.read();
				log("authNumber: " + authNumber);
				for (int i = 0; i < authNumber; i++)
					log("Authmethod[" + i + "]:  " + in.read());
				//respond choice
				out.write(5); //SOCKS5
				out.write(0); //no authentication
				out.flush();
				//read client connection request
				version = in.read();
				int cd = in.read();
				int rsv = in.read();
				int atype = in.read();
				log("version: " + version);
				log("cd: " + cd);
				log("rsv: " + rsv);
				log("atype: " + atype);
				byte[] ip = new byte[4];
				String domainName = "";
				switch(atype) {
				case 1:
					//read IPv4
					for (int i = 0; i < ip.length; i++)
						ip[i] = (byte) in.read();
					log(Arrays.toString(ip));
					break;
				case 3:
					//read domain name
					int length = in.read();
					for (int i = 0; i < length; i++)
						domainName += (char) in.read();
					log(domainName);
					break;
				}
				int[] portBytes = new int[2]; // port is short in network order
				for (int i = 0; i < portBytes.length; i++)
					portBytes[i] = in.read();
				log("other port order: " +  (portBytes[1] << 8 | portBytes[0]));
				int port = portBytes[0] << 8 | portBytes[1];
				log("Port: " + port);
				//respond to request
				out.write(5); //SOCKS5
				out.write(0); //request granted
				out.write(0); //reserved
				out.write(1); //addresstype = IPv4
				byte[] serverIP = {0, 0, 0, 0}; //server ip, doesnt matter here
				out.write(serverIP);
				byte[] serverPort = {0, 0}; //server port, doesnt matter here
				out.write(serverPort);
				out.flush();
				//forward
				ChannelDirectTCPIP channel = (ChannelDirectTCPIP) sshSession.openChannel("direct-tcpip");
				channel.setInputStream(in);
				channel.setOutputStream(out);
				switch(atype) {
				case 1:
					channel.setHost(InetAddress.getByAddress(ip).getHostAddress());
					break;
				case 3:
					channel.setHost(InetAddress.getByName(domainName).getHostAddress());
					break;
				}
				channel.setPort(port);
				channel.connect();
			}
		}
		catch(IOException | JSchException e) {
			System.out.println(e);
		}
	}
}
