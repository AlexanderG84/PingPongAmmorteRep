package AuletteBlu.pingpongammorte;

import static AuletteBlu.pingpongammorte.MainActivity.driveInteraction;
import static AuletteBlu.pingpongammorte.MatchUtils.distinctMatch;
import static AuletteBlu.pingpongammorte.MatchUtils.removeMatchesWithId;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import AuletteBlu.pingpongammorte.utils.DriveInteraction;

public class MatchActivity extends AppCompatActivity  {

    private Spinner player1Spinner, player2Spinner, dateSpinner;
    private RadioGroup sportRadioGroup;
    private ListView matchListView;
    private CheckBox checkBoxPipponi;

    private List<Player> players;
    private ArrayAdapter<Match> matchAdapter;

    boolean pipponi=false;


    private boolean saveScoresToPreferences() {
        Gson gson = new Gson();
        String playersJson = gson.toJson(players);
        boolean ack=false;
        try {
            //driveInteraction.readFirebaseData();
            ack=driveInteraction.uploadText(playersJson,"write");
        } catch (Exception e) {
            e.printStackTrace();
        }

        FileOutputStream fos = null;
        try {
            fos = openFileOutput("players.json", MODE_PRIVATE);
            fos.write(playersJson.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
return ack;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu); // context_menu è il file xml che definisce gli elementi del menu
    }

    // Ovverride del metodo onContextItemSelected
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //String g=item.getItemId();
        int itemId = item.getItemId();
        if (itemId == R.id.delete_match) {
            deleteMatch(info.position);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }


    }

    // Metodo per cancellare la partita
    private void deleteMatch(int position) {

        checkLastModified(position);
        if (true)
            return;

        // Supponiamo che 'matchAdapter' sia un campo nell'Activity che detiene il tuo ArrayAdapter
        Match matchToDelete = matchAdapter.getItem(position);

        // Rimuovere il match dalla lista dei matches di entrambi i player

        Player winner = players.stream()
                .filter(player -> player.getName().equals(matchToDelete.getWinner()))
                .findFirst()
                .orElse(null); // o qualsiasi valore di default tu desideri

        Player loser = players.stream()
                .filter(player -> player.getName().equals(matchToDelete.getLoser()))
                .findFirst()
                .orElse(null); // o qualsiasi valore di default tu desideri




        if (winner != null) {
            winner.getMatches().remove(matchToDelete);
           winner.setMatches(removeMatchesWithId(winner.getMatches(),matchToDelete.id_timestamp));
        }

        if (loser != null) {
            loser.getMatches().remove(matchToDelete);
            loser.setMatches(removeMatchesWithId(loser.getMatches(),matchToDelete.id_timestamp));

        }

        // Rimuovere il match dall'adapter e aggiornare la ListView
        matchAdapter.remove(matchToDelete);
        matchAdapter.notifyDataSetChanged();


        updatePlayersSpinner();

        // Qui puoi chiamare il tuo metodo updateMatch() se necessario
        updateMatches(); // Questo metodo dovrebbe essere definito da te, in base a quello che deve fare
        boolean ack=saveScoresToPreferences();



        if(!ack){
            Toast.makeText(MatchActivity.this, "CANCELLAZIONE FALLITA: DATI INCONSISTENTI. Chiudere e riaprire l'app", Toast.LENGTH_LONG).show();
            //coloraSfondo(false,"","");
            return;

        }
    }


    private void checkLastModified(int position) {
        DatabaseReference lastModifiedDbRef =driveInteraction.databaseRef.child("lastModified");
        lastModifiedDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long lastModified = dataSnapshot.getValue(Long.class);
                handleLastModifiedResult(lastModified,position);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Gestire l'errore
                handleLastModifiedResult(null,position);
            }
        });
    }


    private void handleLastModifiedResult(Long lastModified, int position) {
        if (lastModified != null&&lastModified==driveInteraction.LastModificatedPlayers) {

            Match matchToDelete = matchAdapter.getItem(position);

            // Rimuovere il match dalla lista dei matches di entrambi i player

            Player winner = players.stream()
                    .filter(player -> player.getName().equals(matchToDelete.getWinner()))
                    .findFirst()
                    .orElse(null); // o qualsiasi valore di default tu desideri

            Player loser = players.stream()
                    .filter(player -> player.getName().equals(matchToDelete.getLoser()))
                    .findFirst()
                    .orElse(null); // o qualsiasi valore di default tu desideri




            if (winner != null) {
                winner.getMatches().remove(matchToDelete);
                winner.setMatches(removeMatchesWithId(winner.getMatches(),matchToDelete.id_timestamp));
            }

            if (loser != null) {
                loser.getMatches().remove(matchToDelete);
                loser.setMatches(removeMatchesWithId(loser.getMatches(),matchToDelete.id_timestamp));

            }

            // Rimuovere il match dall'adapter e aggiornare la ListView
            matchAdapter.remove(matchToDelete);
            matchAdapter.notifyDataSetChanged();


            updatePlayersSpinner();

            // Qui puoi chiamare il tuo metodo updateMatch() se necessario
            updateMatches(); // Questo metodo dovrebbe essere definito da te, in base a quello che deve fare
            boolean ack=saveScoresToPreferences();



            if(!ack){
                Toast.makeText(MatchActivity.this, "CANCELLAZIONE FALLITA: DATI INCONSISTENTI. Chiudere e riaprire l'app", Toast.LENGTH_LONG).show();
                //coloraSfondo(false,"","");
                return;

            }


        } else {
            Toast.makeText(MatchActivity.this, "CANCELLAZIONE FALLITA: DATI INCONSISTENTI. Chiudere e riaprire l'app", Toast.LENGTH_LONG).show();

            // Gestisci il caso in cui non sia stato possibile ottenere lastModified
            // Puoi mostrare un messaggio di errore o gestire la situazione in modo appropriato
        }
    }


    private void _saveScoresToPreferences() {
        Gson gson = new Gson();
        String playersJson = gson.toJson(players);

        FileOutputStream fos = null;
        try {
            fos = openFileOutput("players.json", MODE_PRIVATE);
            fos.write(playersJson.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private BroadcastReceiver databaseUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Controlla se l'intento ricevuto è quello per l'aggiornamento del database
            if ("com.example.ACTION_DATABASE_UPDATED".equals(intent.getAction())) {
                // Chiamare il metodo update()
                players=MainActivity.players;
                updateMatches();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_activity);

        IntentFilter filter = new IntentFilter("com.example.ACTION_DATABASE_UPDATED");
        registerReceiver(databaseUpdateReceiver, filter);

        // Recupera la lista di giocatori dall'Intent
        //players = (ArrayList<Player>) getIntent().getSerializableExtra("players");
        players=MainActivity.players;
        mettiSfondo();
        player1Spinner = findViewById(R.id.player1_spinner);
        player2Spinner = findViewById(R.id.player2_spinner);
        dateSpinner = findViewById(R.id.date_spinner);
        //sportRadioGroup = findViewById(R.id.sport_radio_group);
        matchListView = findViewById(R.id.match_list_view);

        checkBoxPipponi = findViewById(R.id.checkBoxPipponi);
/*

        driveInteraction.setUpdateListener(this);
        driveInteraction.startListeningToLastModified();
*/

        checkBoxPipponi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Quando il CheckBox viene selezionato o deselezionato
                // Imposta la variabile "pippone" a true se isChecked è true, altrimenti a false
                pipponi = isChecked;
                updateMatches();
            }
        });


        registerForContextMenu(matchListView);

        setupSpinners();
        //setupRadioGroup();
        setupListView();

        updatePlayersSpinner();

        updateMatches();
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
            LinearLayout mainLayout = findViewById(R.id.layout_spinner3);
            Drawable originalDrawable = ContextCompat.getDrawable(MatchActivity.this, finalImageResourceId);

            // Crea una copia del Drawable originale
            Drawable playerImage = originalDrawable.getConstantState().newDrawable().mutate();


            playerImage.setAlpha(80); // Sostituisci con il valore di alpha desiderato

            mainLayout.setBackground(playerImage);
        });


    }


    private void setupSpinners() {
        // Configura gli Spinner con la lista dei nomi dei giocatori che hanno match di tipo "pp"
        List<Player> playersWithPPMatches = new ArrayList<>();
        for (Player player : players) {

            for (Match match : player.getMatches()) {
                if (MainActivity.matchType.equalsIgnoreCase(match.type)) {
                    //playersWithPPMatches.add(player);
                    if(playersWithPPMatches.stream()
                            .filter(playert -> playert.getName().equals(match.getWinner()))
                            .findFirst()
                            .orElse(null) ==null)

                    playersWithPPMatches.add(players.stream()
                            .filter(playert -> playert.getName().equals(match.getWinner()))
                            .findFirst()
                            .orElse(null));


                    if(playersWithPPMatches.stream()
                            .filter(playert -> playert.getName().equals(match.getLoser()))
                            .findFirst()
                            .orElse(null) ==null)
                    playersWithPPMatches.add(players.stream()
                            .filter(playert -> playert.getName().equals(match.getLoser()))
                            .findFirst()
                            .orElse(null));
                    // Trovato un match con type "pp", quindi non c'è bisogno di controllare ulteriormente.
                }
            }
        }




        // Inizializza l'adapter per player1Spinner con i giocatori filtrati
        ArrayAdapter<Player> player1Adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, playersWithPPMatches);
        player1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Player tuttiPlayer = new Player("Tutti", 0); // Assicurati che ci sia un costruttore adeguato nella classe Player
        if (!playersWithPPMatches.stream().anyMatch(player -> player.getName().equals("Tutti")))

            playersWithPPMatches.add(0, tuttiPlayer);
        player1Adapter.notifyDataSetChanged(); // Notifica l'adapter del cambiamento

        player1Spinner.setAdapter(player1Adapter);

        // Imposta il listener per player1Spinner
        player1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePlayersSpinner();
                // Ottiene il giocatore selezionato in player1Spinner
                Player selectedPlayer1 = (Player) parent.getItemAtPosition(position);
                // Aggiorna player2Spinner escludendo il giocatore selezionato in player1Spinner
                updatePlayer2Spinner(selectedPlayer1, playersWithPPMatches);
                // Aggiorna la ListView dei match
                updateMatches();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Non fare nulla
            }
        });

        // Si inizializza player2Spinner con gli stessi giocatori per ora, sarà aggiornato dinamicamente quando si seleziona un giocatore in player1Spinner
        ArrayAdapter<Player> player2Adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, playersWithPPMatches);
        player2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        player2Adapter.notifyDataSetChanged(); // Notifica l'adapter del cambiamento

        player2Spinner.setAdapter(player2Adapter);

        // Imposta il listener per player2Spinner
        player2Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePlayersSpinner();
                // Aggiorna la ListView dei match quando viene selezionato un nuovo giocatore in player2Spinner
                updateMatches();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Non fare nulla
            }
        });



        // Imposta il listener per dateSpinner come prima
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMatches();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Non fare nulla qui
            }
        });
    }

    private void updatePlayer2Spinner(Player player1, List<Player> allPlayers) {
        List<Player> eligiblePlayersForPlayer2 = new ArrayList<>();

        if (player1.getName().equals("Tutti")) {
            // Se player1 è impostato su "Tutti", considera tutti i giocatori come eleggibili
            eligiblePlayersForPlayer2.addAll(allPlayers);
        } else {
            for (Match match : player1.getMatches()) {
                if (MainActivity.matchType.equalsIgnoreCase(match.type)) {
                    String opponentName = match.getOpponent(player1.getName());
                    // Cerca l'oggetto Player corrispondente a opponentName
                    for (Player player : allPlayers) {
                        if (player.getName().equals(opponentName) && !eligiblePlayersForPlayer2.contains(player)) {
                            eligiblePlayersForPlayer2.add(player);
                            break;
                        }
                    }
                }
            }
        }

        if (!eligiblePlayersForPlayer2.stream().anyMatch(player -> player.getName().equals("Tutti")))

            // Aggiungi "Tutti" come primo elemento
        eligiblePlayersForPlayer2.add(0, new Player("Tutti", 0));

        ArrayAdapter<Player> player2Adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, eligiblePlayersForPlayer2);
        player2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        player2Spinner.setAdapter(player2Adapter);
    }





    private void setupRadioGroup() {
        sportRadioGroup.setOnCheckedChangeListener((group, checkedId) -> updateMatches());
    }

    private void setupListView() {
        matchAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<>());
        matchListView.setAdapter(matchAdapter);
    }

    public void updateDateSpinner(Player player1, Player player2, Spinner dateSpinner) {
        List<String> uniqueDates = new ArrayList<>();
        uniqueDates.add("Tutte le Date");
        String selectedDate = (String) dateSpinner.getSelectedItem();

        if (!player1.getName().equals("Tutti") && !player2.getName().equals("Tutti")) {
            // Entrambi i giocatori sono specifici, quindi aggiungi solo le date in cui hanno giocato tra loro
            for (Match match : player1.getMatches()) {
                if (match.getOpponent(player1.getName()).equals(player2.getName())) {
                    String matchDate = match.getDate();
                    if (!uniqueDates.contains(matchDate)) {
                        uniqueDates.add(matchDate);
                    }
                }
            }

            for (Match match : player2.getMatches()) {
                if (match.getOpponent(player2.getName()).equals(player1.getName())) {
                    String matchDate = match.getDate();
                    if (!uniqueDates.contains(matchDate)) {
                        uniqueDates.add(matchDate);
                    }
                }
            }
        } else {
            // Almeno uno dei giocatori è "Tutti", quindi aggiungi tutte le date in cui c'è almeno un match con il giocatore specificato nell'altro spinner
            String specificPlayerName;
            List<String> specificPlayerNames;

            if (player1.getName().equals("Tutti") && player2.getName().equals("Tutti")) {
                // Entrambi i giocatori sono "Tutti", quindi considera tutte le date di tutti i match
                specificPlayerName = null;  // Non specificare un giocatore specifico
                specificPlayerNames = getPlayerNames();  // Tutti i giocatori
            } else {
                // Solo uno dei giocatori è "Tutti", quindi considera le date di tutti i match con l'altro giocatore specificato
                specificPlayerName = player1.getName().equals("Tutti") ? player2.getName() : player1.getName();
                specificPlayerNames = Collections.singletonList(specificPlayerName);
            }

            for (Player player : players) {
                if (specificPlayerName != null && player.getName().equals(specificPlayerName)) {
                    continue;  // Salta il giocatore specificato, poiché stiamo cercando match con altri giocatori
                }

                for (Match match : player.getMatchesOnDateWithOpponents(specificPlayerNames, "Tutte le Date", MainActivity.matchType)) {
                    String matchDate = match.getDate();
                    if (!uniqueDates.contains(matchDate)) {
                        uniqueDates.add(matchDate);
                    }
                }
            }
        }

        // Ordina le date in ordine decrescente
        Collections.sort(uniqueDates.subList(1, uniqueDates.size()), Collections.reverseOrder());

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, uniqueDates);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dateAdapter);

        int selectedIndex = uniqueDates.indexOf(selectedDate);
        if (selectedIndex != -1) {
            // La data salvata è presente tra le nuove date, quindi selezionala
            dateSpinner.setSelection(selectedIndex);
        } else {
            // La data salvata non è presente, seleziona la prima data disponibile

            try {
                int count=dateSpinner.getAdapter().getCount();
                if (dateSpinner.getAdapter().getCount()>1)
                dateSpinner.setSelection(1);
                else  dateSpinner.setSelection(0);
            } catch (Exception e) {
                dateSpinner.setSelection(0);
            }
        }

    }



    // Supponendo che tu abbia un metodo come questo per aggiornare gli spinner dei giocatori
    public void updatePlayersSpinner() {
        // ... altro codice ...

        //Spinner dateSpinner = findViewById(R.id.dateSpinner);
        //Spinner player1Spinner = findViewById(R.id.player1Spinner);
        //Spinner player2Spinner = findViewById(R.id.player2Spinner);

        Player player1 = (Player) player1Spinner.getSelectedItem();
        Player player2 = (Player) player2Spinner.getSelectedItem();

        updateDateSpinner(player1, player2, dateSpinner);
    }



    // Metodo ausiliario per verificare se un'ora è compresa tra mezzanotte e le 3

    private void displayMatches(List<Match> matches, Player player1, Player player2) {

        if(!pipponi) {
            // Ordina la lista dei match per data
            Collections.sort(matches, new Comparator<Match>() {
                @Override
                public int compare(Match match1, Match match2) {
                    // Confronta le date dei due match per l'ordinamento
                    LocalDate date1 = LocalDate.parse(match1.getDate());
                    LocalDate date2 = LocalDate.parse(match2.getDate());

                    int result = date2.compareTo(date1); // Ordine decrescente per data (più recenti prima)

                    // Se le date sono uguali, confronta per ora
                    if (result == 0) {
                        String hour1 = match1.getHour();
                        String hour2 = match2.getHour();

                        // Gestisci il caso in cui una delle ore è una stringa vuota
                        if (hour1.isEmpty() && hour2.isEmpty()) {
                            return 0; // Le stringhe vuote sono considerate uguali
                        } else if (hour1.isEmpty()) {
                            return 1; // La stringa vuota viene considerata maggiore (posta alla fine)
                        } else if (hour2.isEmpty()) {
                            return -1; // La stringa vuota viene considerata maggiore (posta alla fine)
                        }

                        // Confronta le ore solo se entrambe non sono vuote
                        LocalTime time1 = LocalTime.parse(hour1);
                        LocalTime time2 = LocalTime.parse(hour2);

                        // Aggiungi logica per gestire le ore tra mezzanotte e le 3
                        if (isBetweenMidnightAnd3(time1) && isBetweenMidnightAnd3(time2)) {
                            // Se entrambe le ore sono tra mezzanotte e le 3, confronta normalmente
                            result = time2.compareTo(time1); // Ordine decrescente per ora (più recenti prima)
                        } else if (isBetweenMidnightAnd3(time1)) {
                            // Se solo l'ora di match1 è tra mezzanotte e le 3, posizionalo prima
                            result = -1;
                        } else if (isBetweenMidnightAnd3(time2)) {
                            // Se solo l'ora di match2 è tra mezzanotte e le 3, posizionalo prima
                            result = 1;
                        } else {
                            // Altrimenti, confronta normalmente
                            result = time2.compareTo(time1); // Ordine decrescente per ora (più recenti prima)
                        }
                    }

                    return result;
                }

                // Metodo ausiliario per verificare se un'ora è compresa tra mezzanotte e le 3
                private boolean isBetweenMidnightAnd3(LocalTime time) {
                    return !time.isBefore(LocalTime.MIDNIGHT) && time.isBefore(LocalTime.of(3, 0));
                }
            });
        }else {

            Collections.sort(matches, new Comparator<Match>() {
                @Override
                public int compare(Match match1, Match match2) {

                    double ratio1 = (double) (match1.scoreWinner - match1.scoreLoser) / match1.scoreWinner;
                    double ratio2 = (double) (match2.scoreWinner - match2.scoreLoser) / match2.scoreWinner;

                    if (ratio1 > ratio2) {
                        return -1;
                    } else if (ratio2 > ratio1) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

        }

        if(player1==null||player2==null)
        {

        }
        MatchUtils.VictoryCount victoryCount = MatchUtils.countVictories(matches, player1.getName(), player2.getName());

        int victoriesPlayer1 = victoryCount.victoriesPlayer1;
        int victoriesPlayer2 = victoryCount.victoriesPlayer2;

        TextView pl1Score = findViewById(R.id.player1Wins);
        TextView pl2Score = findViewById(R.id.player2Wins);

        pl1Score.setText(String.format("Vittorie %s: %d", player1, victoriesPlayer1));
        pl2Score.setText(String.format("Vittorie %s: %d", player2, victoriesPlayer2));

        if(player1.getName().equals("Tutti")&&!player2.getName().equals("Tutti")){

            pl1Score.setText(String.format("Vittorie %s: %d", "degli altri", victoriesPlayer1-victoriesPlayer2));
            pl2Score.setText(String.format("Vittorie %s: %d", player2, victoriesPlayer2));

        }else  if(player2.getName().equals("Tutti")&&!player1.getName().equals("Tutti")) {
            pl1Score.setText(String.format("Vittorie %s: %d", player1, victoriesPlayer1));
            pl2Score.setText(String.format("Vittorie %s: %d", "degli altri", victoriesPlayer2-victoriesPlayer1));

        }else {
            pl1Score.setText(String.format("Vittorie %s: %d", player1, victoriesPlayer1));
            pl2Score.setText(String.format("Vittorie %s: %d", player2, victoriesPlayer2));
        }

            matchAdapter = new MatchAdapter(this, matches);
        ListView matchListView = findViewById(R.id.match_list_view);
        matchListView.setAdapter(matchAdapter);

        matchAdapter.notifyDataSetChanged();
    }

  /*  @Override
    public void onFirebaseDataChanged() {
        updateMatches();
    }*/

    public void updateMatches() {
        // Ottieni i giocatori selezionati, lo sport selezionato e la data selezionata
        Player player1 = (Player) player1Spinner.getSelectedItem();
        Player player2 = (Player) player2Spinner.getSelectedItem();
        String selectedSport = MainActivity.matchType;
        String selectedDate="Tutte le Date";
        try {
            selectedDate = dateSpinner.getSelectedItem().toString();
        } catch (Exception e) {

        }

        if (player1.getName().equals("Tutti") && player2.getName().equals("Tutti")) {
            List<Match> allMatches = new ArrayList<>();
            for (Player p1 : players) {
                String finalSelectedDate = selectedDate;
                allMatches.addAll(p1.getMatches().stream().filter(a->a.type.equals(MainActivity.matchType)&&(finalSelectedDate.equals("Tutte le Date") || a.getDate().equals(finalSelectedDate))).collect(Collectors.toList()));
                }
//(date.equals("Tutte le Date") || match.getDate().equals(date))
            try {

                List<Match> allMatches2 = allMatches.stream().distinct().collect(Collectors.toList());

                List<Match> allMatches3 = distinctMatch(allMatches);
                Log.e("bb", "g");
            }
            catch (Exception e)
            {
e.printStackTrace();
            }
            displayMatches(distinctMatch(allMatches), player1, player2);
        }
        // Se almeno uno dei giocatori è "Tutti", ottieni tutti i match per la data selezionata
       else if (player1.getName().equals("Tutti") || player2.getName().equals("Tutti")) {
            List<Match> allMatches = new ArrayList<>();
            for (Player p1 : players) {
                for (Player p2 : players) {
                    // Filtra i match in cui almeno uno dei due giocatori non è "Tutti"
                    if ((!player1.getName().equals("Tutti") && p1.getName().equals(player1.getName())) ||
                            (!player2.getName().equals("Tutti") && p2.getName().equals(player2.getName()))) {
                        allMatches.addAll(p1.getMatchesOnDateWithOpponents(Collections.singletonList(p2.getName()), selectedDate, selectedSport));
                    }
                }
            }
            displayMatches(distinctMatch(allMatches), player1, player2);
        } else {
            // Altrimenti, ottieni i match tra i giocatori specifici selezionati
            List<Match> matches = getMatchesBetweenPlayers(player1, player2, selectedSport, selectedDate);
            displayMatches(matches, player1, player2);
        }
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
        if (player1.getName().equals("Tutti")) {
            // Se player1 è "Tutti", ottieni tutti i match per la data selezionata con player2 come avversario
            return player2.getMatchesOnDateWithOpponents(getPlayerNames(), selectedDate, selectedSport);
        } else if (player2.getName().equals("Tutti")) {
            // Se player2 è "Tutti", ottieni tutti i match per la data selezionata con player1 come avversario
            return player1.getMatchesOnDateWithOpponents(getPlayerNames(), selectedDate, selectedSport);
        } else {
            // Altrimenti, restituisci solo i match tra i giocatori specifici selezionati
            ArrayList<String> opponentNames = new ArrayList<>();
            opponentNames.add(player2.getName());
            return player1.getMatchesOnDateWithOpponents(opponentNames, selectedDate, selectedSport);
        }
    }


}
