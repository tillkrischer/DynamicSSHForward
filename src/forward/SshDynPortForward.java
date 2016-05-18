package forward;
import java.io.IOException;
import java.net.ServerSocket;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshDynPortForward extends Thread {

	private String user;
	private String host;
	private String password;
	private int port;
	private ServerSocket listener;


	public SshDynPortForward(String user, String host, String password, int port) {
		this.user = user;
		this.host = host;
		this.password = password;
		this.port = port;
	}

	public void run() {
		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);
			session.connect();
			System.out.println("Server fingerprint: " + session.getHostKey().getFingerPrint(jsch));
			listener = new ServerSocket(port);
			while(true) {
				Thread thread = new SOCKSServer(listener.accept(), session);
				thread.start();
			}
		}
		catch(JSchException | IOException e) {
			System.out.println(e);
		}
	}
}
