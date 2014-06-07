package ozone.owf;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import org.apache.log4j.Logger;

public class JmxSslBootStrapper {

	private static final Logger LOG = Logger
			.getLogger(JmxSslBootStrapper.class);

	int port = 8002;

	String connectUrl = "Undefined";

	private MBeanServer mBeanServer;
	
	public MBeanServer getMBeanServer() {
		return mBeanServer;
	}

	public void init() {
		try{
			final SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
			final SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
	
			LOG.debug("Creating an SSL RMI registry on port [" + port + "]");
			LocateRegistry.createRegistry(port, csf, ssf);
	
			mBeanServer = ManagementFactory.getPlatformMBeanServer();
	
			HashMap<String, Object> env = new HashMap<String, Object>();
			env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
			env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
			env.put("com.sun.jndi.rmi.factory.socket", csf);
	
			LOG.debug("Initialized the environment map: [" + env.toString() + "]");
	
			LOG.debug("Create an RMI connector server");
			final String hostname = InetAddress.getLocalHost().getHostName();
			String serviceURL = "service:jmx:rmi://" + hostname + ":" + port
					+ "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
			JMXServiceURL url = new JMXServiceURL(serviceURL);
	
			connectUrl = serviceURL;
	
			LOG.info("creating server with URL: " + url);
			final JMXConnectorServer cs = JMXConnectorServerFactory
					.newJMXConnectorServer(url, env, mBeanServer);
	
			LOG.debug("Start the RMI connector server on port " + port);
			cs.start();
			LOG.info("Server started at: " + cs.getAddress());
		}catch (Exception e){
			LOG.error("Error starting up JMX SSL BootStrapper:", e);
		}
	
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getConnectUrl() {
		return connectUrl;
	}

	public void setConnectUrl(String connectUrl) {
		this.connectUrl = connectUrl;
	}

}
