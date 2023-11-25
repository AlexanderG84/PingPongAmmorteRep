package AuletteBlu.pingpongammorte;

import com.google.gson.*;
import java.lang.reflect.Type;

import AuletteBlu.pingpongammorte.Match;

public class MatchDeserializer implements JsonDeserializer<Match> {

    @Override
    public Match deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Estrai i campi dalla struttura JSON
        String winner = jsonObject.get("winner").getAsString();
        String loser = jsonObject.get("loser").getAsString();
        String date = jsonObject.get("date").getAsString();

        int scoreWin = jsonObject.get("scoreWinner").getAsInt();
        int scoreLos = jsonObject.get("scoreLoser").getAsInt();

        String id="";

        // Estrai il campo 'type' se esiste, altrimenti usa un valore predefinito
        String type = "PP1v1";
        if (jsonObject.has("type")) {
            type = jsonObject.get("type").getAsString();
        }
        if (jsonObject.has("id_timestamp")) {
            id = jsonObject.get("id_timestamp").getAsString();
        }
        String hour="";
        if (jsonObject.has("hour")) {
            hour = jsonObject.get("hour").getAsString();
        }
        Match m=new Match(winner, loser, date, type,scoreWin,scoreLos,hour,id);

        // Ritorna un nuovo oggetto Match
        return m;
    }
}
