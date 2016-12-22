package br.com.eb.service.ipmi;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatusResponseData;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;

/**
 * Classe responsável por implementar operações em IPMI.
 * @author Eric Rodrigues (eric@usto.re)
 * */
public class IpmiServiceImpl implements IpmiService {

	private IpmiConnector connector;
	private ConnectionHandle handle;
	private List<CipherSuite> cipherSuites;

	@Override
	public void createConnection(int port, String address) throws Exception {
		connector = new IpmiConnector(port);
		handle = connector.createConnection(InetAddress.getByName(address));
	}

	@Override
	public List<CipherSuite> encryptConnection() throws Exception {
		cipherSuites = connector.getAvailableCipherSuites(handle);// size=15
		return cipherSuites;
	}

	@Override
	public void openConnection(CipherSuite cs, PrivilegeLevel pl, String username, String password, byte[] bmcKey)
			throws Exception {
		connector.getChannelAuthenticationCapabilities(handle, cs, pl);
		connector.openSession(handle, username, password, bmcKey);
	}

	@Override
	public ResponseData sendMessage(IpmiCommandCoder commandCoder) throws Exception {
		ResponseData rd = connector.sendMessage(handle, commandCoder);
		return rd;

	}

	@Override
	public void closeConnection() throws Exception {
		connector.closeSession(handle);
	}

	@Override
	public void releaseConnection() throws Exception {
		connector.tearDown();
	}
	
	/**
	 * Recebe status do chassis de host remoto
	 * */
	public void chassisStatus(CipherSuite cs) throws Exception{
		GetChassisStatus commandCoder = new GetChassisStatus(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus);
		GetChassisStatusResponseData rd = (GetChassisStatusResponseData) sendMessage(commandCoder);
		List<IpmiData> list = new ArrayList<>();
		IpmiData data;
		data = new IpmiData("Current Power State", String.valueOf(rd.getCurrentPowerState()));
		list.add(data);
		data = new IpmiData("", String.valueOf());
		list.add(data);
	}

}
