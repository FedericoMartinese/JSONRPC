package jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;

abstract class JsonRpcMessage extends JsonRpcObj {
    Id id; //può essere String o Integer (o null in alcuni casi (non notifica))
    static final String VER = "2.0";

    public Id getId() {
        //id nullo è diverso da notifica
        if (id == null) {
            throw new NullPointerException("Notify: id undefined"); //è notifica
        }
        return id;
    }

    static void putId(JSONObject obj, String key, Id id) {
        try {
            switch (id.getType()) {
                case INT: obj.put(key, id.getInt()); break;
                case STRING: obj.put(key, id.getString()); break;
                case NULL: obj.put(key, JSONObject.NULL); break;
            }
        } catch (JSONException e) {
                System.out.println(e.getMessage());
            }
    }
}