package Controller;

/**
 * main class used to run controller Created by Lior on 12/11/2014.
 */
public class Main {
	public static void main(String[] args) {
		try {
			int port = Integer.parseInt(args[0]);
			DPIController controller = new DPIController(port);
			controller.run();
		} catch (Exception e) {
			System.out.println("USAGE: port-number");
		}

	}
}
