package com.example.emocheck.chat;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class getRequest {
    private RequestQueue queue;
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=YOUR_API_KEY_HERE";

    public getRequest(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(String reply);
    }

    public void getResponse(String message, final VolleyResponseListener volleyResponseListener) {
        try {
            // Construct the JSON payload
            JSONObject part = new JSONObject();
            part.put("text", message);

            JSONArray parts = new JSONArray();
            parts.put(part);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject payload = new JSONObject();
            payload.put("contents", contents);

            // Create the request
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_URL, payload,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                // Log response for debugging
                                Log.d("API RESPONSE", response.toString());

                                // Navigate through the response to get the reply text
                                JSONArray candidates = response.getJSONArray("candidates");  // Extract candidates array
                                JSONObject candidate = candidates.getJSONObject(0);  // Get the first candidate
                                JSONObject content = candidate.getJSONObject("content");  // Get the content part
                                JSONArray parts = content.getJSONArray("parts");  // Get parts array
                                String reply = parts.getJSONObject(0).getString("text");  // Get the text from the first part

                                // Return the response to the listener
                                volleyResponseListener.onResponse(reply);

                            } catch (Exception e) {
                                // Handle errors in response parsing
                                volleyResponseListener.onError("Response parsing error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Handle API request error
                            volleyResponseListener.onError("API request error: " + error.getMessage());
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            // Add the request to the queue
            queue.add(request);

        } catch (JSONException e) {
            // Handle JSON construction error
            volleyResponseListener.onError("JSON construction error: " + e.getMessage());
        }
    }
}
