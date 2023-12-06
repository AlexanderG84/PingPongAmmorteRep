package AuletteBlu.pingpongammorte;

import static AuletteBlu.pingpongammorte.LeaderboardActivity.deepCloneList;
import static AuletteBlu.pingpongammorte.LeaderboardActivity.deepClonePlayer;
import static AuletteBlu.pingpongammorte.LeaderboardActivity.findPlayerByNameWithoutCloning;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AlboDOroActivity extends AppCompatActivity {

    private ListView listView;
    private RadioGroup radioGroupSelection, radioGroupMetric;
    private List<Player> playersOriginal; // Lista dei giocatori
    private Map<String, List<Player>> playersByDate; // Mappa dei giocatori per data
    private PlayerAdapterAlbo adapter;
    private List<Player> filteredPlayers;

    private List<PlayerWithDate> calculateAlboDOro() {
        // Mappa per tenere traccia dei giocatori e delle loro vittorie per ogni giorno
        Map<String, Player> bestPlayersPerDay = new HashMap<>();

        RadioButton selectedMetric = findViewById(radioGroupMetric.getCheckedRadioButtonId());
        boolean orderByVict = selectedMetric.getId() == R.id.radioPercentVittorie;

        filteredPlayers = deepCloneList(playersOriginal);
        updateLeaderboardBasedOnPairPlayersMode();

        for (Player player : filteredPlayers) {
            for (Match match : player.getMatches()) {
                if (!match.type.equals(MainActivity.matchType)) {
                    continue; // Salta i match che non corrispondono al tipo selezionato
                }

                String date = match.getDate();
                Player currentBest = bestPlayersPerDay.get(date);
                if (currentBest == null || player.getVictoriesOnDate(filteredPlayers,date,orderByVict) >= currentBest.getVictoriesOnDate(filteredPlayers,date,orderByVict)) {
                    if (currentBest != null &&player.getVictoriesOnDate(filteredPlayers,date,orderByVict) == currentBest.getVictoriesOnDate(filteredPlayers,date,orderByVict))
                    {
                        int playerotherType=player.getVictoriesOnDate(filteredPlayers,date,!orderByVict);
                        int playerotherType2=currentBest.getVictoriesOnDate(filteredPlayers,date,!orderByVict);

                        if(playerotherType>playerotherType2){
                            bestPlayersPerDay.put(date,deepClonePlayer (player));

                        }else if(playerotherType2>playerotherType){

                        }else {

                            long countPlayer = bestPlayersPerDay.values().stream()

                                    .filter(_player -> player.getName().equals(_player.getName()))
                                    .count();

                            long countCurrentBest = bestPlayersPerDay.values().stream()

                                    .filter(_player -> currentBest.getName().equals(_player.getName()))
                                    .count();
                            if (countPlayer <= countCurrentBest)
                                bestPlayersPerDay.put(date, deepClonePlayer(player));
                        }

                    }else
                        bestPlayersPerDay.put(date,deepClonePlayer (player));
                }
            }
        }




        List<PlayerWithDate> alboDoro = new ArrayList<>();
        for (Map.Entry<String, Player> entry : bestPlayersPerDay.entrySet()) {
            String date = entry.getKey();
            Player player = entry.getValue();
            alboDoro.add(new PlayerWithDate(player, date,0));
        }


        // Ordina la lista in base alla data in ordine decrescente (dal più recente al più vecchio)
        Collections.sort(alboDoro, new Comparator<PlayerWithDate>() {
            @Override
            public int compare(PlayerWithDate p1, PlayerWithDate p2) {
                // Converte le stringhe delle date in LocalDate per il confronto
                LocalDate date1 = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    date1 = LocalDate.parse(p1.getDate());
                }
                LocalDate date2 = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    date2 = LocalDate.parse(p2.getDate());
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return date2.compareTo(date1); // Ordine decrescente
                }
                return 0;
            }
        });

        return alboDoro;

        //return new ArrayList<>(bestPlayersPerDay.values());
    }


    private List<PlayerWithDate> calculateClassifica() {
        Map<String, Integer> bestPlayerDaysCount = new HashMap<>(); // Mappa per tenere traccia delle volte in cui ogni giocatore è stato il migliore
        RadioButton selectedMetric = findViewById(radioGroupMetric.getCheckedRadioButtonId());
        boolean orderByVict = selectedMetric.getId() == R.id.radioPercentVittorie;

        filteredPlayers = deepCloneList(playersOriginal);
        updateLeaderboardBasedOnPairPlayersMode();


        // Mappa per memorizzare il miglior giocatore di ogni giorno
        Map<String, Player> bestPlayerPerDay = new HashMap<>();

        for (Player player : filteredPlayers) {
            for (Match match : player.getMatches()) {
                if (!match.type.equals(MainActivity.matchType)) {
                    continue; // Considera solo i match del tipo corretto
                }

                String date = match.getDate();
                Player currentBest = bestPlayerPerDay.get(date);
                /*boolean isCurrentBest = (currentBest == null) ||
                        (player.getVictoriesOnDate(players,date,orderByVict) > currentBest.getVictoriesOnDate(players,date,orderByVict)); // Confronta in base al numero di vittorie

                if (isCurrentBest) {
                    bestPlayerPerDay.put(date, deepClonePlayer (player));
                }*/
                if (currentBest == null || player.getVictoriesOnDate(filteredPlayers,date,orderByVict) >= currentBest.getVictoriesOnDate(filteredPlayers,date,orderByVict)) {

                    if (currentBest != null &&player.getVictoriesOnDate(filteredPlayers,date,orderByVict) == currentBest.getVictoriesOnDate(filteredPlayers,date,orderByVict))
                    {
                        int playerotherType=player.getVictoriesOnDate(filteredPlayers,date,!orderByVict);
                        int playerotherType2=currentBest.getVictoriesOnDate(filteredPlayers,date,!orderByVict);

                        if(playerotherType>playerotherType2){
                            bestPlayerPerDay.put(date,deepClonePlayer (player));

                        }else if(playerotherType2>playerotherType){

                        }else {

                            long countPlayer = bestPlayerPerDay.values().stream()

                                    .filter(_player -> player.getName().equals(_player.getName()))
                                    .count();

                            long countCurrentBest = bestPlayerPerDay.values().stream()

                                    .filter(_player -> currentBest.getName().equals(_player.getName()))
                                    .count();
                            if (countPlayer <= countCurrentBest)
                                bestPlayerPerDay.put(date, deepClonePlayer(player));
                        }

                    }else
                        bestPlayerPerDay.put(date,deepClonePlayer (player));

            }
        }
        }

        // Calcola quante volte ciascun giocatore è stato il migliore
        for (Player bestPlayer : bestPlayerPerDay.values()) {
            String playerName = bestPlayer.getName();
            bestPlayerDaysCount.put(playerName, bestPlayerDaysCount.getOrDefault(playerName, 0) + 1);
        }

        // Converti la mappa in una lista di PlayerWithDate
        List<PlayerWithDate> classifica = bestPlayerDaysCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Ordina in base al conteggio
                .map(e -> new PlayerWithDate(findPlayerByName(e.getKey()), String.valueOf(e.getValue()), e.getValue()))
                .collect(Collectors.toList());

        return classifica;
       /*
        // Calcola quante volte ciascun giocatore è stato il migliore
        for (Player bestPlayer : bestPlayerPerDay.values()) {
            String playerName = bestPlayer.getName();
            bestPlayerDaysCount.put(playerName, bestPlayerDaysCount.getOrDefault(playerName, 0) + 1);
        }




        // Converti la mappa in una lista ordinata di giocatori
        return bestPlayerDaysCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Ordina in base al numero di volte migliore
                .map(e -> findPlayerByName(e.getKey()))
                .collect(Collectors.toList());*/
    }



    private Player findPlayerByName(String name) {
        for (Player player : filteredPlayers) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null; // Gestire il caso in cui il giocatore non è trovato
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albo_doro);

        listView = findViewById(R.id.listViewAlboDOro);
        radioGroupSelection = findViewById(R.id.radioGroupSelection);
        radioGroupMetric = findViewById(R.id.radioGroupMetric);

        playersOriginal =MainActivity.players;

        filteredPlayers = deepCloneList(playersOriginal);  // Ottieni una copia profonda della lista originale


        playersByDate = new HashMap<>();

        // Popolare le strutture dati (players e playersByDate) con i dati
        // TODO: Implementa la logica di popolamento

       /* matchAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<>());*/
        adapter = new PlayerAdapterAlbo(this,  new ArrayList<>()); // Inizialmente vuoto
        listView.setAdapter(adapter);

        setupRadioGroupListeners();

        updateListViewData();
    }

    private void setupRadioGroupListeners() {
        radioGroupSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateListViewData();
            }
        });

        radioGroupMetric.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateListViewData();
            }
        });

        CheckBox checkBoxOpzione = findViewById(R.id.checkBoxOpzione);

        // Imposta il listener per il cambio di stato del CheckBox
        checkBoxOpzione.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Il CheckBox è stato selezionato
                    pairPlayersMode=true;
                } else {
                    // Il CheckBox è stato deselezionato
                    pairPlayersMode=false;
                    Log.d("CheckBoxStatus", "CheckBox deselezionato");
                }
                updateListViewData();
            }
        });
    }

    private void updateListViewData() {
        RadioButton selectedView = findViewById(radioGroupSelection.getCheckedRadioButtonId());
        RadioButton selectedMetric = findViewById(radioGroupMetric.getCheckedRadioButtonId());

/*
        List<PlayerWithDate> displayList = new ArrayList<>();

        if (selectedView.getId() == R.id.radioAlboDOro) {
            displayList = calculateAlboDOro();
        } else if (selectedView.getId() == R.id.radioClassifica) {
            //displayList = calculateClassifica();
        }
*/

        List<PlayerWithDate> displayList = new ArrayList<>();
        if (selectedView.getId() == R.id.radioAlboDOro) {
            adapter.setIsAlboDoroView(true);
            displayList = calculateAlboDOro();
            adapter.setPlayersWithDate(displayList);
        } else if (selectedView.getId() == R.id.radioClassifica) {
            adapter.setIsAlboDoroView(false);
            displayList = calculateClassifica();

        }


        adapter.setPlayersWithDate(displayList);
        //adapter = new PlayerAdapterAlbo(this, displayList); // Inizialmente vuoto
        //listView.setAdapter(adapter);
        //listView.setAdapter(adapter);
       // adapter.setPlayers(displayList);
        adapter.notifyDataSetChanged();
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
    private void updateLeaderboardBasedOnPairPlayersMode() {



        // Creare una nuova lista di giocatori basata sul cambio di modalità
        List<Player> updatedPlayers = createIndividualPlayersList(filteredPlayers);

       // String selectedDate = (String) dateSpinner.getSelectedItem();


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

            player.getMatchesOnDateWithOpponents(opponents, "Tutte le Date", MainActivity.matchType);

            if (player.getMatchesFiltered() == 0) {
                playersToRemove.add(player); // Aggiungi il giocatore alla lista di quelli da rimuovere
            }
            // La funzione viene chiamata e l'oggetto Player aggiorna il suo score
        }

// Rimuovi i giocatori dalla lista principale
        filteredPlayers.removeAll(playersToRemove);


        RadioButton selectedMetric = findViewById(radioGroupMetric.getCheckedRadioButtonId());
        boolean orderByVict = selectedMetric.getId() == R.id.radioPercentVittorie;


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



       /* // Aggiorna la ListView con la nuova lista di giocatori
        ArrayAdapter<Player> adapter = (ArrayAdapter<Player>) leaderboardListView.getAdapter();
        adapter.clear();
        adapter.addAll(filteredPlayers);
        adapter.notifyDataSetChanged();*/
    }

    boolean pairPlayersMode=false;
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


    // Altri metodi e logica necessaria
}
