package Common;

import Common.Protocol.DPIProtocolMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Created by Lior on 20/11/2014.
 */
public class JsonUtils {
	/**
	 * this static method generate a middlebox message object represent the json
	 * message string
	 * 
	 * @param message
	 *            a json string, represent a middlebox message
	 * @return sub type of MiddleboxMessage, the message object
	 */
	public static DPIProtocolMessage fromJson(String message) {
		Gson gson = new Gson();
		JsonElement msgTree = new JsonParser().parse(message);
		JsonElement className = msgTree.getAsJsonObject().get("className");

		try {
			return (DPIProtocolMessage) gson.fromJson(message,
					Class.forName(className.getAsString()));
		} catch (ClassNotFoundException e) {
			return null;
		}

	}

	/**
	 * @param msg
	 * @return json representation of the input msg
	 */
	public static String toJson(DPIProtocolMessage msg) {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		return gson.toJson(msg);
	}
}
