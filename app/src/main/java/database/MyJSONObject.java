package database;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * optimized Jsonobject
 */
public class MyJSONObject extends JSONObject {

    JSONObject myJsonObject;
    public MyJSONObject(JSONObject jsonObject){
        myJsonObject = jsonObject;
    }
    @Override
    public String getString(String name) throws JSONException {
        String value = myJsonObject.getString(name);
        if(value.equals("null")){
            return null;
        }
        return value;
    }

    @Override
    public int getInt(String name) throws JSONException {
        return myJsonObject.getInt(name);
    }

}
