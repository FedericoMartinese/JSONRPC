package jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;
import java.security.InvalidParameterException;

public class Id {
    //id = string o int o null
    //da specifica id può essere un numero, ma non dovrebbe contenere parti decimali. per questo si usa Integer e non Number

    public enum Types{NULL, STRING, INT}
    private Object value;
    private Types type;
    public Id(int id) {
        value = id;
        type = Types.INT;
    }
    public Id(String id) {
        if (id == null || id.isEmpty()) {
            value = null;
            type = Types.NULL;
        }
        else {
            value = id;
            type = Types.STRING;
        }
    }
    public Id() {
        value = null;//JSONObject.NULL;
        type = Types.NULL;
    }

    public int getInt() {
        if (type != Types.INT) {throw new ClassCastException("Not an integer");}
        return (Integer)value;
    }

    public String getString() {
        if (type != Types.STRING) {throw new ClassCastException("Not a string");}
        return (String)value;
    }

    public boolean isNull() {
        return type==Types.NULL;
    }
    public Types getType() {
        return type;
    }

    static Id toId(Object id) {
        if (id == null) {throw new NullPointerException("Null id");}

        if (id.equals(JSONObject.NULL)) {
            return new Id();
        } else if (id instanceof Integer) {
            return new Id((int)id);
        } else if (id instanceof String) {
            return new Id((String) id);
        } else {
            throw new InvalidParameterException("Invalid id");
        }
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Id))return false;
        Id o = (Id) other;

        switch (this.type) {
            case NULL: return o.type == Types.NULL;
            case STRING: return o.type == Types.STRING && getString().equals(o.getString());
            case INT: return o.type == Types.INT && ((Integer)getInt()).equals(o.getInt());
            default: return false;
        }

    }

    public static Id getIdFromRequest(String request) {
        try {
            JSONObject obj = new JSONObject(request);
            return toId(obj.get(AbstractRequest.Members.ID.toString()));
        } catch (JSONException e) {
            return new Id(); //se non è possibile recuperarlo della richiesta si crea un id nullo
        }
    }
}
