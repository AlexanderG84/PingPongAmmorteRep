package AuletteBlu.pingpongammorte;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MatchActivity3 extends AppCompatActivity {

    private Spinner player1Spinner, player2Spinner, dateSpinner;
    private RadioGroup sportRadioGroup;
    private ListView matchListView;

    private List<Player> players;
    private ArrayAdapter<Match> matchAdapter;

    public static Intent newIntent(Context context, ArrayList<Player> players) {
        Intent intent = new Intent(context, MatchActivity.class);
        intent.putExtra("players", players);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_activity);

        // Recupera la lista di giocatori dall'Intent
        players = (ArrayList<Player>) getIntent().getSerializableExtra("players");

        player1Spinner = findViewById(R.id.player1_spinner);
        player2Spinner = findViewById(R.id.player2_spinner);
        dateSpinner = findViewById(R.id.date_spinner);
        //sportRadioGroup = findViewById(R.id.sport_radio_group);
        matchListView = findViewById(R.id.match_list_view);

        setupSpinners();
        setupRadioGroup();
        setupListView();

        updateMatches();
    }

    private void setupSpinners() {
        // Configura gli Spinner con la lista dei nomi dei giocatori
        ArrayAdapter<String> playerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getPlayerNames());
        playerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        player1Spinner.setAdapter(playerAdapter);
        player2Spinner.setAdapter(playerAdapter);

        AdapterView.OnItemSelectedListener playerSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMatches();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        };

        player1Spinner.setOnItemSelectedListener(playerSelectedListener);
        player2Spinner.setOnItemSelectedListener(playerSelectedListener);
    }

    private void setupRadioGroup() {
        sportRadioGroup.setOnCheckedChangeListener((group, checkedId) -> updateMatches());
    }

    private void setupListView() {
        matchAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<>());
        matchListView.setAdapter(matchAdapter);
    }

    private void updateMatches() {
        // Ottieni i giocatori selezionati, lo sport selezionato e la data selezionata
        Player player1 = players.get(player1Spinner.getSelectedItemPosition());
        Player player2 = players.get(player2Spinner.getSelectedItemPosition());
        String selectedSport = ((RadioButton) findViewById(sportRadioGroup.getCheckedRadioButtonId())).getText().toString();
        String selectedDate = dateSpinner.getSelectedItem().toString();

        // Ottieni la lista dei match tra i due giocatori per lo sport e la data selezionata
        List<Match> matches = getMatchesBetweenPlayers(player1, player2, selectedSport, selectedDate);

        // Aggiorna la ListView con la nuova lista di match
        matchAdapter.clear();
        matchAdapter.addAll(matches);
        matchAdapter.notifyDataSetChanged();
    }

    @NonNull
    private List<String> getPlayerNames() {
        List<String> playerNames = new ArrayList<>();
        for (Player player : players) {
            playerNames.add(player.getName());
        }
        return playerNames;
    }

    private List<Match> getMatchesBetweenPlayers(Player player1, Player player2, String selectedSport, String selectedDate) {
        // Implementa questa funzione per ottenere la lista dei match tra i due giocatori
        // per lo sport e la data selezionata
        return new ArrayList<>();
    }
}
