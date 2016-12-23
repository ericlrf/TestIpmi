package br.com.eb.service.ipmi;


public class MainClass {

	public static void main(String[] args) throws Exception{

		IpmiServiceImpl impl = new IpmiServiceImpl();
		impl.createConnection(6000, "10.0.1.245");
		impl.encryptConnection();
		impl.openConnection("ADMIN", "ADMIN", null);
		
		
		for (IpmiData data : impl.chassisStatus()) {
			System.out.print(data.getNome());
			System.out.print(": ");
			System.out.println(data.getValor());
		}
		
		impl.closeConnection();
		impl.releaseConnection();
	}

}
