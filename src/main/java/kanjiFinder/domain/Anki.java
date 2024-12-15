package kanjiFinder.domain;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

public class Anki {
    private static final String ANKI_CONNECT_URL = "http://127.0.0.1:8765";
    private String deckName;
    private String fields;

    public Anki() {
        this("","");
    }

    public Anki(String deckName, String fields) {
        this.deckName = deckName;
        this.fields = fields.isEmpty() ? "Word" : fields;
    }


    private JSONObject invoke(String action, JSONObject params) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("action", action);
            requestBody.put("version", 6);
            requestBody.put("params", params);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(ANKI_CONNECT_URL))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject respondBody = new JSONObject(response.body());
            if(!respondBody.isNull("error"))  {
                System.out.println(respondBody.getString("error"));
            }
            return respondBody;
        } catch (IOException | InterruptedException | URISyntaxException e) {
            System.out.println("Request to Anki failed");
            e.printStackTrace();
            return null;
        }
    }

    public  String[] getDeckNames() {
        JSONArray namesJsonArray = invoke("deckNames",null).getJSONArray("result");
        String[] deckNames = new String[namesJsonArray.length()];
        for(int i=0;i<namesJsonArray.length();i++) {
            deckNames[i] = namesJsonArray.optString(i);
        }
        return deckNames;
    }

    public long[] getNoteIdsFromDeck() {
        JSONObject params = new JSONObject();
        params.put("query","deck:" + this.deckName);
        JSONArray notesIdJsonArray = invoke("findNotes",params).getJSONArray("result");
        long[] notesIds = new long[notesIdJsonArray.length()];
        for(int i=0;i<notesIdJsonArray.length();i++) {
            notesIds[i] = notesIdJsonArray.optLong(i);
        }
        return notesIds;
    }

    public String[] getNotesInfoFromIds(long[] ids) {
        JSONArray notes = new JSONArray(ids);
        JSONObject params = new JSONObject();
        params.put("notes",notes);
        JSONArray notesInfoJsonArray = invoke("notesInfo",params).getJSONArray("result");
        String[] notesInfo = new String[notesInfoJsonArray.length()];
        for(int i=0;i<notesInfoJsonArray.length();i++) {
            notesInfo[i] = notesInfoJsonArray.optString(i);
        }
        return notesInfo;
    }

    public String[] getSelectedFieldsOfNotesFromDeck() {
        String[] fieldNames = this.fields.split(",");
        long[] notesId = getNoteIdsFromDeck();
        String[] notesInfo = getNotesInfoFromIds(notesId);
        String[] filteredNotesInfo = new String[notesInfo.length];
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < notesInfo.length; i++) {

            JSONObject fieldsJson = new JSONObject(notesInfo[i]).getJSONObject("fields");
            JSONObject filteredFields = new JSONObject();
            for (String fieldName : fieldNames) {
                if (fieldsJson.has(fieldName)) {
                    String fieldValue = fieldsJson.getJSONObject(fieldName).optString("value");
                    filteredFields.put(fieldName,fieldValue);
                }
                filteredNotesInfo[i] = filteredFields.toString();
            }
        }
        return filteredNotesInfo;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

}
