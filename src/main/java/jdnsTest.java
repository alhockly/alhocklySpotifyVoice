import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.impl.HostInfo;
import javax.jmdns.impl.JmDNSImpl;
import javax.jmdns.impl.ServiceInfoImpl;

public class jdnsTest {

    public static SampleListener listener = new SampleListener();

    private static class SampleListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            System.out.println("Service added: " + event.getInfo());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            System.out.println("Service removed: " + event.getInfo());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            System.out.println("Service resolved: " + event.getInfo());
        }
    }



    public static void main(String[] args) throws InterruptedException, IOException {

//        JmDNSImpl jmdns = new JmDNSImpl(null, null);
//       // ServiceInfo.create()
//        //jmdns.startServiceInfoResolver(ServiceInfoImpl..create("_http._tcp.local","lights",80,"lights"));
//        final HostInfo hostInfo = HostInfo.newHostInfo(InetAddress.getByName("192.168.1.118"), jmdns, null);
//        System.out.println("MDNS hostname (Bonjour): " + hostInfo.getName());
//        System.out.println("DNS hostname: " + hostInfo.getInetAddress().getHostName());
//        System.out.println("IP address: " + hostInfo.getInetAddress().getHostAddress());
//        jmdns.close();

        try {
            // Create a JmDNS instance
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

            // Add a service listener
            jmdns.addServiceListener("_http._tcp.local.", new SampleListener());

            // Wait a bit
            Thread.sleep(30000);
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}