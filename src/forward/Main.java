package forward;
import java.io.IOException;
import java.util.Scanner;

import com.jcraft.jsch.JSchException;

public class Main {

	public static void main(String[] args) throws IOException, JSchException {

		Scanner scan = new Scanner(System.in);
		System.out.print("Enter password: ");
		String password = scan.nextLine();
		scan.close();
		
		Thread t = new SshDynPortForward("till", "tillkrischer.com", password, 1080);
		t.start();
	}

}
