package nl.radiantrealm.library.util.json;

public interface JsonElement {

    default String prettyPrint() {
        return prettyPrint(2, 0);
    }

    default String prettyPrint(int depth, int offset) {
        return toString();
    }
    
    default JsonArray getAsJsonArray() {
        if (this instanceof JsonArray array) {
            return array;
        }
        
        throw new JsonInstanceException("Not a JSON array");
    }
    
    default JsonObject getAsJsonObject() {
        if (this instanceof JsonObject object) {
            return object;
        }
        
        throw new JsonInstanceException("Not a JSON object");
    }
    
    default JsonBoolean getAsJsonBoolean() {
        if (this instanceof JsonBoolean bool) {
            return bool;
        }

        throw new JsonInstanceException("Not a JSON boolean");
    }
    
    default JsonNumber getAsJsonNumber() {
        if (this instanceof JsonNumber number) {
            return number;
        }

        throw new JsonInstanceException("Not a JSON number");
    }
    
    default JsonNull getAsJsonNull() {
        if (this instanceof JsonNull jsonNull) {
            return jsonNull;
        }

        throw new JsonInstanceException("Not a JSON object");
    }
    
    default JsonString getAsJsonString() {
        if (this instanceof JsonString string) {
            return string;
        }

        throw new JsonInstanceException("Not a JSON string");
    }
}
