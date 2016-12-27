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
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdr;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdrResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSensorReading;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSensorReadingResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepository;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepositoryResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.CompactSensorRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.FullSensorRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.ReadingType;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.SensorRecord;
import com.veraxsystems.vxipmi.coding.payload.CompletionCode;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.PropertiesManager;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Classe responsável por implementar operações em IPMI.
 * 
 * @author Eric Rodrigues (eric@usto.re)
 * @since 22-Dezembro-2016
 */
public class IpmiServiceImpl implements IpmiService {

//	private static final String INITIAL_DEFAULT_TIMEOUT = PropertiesManager.getInstance().getProperty("timeout");
	private static final int MAX_REPO_RECORD_ID = 65535;
	private static final int INITIAL_CHUNK_SIZE = 8;
	private static final int CHUNK_SIZE = 16;
	private static final int HEADER_SIZE = 5;
	private IpmiConnector connector;
	private ConnectionHandle handle;
	private List<CipherSuite> cipherSuites;
	private CipherSuite cs;
	private int nextRecId;

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
		IpmiData data = null;

		nextRecId = 0;
		int reservationId = 0;
		int lastReservationId = -1;
		connector.setTimeout(handle, 2750);
		while (nextRecId < MAX_REPO_RECORD_ID) {
			
			SensorRecord record = null;
			try {
				record = getSensorData(connector, handle, reservationId);
				int recordReadingId = -1;
				if (record instanceof FullSensorRecord) {
					FullSensorRecord fsr = (FullSensorRecord) record;
					recordReadingId = TypeConverter.byteToInt(fsr.getSensorNumber());
					data = new IpmiData(fsr.getName(), null);
//					data.setNome(fsr.getName());
				} else if (record instanceof CompactSensorRecord) {
					CompactSensorRecord csr = (CompactSensorRecord) record;
					recordReadingId = TypeConverter.byteToInt(csr.getSensorNumber());
					data = new IpmiData(csr.getName(), null);
//					data.setNome(csr.getName());
				}
				GetSensorReadingResponseData data2 = null;
				try {
					if (recordReadingId >= 0) {
						GetSensorReading commandCoder = new GetSensorReading(IpmiVersion.V20, cs,
								AuthenticationType.RMCPPlus, recordReadingId);
						data2 = (GetSensorReadingResponseData) sendMessage(commandCoder);
						if (record instanceof FullSensorRecord) {
							FullSensorRecord rec = (FullSensorRecord) record;
							data.setValor(data2.getSensorReading(rec) + " " + rec.getSensorBaseUnit().toString());
							list.add(data);
						} else if (record instanceof CompactSensorRecord) {
							CompactSensorRecord rec = (CompactSensorRecord) record;
							List<ReadingType> events = data2.getStatesAsserted(rec.getSensorType(),
									rec.getEventReadingType());
							String s = "";
							for (int i = 0; i < events.size(); ++i) {
								s += events.get(i) + ", ";
							}
							data.setValor(s);
							list.add(data);
						}
					}
				} catch (IPMIException e) {
					if (e.getCompletionCode() == CompletionCode.DataNotPresent) {
						list.add(data);
					} else {
						throw e;
					}
				}

			} catch (IPMIException e) {
				if (lastReservationId == reservationId)
					throw e;
				lastReservationId = reservationId;
				ReserveSdrRepository commandCoder = new ReserveSdrRepository(IpmiVersion.V20, cs,
						AuthenticationType.RMCPPlus);
				reservationId = ((ReserveSdrRepositoryResponseData) sendMessage(commandCoder)).getReservationId();
			}
			
		}

		return list;
	}

	/**
	 * Based on Verax Systems template.
	 * */
	public SensorRecord getSensorData(IpmiConnector connector, ConnectionHandle handle, int reservationId)
			throws Exception {
		try {
			// BMC capabilities are limited - that means that sometimes the
			// record size exceeds maximum size of the message. Since we don't
			// know what is the size of the record, we try to get
			// whole one first
			GetSdr commandCoder = new GetSdr(IpmiVersion.V20,
					handle.getCipherSuite(), AuthenticationType.RMCPPlus, reservationId, nextRecId);
			GetSdrResponseData data = (GetSdrResponseData) sendMessage(commandCoder);
			// If getting whole record succeeded we create SensorRecord from
			// received data...
			SensorRecord sensorDataToPopulate = SensorRecord.populateSensorRecord(data.getSensorRecordData());
			// ... and update the ID of the next record
			nextRecId = data.getNextRecordId();
			return sensorDataToPopulate;
		} catch (IPMIException e) {
			// System.out.println(e.getCompletionCode() + ": " +
			// e.getMessage());
			// The following error codes mean that record is too large to be
			// sent in one chunk. This means we need to split the data in
			// smaller parts.
			if (e.getCompletionCode() == CompletionCode.CannotRespond
					|| e.getCompletionCode() == CompletionCode.UnspecifiedError) {
				System.out.println("Getting chunks");
				// First we get the header of the record to find out its size.
				GetSdrResponseData data = (GetSdrResponseData) connector.sendMessage(handle,
						new GetSdr(IpmiVersion.V20, handle.getCipherSuite(), AuthenticationType.RMCPPlus, reservationId,
								nextRecId, 0, INITIAL_CHUNK_SIZE));
				// The record size is 5th byte of the record. It does not take
				// into account the size of the header, so we need to add it.
				int recSize = TypeConverter.byteToInt(data.getSensorRecordData()[4]) + HEADER_SIZE;
				int read = INITIAL_CHUNK_SIZE;

				byte[] result = new byte[recSize];

				System.arraycopy(data.getSensorRecordData(), 0, result, 0, data.getSensorRecordData().length);

				// We get the rest of the record in chunks (watch out for
				// exceeding the record size, since this will result in BMC's
				// error.
				while (read < recSize) {
					int bytesToRead = CHUNK_SIZE;
					if (recSize - read < bytesToRead) {
						bytesToRead = recSize - read;
					}
					GetSdrResponseData part = (GetSdrResponseData) connector.sendMessage(handle,
							new GetSdr(IpmiVersion.V20, handle.getCipherSuite(), AuthenticationType.RMCPPlus,
									reservationId, nextRecId, read, bytesToRead));

					System.arraycopy(part.getSensorRecordData(), 0, result, read, bytesToRead);

					System.out.println("Received part");

					read += bytesToRead;
				}

				// Finally we populate the sensor record with the gathered
				// data...
				SensorRecord sensorDataToPopulate = SensorRecord.populateSensorRecord(result);
				// ... and update the ID of the next record
				nextRecId = data.getNextRecordId();
				return sensorDataToPopulate;
			} else {
				throw e;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/***/
	public List<IpmiData> modulesStatus() throws Exception{
		List<IpmiData> list = new ArrayList<>();
		
		return list;
	}
}
