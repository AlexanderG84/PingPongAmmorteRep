package AuletteBlu.pingpongammorte;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AlboDOroActivity extends AppCompatActivity {

    private ListView listView;
    private RadioGroup radioGroupSelection, radioGroupMetric;
    private List<Player> players; // Lista dei giocatori
    private Map<String, List<Player>> playersByDate; // Mappa dei giocatori per data
    private PlayerAdapterAlbo adapter;


    private List<Player> calculateAlboDOro() {
        // Mappa per tenere traccia dei giocatori e delle loro vittorie per ogni giorno
        Map<String, Player> bestPlayersPerDay = new HashMap<>();

        RadioButton selectedMetric = findViewById(radioGroupMetric.getCheckedRadioButtonId());
        boolean orderByVict = selectedMetric.getId() == R.id.radioPercentVittorie;


        for (Player player : players) {
            for (Match match : player.getMatches()) {
                if (!match.type.equals(MainActivity.matchType)) {
                    continue; // Salta i match che non corrispondono al tipo selezionato
                }

                String date = match.getDate();
                Player currentBest = bestPlayersPerDay.get(date);
                if (currentBest == null || player.getVictoriesOnDate(players,date,orderByVict) > currentBest.getVictoriesOnDate(players,date,orderByVict)) {
                    bestPlayersPerDay.put(date, player);
                }
            }
        }

        return new ArrayList<>(bestPlayersPerDay.values());
    }


    private List<Player> calculateClassifica() {
        Map<String, Integer> bestPlayerDaysCount = new HashMap<>(); // Mappa per tenere traccia delle volte in cui ogni giocatore è stato il migliore
        RadioButton selectedMetric = findViewById(radioGroupMetric.getCheckedRadioButtonId());
        boolean orderByVict = selectedMetric.getId() == R.id.radioPercentVittorie;


        // Mappa per memorizzare il miglior giocatore di ogni giorno
        Map<String, Player> bestPlayerPerDay = new HashMap<>();

        for (Player player : players) {
            for (Match match : player.getMatches()) {
                if (!match.type.equals(MainActivity.matchType)) {
                    continue; // Considera solo i match del tipo corretto
                }

                String date = match.getDate();
                Player currentBest = bestPlayerPerDay.get(date);
                boolean isCurrentBest = (currentBest == null) ||
                        (player.getVictoriesOnDate(players,date,orderByVict) > currentBest.getVictoriesOnDate(players,date,orderByVict)); // Confronta in base al numero di vittorie

                if (isCurrentBest) {
                    bestPlayerPerDay.put(date, player);
                }
            }
        }

        // Calcola quante volte ciascun giocatore è stato il migliore
        for (Player bestPlayer : bestPlayerPerDay.values()) {
            String playerName = bestPlayer.getName();
            bestPlayerDaysCount.put(playerName, bestPlayerDaysCount.getOrDefault(playerName, 0) + 1);
        }

        // Converti la mappa in una lista ordinata di giocatori
        return bestPlayerDaysCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Ordina in base al numero di volte migliore
                .map(e -> findPlayerByName(e.getKey()))
                .collect(Collectors.toList());
    }


    private List<Player> _calculateClassifica() {
        Map<String, Integer> winsPerPlayer = new HashMap<>();

        for (Player player : players) {
            for (String date : player.getVictoryDates()) {
                winsPerPlayer.put(player.getName(), winsPerPlayer.getOrDefault(player.getName(), 0) + 1);
            }
        }

        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(winsPerPlayer.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue())); // Ordina in base al numero di vittorie

        List<Player> sortedPlayers = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            sortedPlayers.add(findPlayerByName(entry.getKey()));
        }

        return sortedPlayers;
    }

    private Player findPlayerByName(String name) {
        for (Player player : players) {
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

        players =MainActivity.players;
        playersByDate = new HashMap<>();

        // Popolare le strutture dati (players e playersByDate) con i dati
        // TODO: Implementa la logica di popolamento

       /* matchAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<>());*/
        adapter = new PlayerAdapterAlbo(this,  new ArrayList<>()); // Inizialmente vuoto
        listView.setAdapter(adapter);

        setupRadioGroupListeners();
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
    }

    private void updateListViewData() {
        RadioButton selectedView = findViewById(radioGroupSelection.getCheckedRadioButtonId());
        RadioButton selectedMetric = findViewById(radioGroupMetric.getCheckedRadioButtonId());

        List<Player> displayList = new ArrayList<>();

        if (selectedView.getId() == R.id.radioAlboDOro) {
            displayList = calculateAlboDOro();
        } else if (selectedView.getId() == R.id.radioClassifica) {
            displayList = calculateClassifica();
        }

        //adapter = new PlayerAdapterAlbo(this, displayList); // Inizialmente vuoto
        //listView.setAdapter(adapter);
        //listView.setAdapter(adapter);
        adapter.setPlayers(displayList);
        adapter.notifyDataSetChanged();
    }

    // Altri metodi e logica necessaria
}
