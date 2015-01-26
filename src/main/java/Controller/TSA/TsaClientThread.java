package Controller.TSA;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import Common.JsonUtils;
import Common.Protocol.DPIProtocolMessage;
import Common.Protocol.TSA.PolicyChainRequest;
import Common.Protocol.TSA.PolicyChainsData;
import Common.Protocol.TSA.RawPolicyChain;

import com.google.gson.Gson;

public abstract class TsaClientThread extends Thread {

	protected static final Logger LOGGER = Logger
			.getLogger(TSASocketClient.class);
	protected final TSAFacadeImpl _tsaFacade;
	protected Properties _props;

	public TsaClientThread(TSAFacadeImpl tsaFacade) {
		super();
		this._tsaFacade = tsaFacade;
		initProperties();
	}

	abstract void waitForInstructions();

	abstract void connectToTSA() throws Exception;

	abstract boolean sendMessage(String policyChainRequest);

	public static String generatePolicyChainMessage(List<RawPolicyChain> chains) {
		return JsonUtils.toJson(new PolicyChainsData(chains));
	}

	public static PolicyChainsData parsePolicyChain(String msg) {
		DPIProtocolMessage chain = JsonUtils.fromJson(msg);
		if (chain instanceof PolicyChainsData) {
			return (PolicyChainsData) chain;
		} else {
			return null;
		}
	}

	protected void initProperties() {
		try {// TODO: move to singleton
			FileInputStream input = new FileInputStream("config.properties");
			_props = new Properties();
			_props.load(input);
		} catch (Exception e) {
			LOGGER.error("config.properties missing!");
		}
	}

	@Override
	public void run() {
		try {
			connectToTSA();
			sleep(3000);
			sendPolicyChainRequest();
			waitForInstructions();
		} catch (Exception e) {
			LOGGER.error("cant connect to TSA :" + e.getMessage());
			e.printStackTrace();
		}
	}

	private void sendPolicyChainRequest() {
		LOGGER.info("sending TSA request");
		sendMessage(getPolicyChainRequest());
	}

	String getPolicyChainRequest() {
		return new Gson().toJson(new PolicyChainRequest());
	}

	public void sendPolicyChains(List<RawPolicyChain> chains) {
		LOGGER.info("updating policyChains: ");
		LOGGER.info(chains);
		sendMessage(generatePolicyChainMessage(chains));
	}

	protected void handleIncomingMessage(String inputLine) {
		List<RawPolicyChain> chain = parsePolicyChain(inputLine).chains;
		LOGGER.info("got policyChain from TSA: ");
		LOGGER.info(chain);
		if (chain != null && chain.size() > 0) {
			LOGGER.info("valid chains");
			_tsaFacade.setPolicyChains(chain);
		}
	}

}