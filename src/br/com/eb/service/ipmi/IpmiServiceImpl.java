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
import com.veraxsystems.vxipmi.common.PropertiesManager;

/**
 * Classe responsável por implementar operações em IPMI.
 * 
 * @author Eric Rodrigues (eric@usto.re)
 * @since 22-Dezembro-2016
 */
public class IpmiServiceImpl implements IpmiService {

	private static final int MAX_REPO_RECORD_ID = 65535;
	private static final String INITIAL_DEFAULT_TIMEOUT = PropertiesManager.getInstance().getProperty("timeout");
	private IpmiConnector connector;
	private ConnectionHandle handle;
	private List<CipherSuite> cipherSuites;
	private CipherSuite cs;

	@Override
	public void createConnection(int port, String address) throws Exception {
		connector = new IpmiConnector(port);
		handle = connector.createConnection(InetAddress.getByName(address));
	}

	@Override
	public void encryptConnection() throws Exception {
		cipherSuites = connector.getAvailableCipherSuites(handle);// maxSize=15
		if (cipherSuites.size() > 3) {
			cs = cipherSuites.get(3);
		} else if (cipherSuites.size() > 2) {
			cs = cipherSuites.get(2);
		} else if (cipherSuites.size() > 1) {
			cs = cipherSuites.get(1);
		} else {
			cs = cipherSuites.get(0);
		}
	}

	@Override
	public void openConnection(String username, String password, byte[] bmcKey) throws Exception {
		connector.getChannelAuthenticationCapabilities(handle, cs, PrivilegeLevel.Administrator);
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
	 * Recebe informações do chassis do host remoto
	 */
	public List<IpmiData> chassisStatus() throws Exception {
		GetChassisStatus commandCoder = new GetChassisStatus(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus);
		GetChassisStatusResponseData rd = (GetChassisStatusResponseData) sendMessage(commandCoder);
		List<IpmiData> list = new ArrayList<>();
		IpmiData data;
		data = new IpmiData("Current Power State", String.valueOf(rd.getCurrentPowerState()));
		list.add(data);
		data = new IpmiData("is Power Control Fault", String.valueOf(rd.isPowerControlFault()));
		list.add(data);
		data = new IpmiData("is Power Fault", String.valueOf(rd.isPowerFault()));
		list.add(data);
		data = new IpmiData("is Inter lock", String.valueOf(rd.isInterlock()));
		list.add(data);
		data = new IpmiData("is Power Over load", String.valueOf(rd.isPowerOverload()));
		list.add(data);
		data = new IpmiData("is Power On", String.valueOf(rd.isPowerOn()));
		list.add(data);
		data = new IpmiData("Last Power Event", String.valueOf(rd.getLastPowerEvent()));
		list.add(data);
		data = new IpmiData("was Ipmi Power On", String.valueOf(rd.wasIpmiPowerOn()));
		list.add(data);
		data = new IpmiData("was Power Fault", String.valueOf(rd.wasPowerFault()));
		list.add(data);
		data = new IpmiData("was Inter lock", String.valueOf(rd.wasInterlock()));
		list.add(data);
		data = new IpmiData("was Power Over load", String.valueOf(rd.wasPowerOverload()));
		list.add(data);
		data = new IpmiData("ac Failed", String.valueOf(rd.acFailed()));
		list.add(data);
		data = new IpmiData("Misc Chassis State", String.valueOf(rd.getMiscChassisState()));
		list.add(data);
		data = new IpmiData("is Chassis Identify Command Supported",
				String.valueOf(rd.isChassisIdentifyCommandSupported()));
		list.add(data);
		data = new IpmiData("cooling Fault Detected", String.valueOf(rd.coolingFaultDetected()));
		list.add(data);
		data = new IpmiData("drive Fault Detected", String.valueOf(rd.driveFaultDetected()));
		list.add(data);
		data = new IpmiData("Front Panel Lockout Active", String.valueOf(rd.isFrontPanelLockoutActive()));
		list.add(data);
		data = new IpmiData("Chassis Intrusion Active", String.valueOf(rd.isChassisIntrusionActive()));
		list.add(data);
		data = new IpmiData("Front Panel Button Capabilities", String.valueOf(rd.getFrontPanelButtonCapabilities()));
		list.add(data);

		return list;
	}

	/**
	 * Recebe informações dos sensores do host remoto
	 */
	public List<IpmiData> sensorStatus() throws Exception {
		
		
        
        
		List<IpmiData> list = new ArrayList<>();
		IpmiData data;
		data = new IpmiData("", String.valueOf(0));
		list.add(data);
		return list;
	}

}
