package br.com.eb.service.ipmi;

import java.net.InetAddress;
import java.util.List;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.async.IpmiAsyncConnector;
import com.veraxsystems.vxipmi.api.async.IpmiListener;
import com.veraxsystems.vxipmi.api.async.messages.IpmiResponse;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;

/**
 * Classe que reusa quase todo codigo de 'IpmiServiceImpl', contudo implementa
 * configurações de conexão assincrona da interface 'IpmiListener'. Baseada na
 * classe com.veraxsystems.vximpi.test.AsyncApiTest
 */
public class IpmiServiceAsync extends IpmiServiceImpl implements IpmiListener {
	private static IpmiAsyncConnector connector;
	private IpmiResponse response;

	public IpmiServiceAsync() {
		super();
	}

	public void createConnection(int port, String address) throws Exception {
		connector = new IpmiAsyncConnector(port);
		connector.registerListener(this);
		// parametro 'this' faz referencia a instancia (esta classe) do
		// 'IpmiListener' (interface implementada).
		super.handle = connector.createConnection(InetAddress.getByName(address));
	}

	@Override
	public void notify(IpmiResponse response) {
		this.response = response;
	}

}
