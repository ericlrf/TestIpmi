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
		impl.chassisStatus(cs);
		
		GetChassisStatus commandCoder = new GetChassisStatus(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus);
		GetChassisStatusResponseData rd = (GetChassisStatusResponseData) impl.sendMessage(commandCoder);

				System.out.println("|> RESPONSE: " + rd.getCurrentPowerState());
		//		System.out.println("|> RESPONSE: " + rd.getPowerRestorePolicy());
		
		System.out.println("|> RESPONSE: " + rd.isPowerControlFault());
		System.out.println("|> RESPONSE: " + rd.isPowerFault());
		System.out.println("|> RESPONSE: " + rd.isInterlock());
		System.out.println("|> RESPONSE: " + rd.isPowerOverload());
		System.out.println("|> RESPONSE: " + rd.isPowerOn());

				System.out.println("|> RESPONSE: " + rd.getLastPowerEvent());
		
		System.out.println("|> RESPONSE: " + rd.wasIpmiPowerOn());
		System.out.println("|> RESPONSE: " + rd.wasPowerFault());
		System.out.println("|> RESPONSE: " + rd.wasInterlock());
		System.out.println("|> RESPONSE: " + rd.wasPowerOverload());
		System.out.println("|> RESPONSE: " + rd.acFailed());

				System.out.println("|> RESPONSE: " + rd.getMiscChassisState());
		
		System.out.println("|> RESPONSE: " + rd.isChassisIdentifyCommandSupported());

		//		System.out.println("|> RESPONSE: " + rd.getChassisIdentifyState());
		
		System.out.println("|> RESPONSE: " + rd.coolingFaultDetected());
		System.out.println("|> RESPONSE: " + rd.driveFaultDetected());
		System.out.println("|> RESPONSE: " + rd.isFrontPanelLockoutActive());
		System.out.println("|> RESPONSE: " + rd.isChassisIntrusionActive());

				System.out.println("|> RESPONSE: " + rd.getFrontPanelButtonCapabilities());

		/**Exception in thread "main" java.lang.IllegalAccessException: Front Panel Button Capabilities not set
	at com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatusResponseData.isStandbyButtonDisabled(GetChassisStatusResponseData.java:268)
	at br.com.eb.service.ipmi.MainClass.main(MainClass.java:58)* */
//		System.out.println("|> RESPONSE: " + rd.isStandbyButtonDisableAllowed());
//		System.out.println("|> RESPONSE: " + rd.isDiagnosticInterruptButtonDisableAllowed());
//		System.out.println("|> RESPONSE: " + rd.isResetButtonDisableAllowed());
//		System.out.println("|> RESPONSE: " + rd.isResetButtonDisableAllowed());
//		System.out.println("|> RESPONSE: " + rd.isPowerOffButtonDisableAllowed());
//		System.out.println("|> RESPONSE: " + rd.isStandbyButtonDisabled());
//		System.out.println("|> RESPONSE: " + rd.isDiagnosticInterruptButtonDisabled());
//		System.out.println("|> RESPONSE: " + rd.isResetButtonDisabled());
//		System.out.println("|> RESPONSE: " + rd.isPowerOffButtonDisabled());
//		System.out.println("|> RESPONSE: " + rd.isFrontPanelButtonCapabilitiesSet());
		
		
		impl.closeConnection();
		impl.releaseConnection();
	}

}
