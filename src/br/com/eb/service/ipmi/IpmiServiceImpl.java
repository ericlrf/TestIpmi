package br.com.eb.service.ipmi;

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import com.veraxsystems.vxipmi.coding.commands.fru.BaseUnit;
import com.veraxsystems.vxipmi.coding.commands.fru.GetFruInventoryAreaInfo;
import com.veraxsystems.vxipmi.coding.commands.fru.GetFruInventoryAreaInfoResponseData;
import com.veraxsystems.vxipmi.coding.commands.fru.ReadFruData;
import com.veraxsystems.vxipmi.coding.commands.fru.ReadFruDataResponseData;
import com.veraxsystems.vxipmi.coding.commands.fru.record.BoardInfo;
import com.veraxsystems.vxipmi.coding.commands.fru.record.ChassisInfo;
import com.veraxsystems.vxipmi.coding.commands.fru.record.FruRecord;
import com.veraxsystems.vxipmi.coding.commands.fru.record.PowerSupplyInfo;
import com.veraxsystems.vxipmi.coding.commands.fru.record.ProductInfo;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdr;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdrResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSensorReading;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSensorReadingResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepository;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepositoryResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.CompactSensorRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.FruDeviceLocatorRecord;
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

	private static final int MAX_REPO_RECORD_ID = 65535;
	private static final int INITIAL_CHUNK_SIZE = 8;
	private static final int CHUNK_SIZE = 16;
	private static final int HEADER_SIZE = 5;
	private static final int DEFAULT_FRU_ID = 0;
	private static final int FRU_READ_PACKET_SIZE = 16;
	private IpmiConnector connector;
	private ConnectionHandle handle;
	private List<CipherSuite> cipherSuites;
	private CipherSuite cs;
	private int nextRecId;

	public IpmiServiceImpl() {
		super();
	}

	public IpmiServiceImpl(int timeout) {
		super();
		PropertiesManager.getInstance().setProperty("timeout", String.valueOf(timeout));
	}

	public int getDefaultTimeout() {
		return Integer.parseInt(PropertiesManager.getInstance().getProperty("timeout"));
	}

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

		for (IpmiData ipmiData : list) {
			ipmiData.setGrupo("Chassis");
		}
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
				} else if (record instanceof CompactSensorRecord) {
					CompactSensorRecord csr = (CompactSensorRecord) record;
					recordReadingId = TypeConverter.byteToInt(csr.getSensorNumber());
					data = new IpmiData(csr.getName(), null);
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

		for (IpmiData ipmiData : list) {
			ipmiData.setGrupo("SDR");
		}
		return list;
	}

	/**
	 * Responsável por receber conjunto de dados que não podem ser recuperados
	 * numa única requisição IPMI. Método baseado em template da biblioteca IPMI
	 * da VeraxSystems.
	 */
	public SensorRecord getSensorData(IpmiConnector connector, ConnectionHandle handle, int reservationId)
			throws Exception {
		try {
			// BMC capabilities are limited - that means that sometimes the
			// record size exceeds maximum size of the message. Since we don't
			// know what is the size of the record, we try to get
			// whole one first
			GetSdr commandCoder = new GetSdr(IpmiVersion.V20, handle.getCipherSuite(), AuthenticationType.RMCPPlus,
					reservationId, nextRecId);
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
				GetSdr commandCoder = new GetSdr(IpmiVersion.V20, handle.getCipherSuite(), AuthenticationType.RMCPPlus,
						reservationId, nextRecId, 0, INITIAL_CHUNK_SIZE);
				GetSdrResponseData data = (GetSdrResponseData) connector.sendMessage(handle, commandCoder);
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

	/**
	 * Recebe informações do inventário do host remoto
	 */
	public List<IpmiData> modulesStatus() throws Exception {
		List<IpmiData> list = new ArrayList<>();
		IpmiData data = null;
		nextRecId = 0;

		ReserveSdrRepository commandCoder = new ReserveSdrRepository(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus);
		ReserveSdrRepositoryResponseData reservation = (ReserveSdrRepositoryResponseData) sendMessage(commandCoder);
		processFru(connector, handle, DEFAULT_FRU_ID, list);
		while (nextRecId < MAX_REPO_RECORD_ID) {
			try {
				SensorRecord record = getSensorData(connector, handle, reservation.getReservationId());
				if (record instanceof FruDeviceLocatorRecord) {
					FruDeviceLocatorRecord fruLocator = (FruDeviceLocatorRecord) record;
					data = new IpmiData("Name", fruLocator.getName());
					list.add(data);
					data = new IpmiData("Access Lun", String.valueOf(fruLocator.getAccessLun()));
					list.add(data);
					data = new IpmiData("Device Access Address", String.valueOf(fruLocator.getDeviceAccessAddress()));
					list.add(data);
					data = new IpmiData("Device Id", String.valueOf(fruLocator.getDeviceId()));
					list.add(data);
					data = new IpmiData("Device Type", fruLocator.getDeviceType().name());
					list.add(data);
					data = new IpmiData("Device Type Modifier", String.valueOf(fruLocator.getDeviceTypeModifier()));
					list.add(data);
					data = new IpmiData("Fru Entity Id", String.valueOf(fruLocator.getFruEntityId()));
					list.add(data);
					data = new IpmiData("Fru Entity Instance", String.valueOf(fruLocator.getFruEntityInstance()));
					list.add(data);
					data = new IpmiData("Management Channel Number",
							String.valueOf(fruLocator.getManagementChannelNumber()));
					list.add(data);
					data = new IpmiData("is Logical", String.valueOf(fruLocator.isLogical()));
					list.add(data);

					if (fruLocator.isLogical()) {
						processFru(connector, handle, fruLocator.getDeviceId(), list);
					}
				}
			} catch (Exception e) {
				ReserveSdrRepository commandCoder2 = new ReserveSdrRepository(IpmiVersion.V20, cs,
						AuthenticationType.RMCPPlus);
				reservation = (ReserveSdrRepositoryResponseData) sendMessage(commandCoder2);
				throw e;
			}
		}

		for (IpmiData ipmiData : list) {
			ipmiData.setGrupo("FRU");
		}
		return list;
	}

	/**
	 * Recebe informações dos módulos listados no inventário do host remoto.
	 * Método baseado em template da biblioteca IPMI da VeraxSystems.
	 */
	private void processFru(IpmiConnector connector, ConnectionHandle handle, int fruId, List<IpmiData> list)
			throws Exception {
		List<ReadFruDataResponseData> fruData = new ArrayList<ReadFruDataResponseData>();
		IpmiData data = null;

		// get the FRU Inventory Area info
		GetFruInventoryAreaInfo commandCoder = new GetFruInventoryAreaInfo(IpmiVersion.V20, handle.getCipherSuite(),
				AuthenticationType.RMCPPlus, fruId);
		GetFruInventoryAreaInfoResponseData info = (GetFruInventoryAreaInfoResponseData) sendMessage(commandCoder);

		int size = info.getFruInventoryAreaSize();
		BaseUnit unit = info.getFruUnit();

		// since the size of single FRU entry can exceed maximum size of the
		// message sent via IPMI, it has to be read in chunks
		for (int i = 0; i < size; i += FRU_READ_PACKET_SIZE) {
			int cnt = FRU_READ_PACKET_SIZE;
			if (i + cnt > size) {
				cnt = size % FRU_READ_PACKET_SIZE;
			}
			try {
				// get single package od FRU data
				ReadFruData commandCoder2 = new ReadFruData(IpmiVersion.V20, handle.getCipherSuite(),
						AuthenticationType.RMCPPlus, fruId, unit, i, cnt);
				ReadFruDataResponseData data2 = (ReadFruDataResponseData) connector.sendMessage(handle, commandCoder2);

				fruData.add(data2);

			} catch (Exception e) {
				throw e;
			}
		}

		try {
			// after collecting all the data, we can combine and parse it
			List<FruRecord> records = ReadFruData.decodeFruData(fruData);

			for (FruRecord record : records) {
				// now we can for example display received info about board
				if (record instanceof BoardInfo) {
					String module = record.getClass().getSimpleName();
					BoardInfo bi = (BoardInfo) record;
					LocalDateTime localDateTime = LocalDateTime
							.ofInstant(Instant.ofEpochMilli(bi.getMfgDate().getTime()), ZoneId.systemDefault());
					data = new IpmiData(module + " Board Serial Number", bi.getBoardSerialNumber());
					list.add(data);
					data = new IpmiData(module + " Board Product Name", bi.getBoardProductName());
					list.add(data);
					data = new IpmiData(module + " Board Part Number", bi.getBoardPartNumber());
					list.add(data);
					data = new IpmiData(module + " Board Manufacturer", bi.getBoardManufacturer());
					list.add(data);
					data = new IpmiData(module + " Mfg Date", localDateTime.toString());
					list.add(data);

				} else if (record instanceof ChassisInfo) {
					String module = record.getClass().getSimpleName();
					ChassisInfo ci = (ChassisInfo) record;
					data = new IpmiData(module + " Chassis Part Number", ci.getChassisPartNumber());
					list.add(data);
					data = new IpmiData(module + " Chassis Serial Number", ci.getChassisSerialNumber());
					list.add(data);
					data = new IpmiData(module + " Chassis Type", ci.getChassisType().toString());
					list.add(data);

				} else if (record instanceof ProductInfo) {
					String module = record.getClass().getSimpleName();
					ProductInfo pi = (ProductInfo) record;
					System.out.println();
					data = new IpmiData(module + " Asset Tag", pi.getAssetTag());
					list.add(data);
					data = new IpmiData(module + " Manufacturer Name", pi.getManufacturerName());
					list.add(data);
					data = new IpmiData(module + " ProductModel Number", pi.getProductModelNumber());
					list.add(data);
					data = new IpmiData(module + " Product Name", pi.getProductName());
					list.add(data);
					data = new IpmiData(module + " Product Serial Number", pi.getProductSerialNumber());
					list.add(data);
					data = new IpmiData(module + " Product Version", pi.getProductVersion());
					list.add(data);
				} else {
					// The record could be:
					// SpdInfo: Stub for future implementation of DIMM SPD
					// information format.
					// MultiRecordInfo: BaseCompatibilityInfo, DcLoadInfo,
					// DcOutputInfo, ManagementAccessInfo, OemInfo,
					// PowerSupplyInfo.
				}
			}

		} catch (Exception e) {
			throw e;
		}

	}
}
