package pw.aru.libs.andeclient.entities.player;

import com.grack.nanojson.JsonObject;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * This class interacts with the Andesite filters
 */
public abstract class PlayerFilter {
    @Nonnull
    public abstract Map.Entry<String, JsonObject> updatePayload();

    public static class Raw extends PlayerFilter implements Map.Entry<String, JsonObject> {
        private final String key;
        private JsonObject value;

        public Raw(String key, JsonObject value) {
            this.key = key;
            this.value = value;
        }

        @Nonnull
        @Override
        public Map.Entry<String, JsonObject> updatePayload() {
            return this;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public JsonObject getValue() {
            return value;
        }

        @Override
        public JsonObject setValue(JsonObject value) {
            JsonObject lastValue = this.value;
            this.value = value;
            return lastValue;
        }
    }
}
