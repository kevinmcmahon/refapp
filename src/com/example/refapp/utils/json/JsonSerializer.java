package com.example.refapp.utils.json;

import com.example.refapp.utils.constants.Config;
import com.example.refapp.utils.constants.Constants;
import com.google.inject.Singleton;
import org.apache.http.HttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializationProblemHandler;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.util.Ln;

import java.io.IOException;
import java.io.InputStream;

/**
 * Serialize and deserialize object to and from JSON string
 */
@Singleton
public class JsonSerializer {
	final private ObjectMapper jsonObjectMapper;

	public JsonSerializer() {
		jsonObjectMapper = new ObjectMapper();
		jsonObjectMapper.getDeserializationConfig().addHandler(
				new JsonDeserializationProblemHandler());
	}


	/**
	 * Return JSON content type
	 *
	 * @return String
	 */
	public String getContentType() {
		return Constants.CONTENT_TYPE_JSON;
	}


	/**
	 * Serializes an object and returns the JSON version of that serialized object.
	 *
	 * @param jsonObject The object to be serialized
	 * @return JSON data string
	 * @throws IOException
	 */
	public String serialize(Object jsonObject) throws IOException {
		Ln.d("Before serializing json object ");
		String result = jsonObjectMapper.writeValueAsString(jsonObject);
		Ln.d("After serializing json object - %s", result);
		return result;
	}

	/**
	 * Extracts the HTTP response from the HttpEntity object and return deserialized result object
	 *
	 * @param entity HttpEntity
	 * @param resultType Class of the result object
	 * @param <TResult> Type of the result object
	 * @return Deserialized result object
	 * @throws IOException
	 */
	public <TResult> TResult deserialize(HttpEntity entity,
												Class<? extends TResult> resultType) throws IOException {
		if (Ln.isDebugEnabled()) {
			BufferedHttpEntity buffered = new BufferedHttpEntity(entity);
			String responseText = slurp(buffered.getContent());
			return deserialize(responseText, resultType);
		} else {
			return deserialize(entity.getContent(), resultType);
		}
	}

	/**
	 * Read the JSON string and return the deserialized object
	 *
	 * @param input JSON String
	 * @param resultType Class of the result object
	 * @param <TResult> Type of the result object
	 * @return Deserialized result object
	 * @throws IOException
	 */
	public <TResult> TResult deserialize(String input,
												Class<? extends TResult> resultType) throws IOException {
		Ln.d("Before deserializing json string - %s", input);
		TResult result = jsonObjectMapper.readValue(input, resultType);
		Ln.d("After deserializing json string");
		return result;
	}


	/**
	 * Read the data from the input stream and deserialize the data
	 *
	 * @param input InputStream
	 * @param resultType Class of the result object
	 * @param <TResult> Type of the result object
	 * @return Deserialized result object
	 * @throws IOException
	 */
	public <TResult> TResult deserialize(InputStream input,
												Class<? extends TResult> resultType) throws IOException {
		Ln.d("Before deserializing json string");
		TResult result = jsonObjectMapper.readValue(input, resultType);
		Ln.d("After deserializing json string");
		return result;
	}

	/**
	 * Read string from the input stream
	 *
	 * @param in InputStream
	 * @return String from the input stream
	 * @throws IOException
	 */
	public String slurp(InputStream in) throws IOException {
		StringBuilder out = new StringBuilder();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1; ) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	/**
	 * Handle unknown properties when deserializing JSON string
	 */
	static class JsonDeserializationProblemHandler extends
			DeserializationProblemHandler {
		@Override
		public boolean handleUnknownProperty(DeserializationContext context,
											 org.codehaus.jackson.map.JsonDeserializer<?> deserializer,
											 Object beanOrClass, String propertyName) throws IOException {
			if (Config.LOG_SERIALIZATION) {
				Ln.w("Unable to deserialize class[%s].  Unknow property[%s]",
						beanOrClass.getClass().getName(), propertyName);
			}
			context.getParser().skipChildren();
			return true;
		}
	}
}
