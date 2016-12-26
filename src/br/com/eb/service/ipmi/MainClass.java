package br.com.eb.service.ipmi;

import java.util.ArrayList;
import java.util.List;

public class MainClass {

	public static void main(String[] args) {
		IpmiServiceImpl impl = new IpmiServiceImpl();
		List<IpmiData> ipmiList = new ArrayList<>();

		try {
			impl.createConnection(6000, "10.0.1.245");
			impl.encryptConnection();
			impl.openConnection("ADMIN", "ADMIN", null);
			ipmiList.addAll(impl.chassisStatus());
			 ipmiList.addAll(impl.sensorStatus());
			impl.closeConnection();
			impl.releaseConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (IpmiData data : ipmiList) {
			System.out.print(data.getNome());
			System.out.print(": ");
			System.out.println(data.getValor());
		}

	}

}
