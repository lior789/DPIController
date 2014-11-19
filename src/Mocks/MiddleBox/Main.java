package Mocks.MiddleBox;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    static String USAGE = "USAGE: controllerIp controller port";
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println(USAGE);
            return;
        }


	    String controllerIpStr = args[0];
        String ControllerPortStr = args[1];
        try {
            InetAddress controllerIp = Inet4Address.getByName(controllerIpStr);
            int controllerPort = Integer.parseInt(ControllerPortStr);
            MockMiddleBox mmd = new MockMiddleBox(controllerIp,controllerPort,"mb1","ui middle-box"); //todo: change hard-coded
            mmd.run();
        } catch (UnknownHostException e) {
            System.out.println(controllerIpStr+" is an invalid Ip address");

            System.out.println(USAGE);
        }
        catch (NumberFormatException e){
            System.out.println(ControllerPortStr+" is an invalid port address");
            System.out.println(USAGE);
        }



    }
}
