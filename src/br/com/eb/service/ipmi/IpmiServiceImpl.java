package br.com.eb.service.ipmi;

import java.net.InetAddress;
import java.util.List;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatusResponseData;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;

public class IpmiServiceImpl implements IpmiService {

	private IpmiConnector connector;
	private ConnectionHandle handle;
	private CipherSuite cs;
	
	@Override
	public void openConnection(int port, String address, PrivilegeLevel privilegeLevel, String username, String password, byte[] bmcKey) throws Exception {
		connector = new IpmiConnector(port);
		handle = connector.createConnection(InetAddress.getByName(address));
		
		List<CipherSuite> cipherSuites = connector.getAvailableCipherSuites(handle);//size=15
		cs = cipherSuites.get(3); //3
		
		connector.getChannelAuthenticationCapabilities(handle, cs, privilegeLevel);
		connector.openSession(handle, username, password, bmcKey);
	}


	@Override
	public void sendMessageChassis() throws Exception {
		GetChassisStatusResponseData rd = (GetChassisStatusResponseData) connector.sendMessage(handle, new GetChassisStatus(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus));
		System.out.println("Servidor remoto está ligado? "+rd.isPowerOn());
	}

	@Override
	public void sendMessageSensors() throws Exception{
		
	}
	
	@Override
	public void closeConnection() throws Exception {
		connector.closeSession(handle);
		connector.tearDown();
	}
	
	public static void main(String[] args) throws Exception {
		IpmiServiceImpl impl = new IpmiServiceImpl();
		impl.openConnection(6000, "10.0.1.245", PrivilegeLevel.Administrator, "ADMIN", "ADMIN", null);
		impl.sendMessageChassis();
		impl.closeConnection();
	}

}
