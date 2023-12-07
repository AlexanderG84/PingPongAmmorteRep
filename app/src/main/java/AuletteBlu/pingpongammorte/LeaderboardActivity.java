package AuletteBlu.pingpongammorte;



import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LeaderboardActivity extends AppCompatActivity {

    private CheckBox pairPlayersCheckbox;
    private boolean pairPlayersMode = false;
    private List<Player> originalPlayers;  // Conserva la lista originale per ripristinarla dopo il cambio di modalità


    private ListView leaderboardListView;

    private Spinner dateSpinner;
    private CheckBox player1Checkbox, player2Checkbox, player3Checkbox, player4Checkbox; // ... aggiungi per gli altri giocatori


    private List<Player> players; // Dovresti recuperare questa lista da una fonte di dati persistente
    private List<Player> filteredPlayers;  // Lista dei giocatori filtrati in base ai criteri
    private int calculateScoreBasedOnSelectedPlayers(Player player, List<String> selectedPlayers) {
        int score = 0;
        for (String victory : player.getVictoriesAgainst()) {
            if (selectedPlayers.contains(victory)) {
                score++;
            }
        }
        return score;
    }

    private void loadScoresFromPreferences() {
        FileInputStream fis = null;
        try {
            fis = openFileInput("players.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }

            // Registra il deserializzatore personalizzato e crea un'istanza di Gson
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Match.class, new MatchDeserializer());
            Gson gson = gsonBuilder.create();

            Type playerListType = new TypeToken<ArrayList<Player>>() {}.getType();
            players = gson.fromJson(sb.toString(), playerListType);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboardactivity);

        Button btnOrderVictories = findViewById(R.id.button_order_victories);
        Button btnOrderPercentage = findViewById(R.id.button_order_percentage);

        pairPlayersCheckbox = findViewById(R.id.pairPlayersCheckbox);
        pairPlayersCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pairPlayersMode = isChecked;
                if (pairPlayersMode) {
                    // Salva la lista originale prima di applicare il cambio di modalità
                    //originalPlayers = deepCloneList(filteredPlayers);
                    // Crea una nuova lista di giocatori individuali basata sul cambio di modalità
                    updateLeaderboardBasedOnPairPlayersMode();
                } else {
                    // Ripristina la lista originale
                    //players = deepCloneList(originalPlayers);
                    // Aggiorna la ListView con la lista originale
                    updateLeaderboard();
                }
            }
        });

        btnOrderVictories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderByVict=true;
                if (!pairPlayersMode)
                    updateLeaderboard();
                else updateLeaderboardBasedOnPairPlayersMode();
            }
        });

        btnOrderPercentage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderByVict=false;
                if (!pairPlayersMode)
                updateLeaderboard();
                else updateLeaderboardBasedOnPairPlayersMode();
            }
        });

        leaderboardListView = findViewById(R.id.listView_leaderboard);
        players =MainActivity.players;

        mettiSfondo();
        //players = (ArrayList<Player>) getIntent().getSerializableExtra("players");
        //loadScoresFromPreferences();

        // Supponiamo che "selectedPlayers" sia la lista dei nomi dei giocatori che hai selezionato
        // Per ora, potresti prendere tutti i giocatori come esempio
//        List<String> selectedPlayers = new ArrayList<>();
//        for (Player p : players) {
//            selectedPlayers.add(p.getName());
//        }

//        Collections.sort(players, new Comparator<Player>() {
//            @Override
//            public int compare(Player p1, Player p2) {
//                int score1 = calculateScoreBasedOnSelectedPlayers(p1, selectedPlayers);
//                int score2 = calculateScoreBasedOnSelectedPlayers(p2, selectedPlayers);
//                return Integer.compare(score2, score1);
//            }
//        });

        filteredPlayers = deepCloneList(players);  // Ottieni una copia profonda della lista originale

        //updateLeaderboard(null, null);  // Chiamata iniziale per configurare la leaderboard

      //  ArrayAdapter<Player> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredPlayers);
        PlayerAdapter adapter = new PlayerAdapter(this, R.layout.list_item_player, filteredPlayers);

        leaderboardListView.setAdapter(adapter);

        // Ordina i giocatori in base al punteggio
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                return Integer.compare(p2.getScore(), p1.getScore()); // Ordine decrescente
            }
        });

       // ArrayAdapter<Player> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, players);
        leaderboardListView.setAdapter(adapter);


        dateSpinner = findViewById(R.id.dateSpinner);
        player1Checkbox = findViewById(R.id.player1Checkbox);
        player2Checkbox = findViewById(R.id.player2Checkbox);
        player3Checkbox = findViewById(R.id.player3Checkbox);
        player4Checkbox = findViewById(R.id.player4Checkbox);
        // ... inizializza gli altri checkbox


        Set<String> distinctDatesSet = new HashSet<>();

// Itera su ogni Player nella lista filteredPlayers
        for (Player player : filteredPlayers) {
            // Itera su ogni Match per il Player corrente
            for (Match match : player.getMatches()) {
                if(match.type.equals(MainActivity.matchType))
                distinctDatesSet.add(match.getDate());
            }
        }

        List<String> datesList = new ArrayList<>(distinctDatesSet);
        Collections.sort(datesList, Collections.reverseOrder()); // Opzionale, se vuoi che le date siano in ordine
        datesList.add(0, "Tutte le Date");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, datesList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(spinnerAdapter);
        try {
            int count=dateSpinner.getAdapter().getCount();
            if (dateSpinner.getAdapter().getCount()>1)
            dateSpinner.setSelection(1);
            else dateSpinner.setSelection(0);
        } catch (Exception e) {
            dateSpinner.setSelection(0);
        }
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDate = (String) dateSpinner.getSelectedItem();

                if ("Tutte le Date".equals(selectedDate)) {
                    // Mostra e seleziona tutti i checkbox se la data selezionata è "ALL"
                    setCheckboxVisibilityAndState(player1Checkbox, true, true);
                    setCheckboxVisibilityAndState(player2Checkbox, true, true);
                    setCheckboxVisibilityAndState(player3Checkbox, true, true);
                    setCheckboxVisibilityAndState(player4Checkbox, true, true);
                } else {
                    // Controlla ciascun giocatore basandoti sul nome e mostra/nascondi e seleziona/deseleziona il checkbox appropriato
                    Player player;

                    player = findPlayerByNameWithoutCloning(players, "Talex");
                    setCheckboxVisibilityAndState(player1Checkbox, (player != null && player.playedOnDate(selectedDate)), (player != null && player.playedOnDate(selectedDate)));

                    player = findPlayerByNameWithoutCloning(players, "Pompolus");
                    setCheckboxVisibilityAndState(player2Checkbox, (player != null && player.playedOnDate(selectedDate)), (player != null && player.playedOnDate(selectedDate)));

                    player = findPlayerByNameWithoutCloning(players, "Strino");
                    setCheckboxVisibilityAndState(player3Checkbox, (player != null && player.playedOnDate(selectedDate)), (player != null && player.playedOnDate(selectedDate)));

                    player = findPlayerByNameWithoutCloning(players, "Daniele");
                    setCheckboxVisibilityAndState(player4Checkbox, (player != null && player.playedOnDate(selectedDate)), (player != null && player.playedOnDate(selectedDate)));
                }
                updateLeaderboard();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        CompoundButton.OnCheckedChangeListener checkboxListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateLeaderboard();
            }
        };

        player1Checkbox.setOnCheckedChangeListener(checkboxListener);
        player2Checkbox.setOnCheckedChangeListener(checkboxListener);
        player3Checkbox.setOnCheckedChangeListener(checkboxListener);
        player4Checkbox.setOnCheckedChangeListener(checkboxListener);

    }


    // Metodo helper per contare le vittorie di un giocatore in una specifica data
    private int countWinsOnDate(Player player, LocalDate date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return (int) player.getMatches().stream()
                    .filter(match -> match.getWinner().equals(player.getName()))
                    .filter(match -> LocalDate.parse(match.getDate()).equals(date))
                    .count();
        }
        else return 0;
    }

    public void mettiSfondo(){

            String playerName=MainActivity.playerName;

            String imageFileName = playerName.toLowerCase() + "win"; // Prova prima con 'win'

            int imageResourceId = getResources().getIdentifier(imageFileName, "drawable", getPackageName());
            if (imageResourceId == 0) { // Se l'immagine 'win' non esiste
                imageFileName = playerName.toLowerCase() ; // Prova con il nome normale
                imageResourceId = getResources().getIdentifier(imageFileName, "drawable", getPackageName());
                if (imageResourceId == 0) { // Se neanche l'immagine normale esiste
                    imageResourceId = R.drawable.default_player_image; // Usa l'immagine di default
                }
            }

            // Imposta l'immagine come sfondo nel thread UI
            int finalImageResourceId = imageResourceId;
            runOnUiThread(() -> {
                LinearLayout mainLayout = findViewById(R.id.layout_spinner2);
                Drawable originalDrawable = ContextCompat.getDrawable(LeaderboardActivity.this, finalImageResourceId);

                // Crea una copia del Drawable originale
                Drawable playerImage = originalDrawable.getConstantState().newDrawable().mutate();


                playerImage.setAlpha(80); // Sostituisci con il valore di alpha desiderato

                mainLayout.setBackground(playerImage);
            });


    }

    private void updateLeaderboardBasedOnPairPlayersMode() {



        // Creare una nuova lista di giocatori basata sul cambio di modalità
        List<Player> updatedPlayers = createIndividualPlayersList(filteredPlayers);

        String selectedDate = (String) dateSpinner.getSelectedItem();


        filteredPlayers = deepCloneList(updatedPlayers);

        filterPlayersByMatchType(MainActivity.matchType);
        // Creare una lista filtrata basata sui checkbox selezionati
        // filteredPlayers = new ArrayList<>();
        // Ora puoi controllare i tuoi checkbox in questo modo:

        // ... ripeti per gli altri checkbox e giocatori


        //verifica qui cè la questione degli oppositori

        // Se non è selezionato "ALL", ulteriormente filtra in base alla data selezionata
//        if (!"ALL".equals(selectedDate)) {
//            filteredPlayers = filteredPlayers.stream()
//                    .filter(player -> player.getVictoryDates().contains(selectedDate))
//                    .collect(Collectors.toList());
//        }

        List<Player> playersToRemove = new ArrayList<>();

        for (Player player : filteredPlayers) {
            List<String> opponents = filteredPlayers.stream()
                    .map(Player::getName)
                    .filter(opponentName -> !opponentName.equals(player.getName()))
                    .collect(Collectors.toList());

            player.getMatchesOnDateWithOpponents(opponents, selectedDate, MainActivity.matchType);

            if (player.getMatchesFiltered() == 0) {
                playersToRemove.add(player); // Aggiungi il giocatore alla lista di quelli da rimuovere
            }
            // La funzione viene chiamata e l'oggetto Player aggiorna il suo score
        }

// Rimuovi i giocatori dalla lista principale
        filteredPlayers.removeAll(playersToRemove);




        if(orderByVict)
            // Ora puoi ordinare la lista filtrata in base al punteggio o qualsiasi altro criterio
            Collections.sort(filteredPlayers, new Comparator<Player>() {
                @Override
                public int compare(Player p1, Player p2) {
                    return Integer.compare(p2.getScore(), p1.getScore());  // Ordine decrescente
                }
            });
        else
            Collections.sort(filteredPlayers, new Comparator<Player>() {
                @Override
                public int compare(Player p1, Player p2) {
                    return Long.compare(p2.victoryPercentage, p1.victoryPercentage);  // Ordine decrescente
                }
            });



        // Aggiorna la ListView con la nuova lista di giocatori
        ArrayAdapter<Player> adapter = (ArrayAdapter<Player>) leaderboardListView.getAdapter();
        adapter.clear();
        adapter.addAll(filteredPlayers);
        adapter.notifyDataSetChanged();
    }

    private List<Player> createIndividualPlayersList(List<Player> originalPlayers) {
        List<Player> individualPlayers = new ArrayList<>();

        for (Player originalPlayer : originalPlayers) {
            if (pairPlayersMode && originalPlayer.getName().contains("-")) {
                // Se siamo in modalità coppie e il nome contiene "-", scomponi i giocatori
                String[] playerNames = originalPlayer.getName().split(" - ");
                for (String playerName : playerNames) {
                    // Controlla se il giocatore singolo è già stato aggiunto agli individualPlayers
                    Player existingIndividualPlayer = findPlayerByNameWithoutCloning(individualPlayers, playerName);
                    if (existingIndividualPlayer != null) {
                        // Aggiorna i dati del giocatore esistente in base ai match della coppia
                        updateIndividualPlayerData(existingIndividualPlayer, originalPlayer.getMatches());
                    } else {
                        // Se il giocatore singolo non è presente, crea un nuovo giocatore individuale
                        Player individualPlayer = new Player(playerName, 0);  // Inizializza il giocatore singolo con punteggio 0
                        // Aggiungi il nuovo giocatore alla lista degli individuali
                        individualPlayers.add(individualPlayer);
                        // Aggiorna i dati del nuovo giocatore in base ai match della coppia
                        updateIndividualPlayerData(individualPlayer, originalPlayer.getMatches());
                    }
                }
            } else {
                // Altrimenti, aggiungi il giocatore originale alla lista
                individualPlayers.add(originalPlayer);
            }
        }

        return individualPlayers;
    }

    private void updateIndividualPlayerData(Player individualPlayer, List<Match> matches) {
        for (Match originalMatch : matches) {
            if (originalMatch.getWinner().contains("-")) {
                // Se il vincitore è una coppia, crea due nuovi match con vincitori singoli
                String[] winners = originalMatch.getWinner().split(" - ");
                for (String winner : winners) {

                    if (winner.equals(individualPlayer.getName())) {
                        Match individualMatch = new Match(winner, originalMatch.getLoser().split(" - ")[0], originalMatch.getDate(),
                                originalMatch.type, originalMatch.scoreWinner, originalMatch.scoreLoser, originalMatch.getHour(), originalMatch.id_timestamp);
                        individualPlayer.addMatch(individualMatch);
                        individualPlayer.setScore(individualPlayer.getScore() + 1);
                        individualPlayer.score+=1;
                    }
                }

                // Se il vincitore è una coppia, crea due nuovi match con vincitori singoli
                String[] losers = originalMatch.getLoser().split(" - ");
                for (String loser : losers) {
                    if (loser.equals(individualPlayer.getName())) {
                        Match individualMatch = new Match(originalMatch.getWinner().split(" - ")[0], loser, originalMatch.getDate(),
                                originalMatch.type, originalMatch.scoreWinner, originalMatch.scoreLoser, originalMatch.getHour(), originalMatch.id_timestamp);
                        individualPlayer.addMatch(individualMatch);
                        individualPlayer.score+=1;
                    }
                }

            } else {
                // Se il vincitore è già un singolo giocatore, crea un nuovo match identico
                Match individualMatch = new Match(originalMatch.getWinner(), originalMatch.getLoser(),
                        originalMatch.getDate(), originalMatch.type, originalMatch.scoreWinner,
                        originalMatch.scoreLoser, originalMatch.getHour(),originalMatch.id_timestamp);
                individualPlayer.addMatch(individualMatch);
                individualPlayer.setScore(individualPlayer.getScore()+1);
                individualPlayer.score+=1;
            }
        }
    }



    static boolean orderByVict=true;
    //static String typeMatch="PP1v1";
    static public List<Player> deepCloneList(List<Player> original) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(original);
        Type playerListType = new TypeToken<ArrayList<Player>>() {}.getType();
        return gson.fromJson(jsonString, playerListType);
    }

    static public Player deepClonePlayer(Player original) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(original);
        Type playerListType = new TypeToken<Player>() {}.getType();
        return gson.fromJson(jsonString, playerListType);
    }

    // E che tu abbia una funzione per cercare un giocatore basato sul nome:
    private Player findPlayerByName(List<Player> players, String name) {
        for (Player player : players) {
            if (player.getName().equals(name)) {
                return player.deepClone();
            }
        }
        return null;  // oppure potresti lanciare un'eccezione se preferisci.
    }

    static public Player findPlayerByNameWithoutCloning(List<Player> players, String name) {
        for (Player player : players) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null;  // oppure potresti lanciare un'eccezione se preferisci.
    }
    private void removePlayerByName(List<Player> playerList, String playerName) {
        Iterator<Player> iterator = playerList.iterator();

        while (iterator.hasNext()) {
            Player currentPlayer = iterator.next();
            if (currentPlayer.getName().equals(playerName)) {
                iterator.remove();
                break;
            }
        }
    }

    private void filterPlayersByMatchType(String typeMatch) {
        Iterator<Player> iterator = filteredPlayers.iterator();
        while (iterator.hasNext()) {
            Player currentPlayer = iterator.next();
            if (!hasMatchOfType(currentPlayer, typeMatch)) {
                iterator.remove();
            }
        }
    }

    private boolean hasMatchOfType(Player player, String typeMatch) {
        for (Match match : player.getMatches()) {
            if (match.type.equals(typeMatch)) {
                return true;
            }
        }
        return false;
    }

    private void updateLeaderboard() {

        if(pairPlayersMode) {
            pairPlayersCheckbox.setChecked(false);
            return;
        }
        String selectedDate = (String) dateSpinner.getSelectedItem();


        filteredPlayers = deepCloneList(players);

        filterPlayersByMatchType(MainActivity.matchType);
        // Creare una lista filtrata basata sui checkbox selezionati
        // filteredPlayers = new ArrayList<>();
        // Ora puoi controllare i tuoi checkbox in questo modo:
        if (!player1Checkbox.isChecked()) {
            removePlayerByName(filteredPlayers, "Talex");
        }

        if (!player2Checkbox.isChecked()) {
            removePlayerByName(filteredPlayers, "Pompolus");
        }

        if (!player3Checkbox.isChecked()) {
            removePlayerByName(filteredPlayers, "Strino");
        }

        if (!player4Checkbox.isChecked()) {
            removePlayerByName(filteredPlayers, "Daniele");
        }

        // ... ripeti per gli altri checkbox e giocatori


        //verifica qui cè la questione degli oppositori

        // Se non è selezionato "ALL", ulteriormente filtra in base alla data selezionata
//        if (!"ALL".equals(selectedDate)) {
//            filteredPlayers = filteredPlayers.stream()
//                    .filter(player -> player.getVictoryDates().contains(selectedDate))
//                    .collect(Collectors.toList());
//        }

        List<Player> playersToRemove = new ArrayList<>();

        for (Player player : filteredPlayers) {
            List<String> opponents = filteredPlayers.stream()
                    .map(Player::getName)
                    .filter(opponentName -> !opponentName.equals(player.getName()))
                    .collect(Collectors.toList());

            player.getMatchesOnDateWithOpponents(opponents, selectedDate, MainActivity.matchType);

            if (player.getMatchesFiltered() == 0) {
                playersToRemove.add(player); // Aggiungi il giocatore alla lista di quelli da rimuovere
            }
            // La funzione viene chiamata e l'oggetto Player aggiorna il suo score
        }

// Rimuovi i giocatori dalla lista principale
        filteredPlayers.removeAll(playersToRemove);




        if(orderByVict)
        // Ora puoi ordinare la lista filtrata in base al punteggio o qualsiasi altro criterio
        Collections.sort(filteredPlayers, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                return Integer.compare(p2.getScore(), p1.getScore());  // Ordine decrescente
            }
        });
        else
            Collections.sort(filteredPlayers, new Comparator<Player>() {
                @Override
                public int compare(Player p1, Player p2) {
                    return Long.compare(p2.victoryPercentage, p1.victoryPercentage);  // Ordine decrescente
                }
            });

        // Aggiorna la ListView con la nuova lista filtrata
        // Assumendo che tu abbia un ArrayAdapter per la tua ListView:
        ArrayAdapter<Player> adapter = (ArrayAdapter<Player>) leaderboardListView.getAdapter();
        adapter.clear();
        adapter.addAll(filteredPlayers);
        adapter.notifyDataSetChanged();
    }

    // Funzione helper per impostare visibilità e stato di un checkbox
    private void setCheckboxVisibilityAndState(CheckBox checkbox, boolean visible, boolean checked) {
        if (visible) {
            checkbox.setVisibility(View.VISIBLE);
            checkbox.setChecked(checked);
        } else {
            checkbox.setVisibility(View.GONE);
            checkbox.setChecked(false);  // sempre deselezionato se invisibile
        }
    }

}
