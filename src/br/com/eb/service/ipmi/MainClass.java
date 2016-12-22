package br.com.eb.service.ipmi;

import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatusResponseData;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;

public class MainClass {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		IpmiServiceImpl impl = new IpmiServiceImpl();
		impl.createConnection(6000, "10.0.1.245");
		CipherSuite cs = impl.encryptConnection().get(3);
		impl.openConnection(cs, PrivilegeLevel.Administrator, "ADMIN", "ADMIN", null);
		
		
		for (IpmiData data : impl.chassisStatus(cs)) {
			System.out.print(data.getNome());
			System.out.print(": ");
			System.out.println(data.getValor());
		}
		
		impl.closeConnection();
		impl.releaseConnection();
	}

}
