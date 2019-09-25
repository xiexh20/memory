package database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class MyErrorResponse implements Response.ErrorListener {
    private Context responseContext;

    public MyErrorResponse(Context responseContext) {
        this.responseContext = responseContext;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d("ERROR",error.toString());
        Toast.makeText(responseContext.getApplicationContext(), "Network failed", Toast.LENGTH_SHORT).show();
    }


}
