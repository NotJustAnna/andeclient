package pw.aru.libs.andeclient.entities.player;

import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * This class interacts with the Andesite filters
 */
public abstract class PlayerFilter {
    @Nonnull
    public abstract Map.Entry<String, JSONObject> updatePayload();

    public static class Raw extends PlayerFilter implements Map.Entry<String, JSONObject> {
        private final String key;
        private JSONObject value;

        public Raw(String key, JSONObject value) {
            this.key = key;
            this.value = value;
        }

        @Nonnull
        @Override
        public Map.Entry<String, JSONObject> updatePayload() {
            return this;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public JSONObject getValue() {
            return value;
        }

        @Override
        public JSONObject setValue(JSONObject value) {
            JSONObject lastValue = this.value;
            this.value = value;
            return lastValue;
        }
    }
}
