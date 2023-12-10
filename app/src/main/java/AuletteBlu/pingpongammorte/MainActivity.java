package AuletteBlu.pingpongammorte;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;



import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;


import static AuletteBlu.pingpongammorte.LeaderboardActivity.deepCloneList;
import static AuletteBlu.pingpongammorte.LeaderboardActivity.findPlayerByNameWithoutCloning;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.github.javiersantos.appupdater.AppUpdater;

import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import AuletteBlu.pingpongammorte.utils.UpdateManager;

public class MainActivity extends AppCompatActivity implements DriveInteraction.FirebaseUpdateListener {

  static public  PackageInfo packageInfo=null;


    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String UNIQUE_ID_KEY = "unique_id";
    public static String uniqueID;

    LinearLayout layoutSpinner;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_MANAGE_STORAGE = 2;

    static public String matchType="PingPong 1v1";
    private Spinner player1Spinner;
    private Spinner player2Spinner;
    private Button submitButton;
    private Button viewScoresButton;
    private Button viewHistoryButton;
    private Button viewHistoryAlbo;
    Button btnClearPlayers;
    Button btnMockPlayer;

    static String playerName="";

    public static ArrayList<Player> players; // Popolato con i dati
    private List<Player> mockPlayers; // Popolato con i dati

    @Override
    public void onFirebaseDataChanged() {
        loadScoresFromPreferences(new LoadScoresCallback() {
            @Override
            public void onScoresLoaded(List<Player> players) {
                // Logica da eseguire dopo il caricamento di 'players'

                postLoadPlayers();
                mettiSfondo();
            }
        });
    }

    private void checkAndRequestStoragePermission(Runnable onSuccess) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Se è Android 10 o superiore, esegui direttamente il Runnable
            // poiché stai usando il "scoped storage"
            onSuccess.run();
        } else {
            // Controlla e richiedi il permesso per le versioni precedenti di Android
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE);
            } else {
                onSuccess.run();
            }
        }
    }

    private void checkAndRequestManageStoragePermission(Runnable onSuccess) {
        if (Environment.isExternalStorageManager()) {
            // You have the permission, do the storage task
            onSuccess.run();
        } else {
            // You don't have the permission, launch the settings screen
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, MY_PERMISSIONS_REQUEST_MANAGE_STORAGE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permesso concesso
                    //saveScoresToPreferencesBackup();
                    // o loadScoresFromPreferencesBackup();
                } else {
                    // Permesso negato. Potresti voler mostrare all'utente una spiegazione
                    // o disabilitare la funzionalità che necessita dei permessi.
                    Toast.makeText(this, "Permesso negato", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // Qui puoi gestire altri casi di 'requestCode' se hai altre richieste di permessi.
        }
    }


    /*public void updatePlayers() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String   jsonFromDB=driveInteraction.getJsonSynchronously();
                Log.e("FIREBASE", jsonFromDB);

                Type playerListType = new TypeToken<ArrayList<Player>>() {}.getType();

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Match.class, new MatchDeserializer());
                Gson gson = gsonBuilder.create();


                try {
                    players = gson.fromJson(jsonFromDB, playerListType);
                } catch (JsonSyntaxException e) {
                    FileInputStream fis = null;
                    try {
                        fis = openFileInput("players.json");
                    } catch (FileNotFoundException ex) {

                    }
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String text = "";
                    while (true) {
                        try {
                            if (!((text = br.readLine()) != null)) break;
                        } catch (IOException ex) {

                        }
                        sb.append(text).append("\n");
                    }

                    players = gson.fromJson(sb.toString(), playerListType);
                }

                //qui sollevo una callback

                //readFirebaseData();
//                Log.e("DriveInteraction 1",convertTimestampToDate(getLastModifiedSynchronously()));
//
//                Log.e("DriveInteraction 3",convertTimestampToDate(getLastModifiedSynchronously()));
//                uploadText("Nuoverrimissimio contenuto del JSON");
//                Log.e("DriveInteraction 4",convertTimestampToDate(getLastModifiedSynchronously()));
//                Log.e("DriveInteraction 5",getJsonSynchronously());
            }
        }).start();

    }
*/

    private void loadScoresFromPreferences(LoadScoresCallback callback) {

        try {
            Type playerListType = new TypeToken<ArrayList<Player>>() {}.getType();

            // Registra il deserializzatore personalizzato e crea un'istanza di Gson
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Match.class, new MatchDeserializer());
            Gson gson = gsonBuilder.create();


            try {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String   jsonFromDB=driveInteraction.getJsonSynchronously();

                        Log.e("FIREBASE", jsonFromDB);

                        try {
                            players = gson.fromJson(jsonFromDB, playerListType);
                            blockSaving=false;
                        } catch (JsonSyntaxException e) {
                            blockSaving=true;

                            FileInputStream fis = null;
                            try {
                                fis = openFileInput("players.json");
                            } catch (FileNotFoundException ex) {

                            }
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader br = new BufferedReader(isr);
                            StringBuilder sb = new StringBuilder();
                            String text = "";
                            while (true) {
                                try {
                                    if (!((text = br.readLine()) != null)) break;
                                } catch (IOException ex) {

                                }
                                sb.append(text).append("\n");
                            }

                            players = gson.fromJson(sb.toString(), playerListType);
                        }

                        //qui sollevo una callback
                        if (callback != null) {
                            callback.onScoresLoaded(players);
                        }
                        //readFirebaseData();
//                Log.e("DriveInteraction 1",convertTimestampToDate(getLastModifiedSynchronously()));
//
//                Log.e("DriveInteraction 3",convertTimestampToDate(getLastModifiedSynchronously()));
//                uploadText("Nuoverrimissimio contenuto del JSON");
//                Log.e("DriveInteraction 4",convertTimestampToDate(getLastModifiedSynchronously()));
//                Log.e("DriveInteraction 5",getJsonSynchronously());
                    }
                }).start();

                //String jsonFromDB=driveInteraction.getJsonSynchronously();
                //     Log.e("FIREBASE", jsonFromDB);
                //     players = gson.fromJson(jsonFromDB, playerListType);
            } catch (IllegalStateException e) {
                Log.e("FirebaseInit", "Firebase non è stato inizializzato");
                // Gestisci l'errore
            } catch (Exception e) {
                e.printStackTrace();
            }







        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static final int READ_REQUEST_CODE = 42;

    private void loadScoresFromPreferencesBackup() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        String downloadsDirPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).toString() + "/PingPongAmmorteBackup";
        Uri initialUri = Uri.parse(downloadsDirPath + "/playersBU.json");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        // Handle the result for the MANAGE_EXTERNAL_STORAGE permission request
        if (requestCode == MY_PERMISSIONS_REQUEST_MANAGE_STORAGE) {
            if (Environment.isExternalStorageManager()) {
                // The permission is granted, continue with the storage task
                //doYourStorageOperation();
            } else {
                // The permission is denied
                //handlePermissionDenied();
            }
        }

        // Handle the result for your file read request
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                loadFile(uri);
            }
        }
    }


    private void loadFile(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                Gson gson = new Gson();
                Type playerListType = new TypeToken<ArrayList<Player>>() {}.getType();
                players = gson.fromJson(sb.toString(), playerListType);
                is.close();
                updateAdapter();
                saveScoresToPreferences();
                foundImgSfondo();
                mettiSfondo();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void updateAdapter() {
        ArrayAdapter<Player> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, players);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        player1Spinner.setAdapter(adapter);
        player2Spinner.setAdapter(adapter);
    }


    private void saveScoresToPreferencesBackup2() {
        Gson gson = new Gson();
        String playersJson = gson.toJson(players);

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File backupDir = new File(downloadsDir, "PingPongAmmorteBackup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        File backupFile = new File(backupDir, "playersBU.json");

        try (FileOutputStream fos = new FileOutputStream(backupFile)) {
            fos.write(playersJson.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadScoresFromPreferencesBackup2() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File backupDir = new File(downloadsDir, "PingPongAmmorteBackup");
        File backupFile = new File(backupDir, "playersBU.json");

        if (backupFile.exists()) {
            try (FileInputStream fis = new FileInputStream(backupFile);
                 InputStreamReader isr = new InputStreamReader(fis);
                 BufferedReader br = new BufferedReader(isr)) {

                StringBuilder sb = new StringBuilder();
                String text;
                while ((text = br.readLine()) != null) {
                    sb.append(text).append("\n");
                }
                Gson gson = new Gson();
                Type playerListType = new TypeToken<ArrayList<Player>>() {}.getType();
                players = gson.fromJson(sb.toString(), playerListType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveScoresToPreferences() {
        Gson gson = new Gson();
        String playersJson = gson.toJson(players);

        try {
            //driveInteraction.readFirebaseData();
            driveInteraction.uploadText(playersJson);
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

    }

    public void hideKeyboard() {
        Activity activity =this;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Trova la view che ha il focus attualmente, potrebbe non essere una view di input.
        View view = activity.getCurrentFocus();
        // Se nessuna view ha il focus, crea una nuova così possiamo nascondere comunque la tastiera
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void saveScoresToPreferencesBackup() {
        Gson gson = new Gson();
        String playersJson = gson.toJson(players);



        // Definisci le variabili per il nome del file e il percorso relativo
        String backupDirectory = Environment.DIRECTORY_DOWNLOADS + "/PingPongAmmorteBackup/";
        String fileName = "playersBU.json";

        // Assicurati che la directory OLD esista
        ensureOldDirectoryExists();

        // Crea la selezione e gli argomenti di selezione
        Uri existingBackupUri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.Files.FileColumns.DISPLAY_NAME + "=? AND " +
                MediaStore.Files.FileColumns.RELATIVE_PATH + "=?";
        String[] selectionArgs = new String[]{fileName, backupDirectory};

        // Controlla se esiste un file con il nome e percorso forniti
        Cursor cursor = getContentResolver().query(existingBackupUri, null, selection, selectionArgs, null);
        // ... il resto del tuo codice sopra ...

        if (cursor != null && cursor.moveToFirst()) {
            // Ottieni l'ID del file originale dalla Cursor
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
            long fileId = cursor.getLong(idColumn);

            // Costruisci l'URI del file originale da eliminare
            Uri fileToDeleteUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), fileId);

            // Sposta il file nella directory OLD
            Uri oldBackupUri = copyToOldDirectory(cursor);

            // Elimina il file originale utilizzando l'URI corretto
            getContentResolver().delete(fileToDeleteUri, null, null);
            cursor.close();
        }

        else {
            // Log per debugging
            Log.d("BackupPathCheck", "No existing file to move. Path checked: " + backupDirectory);
        }

        // Se necessario, chiudi il cursore
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        // Utilizza le stesse variabili per i ContentValues
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
        values.put(MediaStore.Downloads.RELATIVE_PATH, backupDirectory);

        // Inserisci il nuovo file nel MediaStore
        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            if (os != null) {
                os.write(playersJson.getBytes());
                // Non è necessario chiamare os.close() qui, perché try-with-resources lo farà per te
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("BackupPathCheck", "Error saving file: " + e.getMessage());
        }

    }


    private Uri copyToOldDirectory(Cursor cursor) {
        String oldBackupDir = Environment.DIRECTORY_DOWNLOADS + "/PingPongAmmorteBackup/OLD";
        String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        long id = cursor.getLong(idColumn);
        Uri sourceUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id);

        // Creiamo un nuovo URI nella directory OLD
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
        values.put(MediaStore.Downloads.RELATIVE_PATH, oldBackupDir);
        Uri destUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

        // Copia effettivamente il contenuto
        try (InputStream is = getContentResolver().openInputStream(sourceUri);
             OutputStream os = getContentResolver().openOutputStream(destUri)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        pruneOldDirectory();
        return destUri;
    }

    private static final int MAX_FILES_IN_OLD = 50;

    private void pruneOldDirectory() {
        Uri oldBackupUri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + "=?";
        String[] selectionArgs = new String[]{Environment.DIRECTORY_DOWNLOADS + "/PingPongAmmorteBackup/OLD/"};

        Cursor cursor = getContentResolver().query(oldBackupUri, null, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " ASC");
        if (cursor != null) {
            int filesToDelete = cursor.getCount() - (MAX_FILES_IN_OLD );
            while (filesToDelete > 0 && cursor.moveToNext()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                long id = cursor.getLong(idColumn);
                Uri fileUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id);
                getContentResolver().delete(fileUri, null, null);
                filesToDelete--;
            }
            cursor.close();
        }
    }


    private void ensureOldDirectoryExists() {
        String selection = MediaStore.Downloads.DISPLAY_NAME + "=? AND " +
                MediaStore.Downloads.RELATIVE_PATH + "=?";
        String[] selectionArgs = {".nomedia", Environment.DIRECTORY_DOWNLOADS + "/PingPongAmmorteBackup/OLD"};

        try (Cursor cursor = getContentResolver().query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                null, selection, selectionArgs, null)) {

            // Se il file .nomedia non esiste, lo creiamo
            if (cursor == null || !cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, ".nomedia");
                values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PingPongAmmorteBackup/OLD");

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    Log.d("Backup", "Il file .nomedia dovrebbe essere stato creato nella cartella OLD!");
                } else {
                    Log.d("Backup", "Errore nella creazione del file .nomedia nella cartella OLD!");
                }
            } else {
                Log.d("Backup", "Il file .nomedia esiste già nella cartella OLD!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static void setMatchesToDate(List<Player> players, LocalDate newDate) {
        for (Player player : players) {
            for (Match match : player.getMatches()) {
                match.setDate(newDate.toString());
            }
        }
    }


    private void updatePlayerAndMatches(Player player, String newName) {
        // Aggiorna il nome del giocatore
        player.setName(newName);

        // Aggiorna i nomi nei match associati al giocatore
        for (Player otherPlayer : players) {
            for (Match match : otherPlayer.getMatches()) {
                if (match.getWinner().equals(player.getName())) {
                    match.setWinner(newName);
                }
                if (match.getLoser().equals(player.getName())) {
                    match.setLoser(newName);
                }
            }
        }
    }

    private static boolean containsName(String[] array, String name) {
        for (String element : array) {
            if (element.toLowerCase().trim().equals(name.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }
    // Metodo per verificare la presenza di duplicati
    private static boolean containsDuplicate(String[] array1, String[] array2) {
        for (String name : array1) {
            if (containsName(array2, name)) {
                return true;
            }
        }
        return false;
    }


    public interface LoadScoresCallback {
        void onScoresLoaded(List<Player> players);
    }

    public void postLoadPlayers(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                runPostLoadPlayers();
                if (blockSaving){
                    layoutSpinner.setBackgroundColor(Color.RED);
                    //saveScoresToPreferences();
                    Toast.makeText(MainActivity.this, "CARICAMENTO FALLITO: DATI INCONSISTENTI. Chiudere e riaprire l'app oppure è possibile solo leggere i dati locali", Toast.LENGTH_SHORT).show();
                }
                else
                    layoutSpinner.setBackgroundColor(Color.TRANSPARENT);
            }
        });


    }
    public void runPostLoadPlayers(){





        LocalDate specificDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            specificDate = LocalDate.of(2023, Month.OCTOBER, 29);
        }
        //setMatchesToDate(players, specificDate);
        player1Spinner = findViewById(R.id.player1_spinner);
        player2Spinner = findViewById(R.id.player2_spinner);
        submitButton = findViewById(R.id.button_submit);
        viewScoresButton = findViewById(R.id.button_view_scores);
        viewHistoryButton = findViewById(R.id.button_view_history);
        viewHistoryAlbo =  findViewById(R.id.button_view_albo);
        btnClearPlayers = findViewById(R.id.clearPlayer);
        btnMockPlayer = findViewById(R.id.MockPlayer);

        mockPlayers = deepCloneList(players);  // Ottieni una copia profonda della lista originale

        layoutSpinner = findViewById(R.id.layout_spinner);

        Button addPlayerButton = findViewById(R.id.addPlayerButton);
        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddPlayerClicked();
                saveScoresToPreferences();
                hideKeyboard();
            }
        });

        btnClearPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestStoragePermission(new Runnable() {
                    @Override
                    public void run() {

                        checkAndRequestManageStoragePermission(new Runnable() {
                            @Override
                            public void run() {


                                saveScoresToPreferencesBackup();
                            }
                        });

                    }
                });
            }
        });

        btnMockPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestStoragePermission(new Runnable() {
                    @Override
                    public void run() {
                        loadScoresFromPreferencesBackup();
                    }
                });
            }
        });


       /* RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = group.findViewById(checkedId);
                matchType = radioButton.getText().toString();
            }
        });*/
        GridLayout gridLayout = findViewById(R.id.sports);

// Inizializza un OnClickListener che sarà comune a tutti i RadioButton.
        View.OnClickListener radioButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deseleziona tutti i RadioButton nel GridLayout.
                for (int i = 0; i < gridLayout.getChildCount(); i++) {
                    View child = gridLayout.getChildAt(i);
                    if (child instanceof RadioButton) {
                        ((RadioButton) child).setChecked(false);
                    }
                }
                // Seleziona il RadioButton che è stato cliccato.
                ((RadioButton) v).setChecked(true);
                matchType = ((RadioButton) v).getText().toString();

                refreshSpinner();
                foundImgSfondo();
                mettiSfondo();
            }
        };

// Assegna l'OnClickListener a tutti i RadioButton dentro il GridLayout.
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof RadioButton) {
                child.setOnClickListener(radioButtonClickListener);
            }
        }

        refreshSpinner();
        EditText player1ScoreEditText = findViewById(R.id.player1_score);
        EditText player2ScoreEditText = findViewById(R.id.player2_score);

        // Imposta il valore di default a -1
        player1ScoreEditText.setText("-1");
        player2ScoreEditText.setText("-1");

        // Aggiungi un OnFocusChangeListener
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    EditText editText = (EditText) v;
                    if (editText.getText().toString().equals("-1")) {
                        editText.setText("");
                    }
                }
            }
        };

        player1ScoreEditText.setOnFocusChangeListener(onFocusChangeListener);
        player2ScoreEditText.setOnFocusChangeListener(onFocusChangeListener);


//        player1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Do nothing here
//            }
//        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(blockSaving){
                    //saveScoresToPreferences();
                    Toast.makeText(MainActivity.this, "CARICAMENTO FALLITO: DATI INCONSISTENTI. Chiudere e riaprire l'app", Toast.LENGTH_SHORT).show();
                    //coloraSfondo(false);
                    Log.e("log","blocked");
                    return;
                }

                int player1Score=-1;
                int player2Score=-1;

                try {
                    // Converti i punteggi da String a Integer
                    player1Score = Integer.parseInt(player1ScoreEditText.getText().toString().trim());
                    player2Score = Integer.parseInt(player2ScoreEditText.getText().toString().trim());
                } catch (NumberFormatException e) {
                    player1Score=-1;
                    player2Score=-1;

                }

// Inizialmente assumiamo che il giocatore 1 sia il vincitore
                Player winner = (Player) player1Spinner.getSelectedItem();
                Player loser = (Player) player2Spinner.getSelectedItem();
                int winnerScore = player1Score;
                int loserScore = player2Score;

// Se il punteggio del giocatore 2 è maggiore, allora diventa il vincitore
                if (player2Score > player1Score) {
                    winner = (Player) player2Spinner.getSelectedItem();
                    loser = (Player) player1Spinner.getSelectedItem();
                    winnerScore = player2Score;
                    loserScore = player1Score;
                }

// Aggiorna i giocatori con i loro nomi reali
                winner = findPlayerByNameWithoutCloning(players, winner.getName());
                loser = findPlayerByNameWithoutCloning(players, loser.getName());

// Controlla che i giocatori selezionati non siano la stessa persona
                if (winner.equals(loser)) {
                    //saveScoresToPreferences();
                    Toast.makeText(MainActivity.this, "I giocatori selezionati devono essere diversi!", Toast.LENGTH_SHORT).show();
                    coloraSfondo(false,"","");
                    return;
                }


                String[] winnerNames = winner.getName().split("-");
                String[] loserNames = loser.getName().split("-");

                // Verifica che non ci siano duplicati
                if (containsDuplicate(winnerNames, loserNames)) {
                    Toast.makeText(MainActivity.this, "FAGGIANO! Un giocatore non può stare in entrambe le squadre.", Toast.LENGTH_SHORT).show();
                    coloraSfondo(false,"","");;
                    return;

                }

// Aggiorna il punteggio del vincitore e registra la vittoria
                winner.setScore(winner.getScore() + 1);
                winner.addVictoryAgainst(loser.getName());

                // String currentDate = null;
                int delay = 0; // Numero di giorni da sottrarre
                String resultDate = null;
                String currentHour=LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    LocalDate currentDate = LocalDate.now();
                    LocalTime currentTime = LocalTime.now();

                    // Controlla se l'ora corrente è tra mezzanotte e le 2 AM
                    if (currentTime.isAfter(LocalTime.MIDNIGHT) && currentTime.isBefore(LocalTime.of(2, 0))) {
                        // Se sì, considera la partita come parte del giorno precedente
                        currentDate = currentDate.minusDays(1);
                    }

                    // Qui puoi aggiungere il tuo "delay" se necessario
                    LocalDate adjustedDate = currentDate.minusDays(delay);
                    resultDate = adjustedDate.toString();
                    // Fai qualcosa con resultDate, ad esempio mostralo all'utente
                }
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    // Imposta la data al 29 ottobre 2023
//                    LocalDate specificDate = LocalDate.of(2023, Month.OCTOBER, 29);
//                    resultDate = specificDate.toString();
//                    // Fai qualcosa con resultDate, ad esempio mostralo all'utente
//                }


                // Crea e salva il match
                Match match = new Match(winner.getName(), loser.getName(), resultDate, matchType, winnerScore, loserScore,currentHour,RandomStringGenerator.generateRandomString());

                winner.addMatch(match);
                loser.addMatch(match);
                saveScoresToPreferences();

// Messaggio di vittoria
                if(winnerScore-loserScore>7)
                    Toast.makeText(MainActivity.this, winner.getName() + " ROMPE IL CULO a quella pippa di "+loser.getName(), Toast.LENGTH_SHORT).show();

                else
                    Toast.makeText(MainActivity.this, winner.getName() + " VINCE  contro quella pippa di "+loser.getName(), Toast.LENGTH_SHORT).show();
                coloraSfondo(true, winner.getName(), loser.getName());
// Reset dei campi del punteggio
                player1ScoreEditText.setText("-1");
                player2ScoreEditText.setText("-1");

// Nascondi la tastiera
                hideKeyboard();
                refreshSpinner();
            }
        });

        viewScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
                intent.putExtra("players", players);

                startActivity(intent);
            }
        });

        viewHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, MatchActivity.class);
                intent.putExtra("players", players);
                startActivity(intent);

            }
        });

        viewHistoryAlbo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, AlboDOroActivity.class);
                intent.putExtra("players", players);
                startActivity(intent);

            }
        });

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        // Il codice qui viene eseguito quando l'attività sta per essere riavviata dopo essere stata fermata.
        Log.d("SDL", "onRestart: Activity in fase di riavvio.");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Il codice qui viene eseguito quando l'attività sta diventando visibile all'utente.
        Log.d("SDL", "onStart: Activity visibile.");
    }

    @Override
    protected void onResume() {
        Log.v("SDL", "onResume()");
        super.onResume();
        // updatePlayers();
        loadScoresFromPreferences(new LoadScoresCallback() {
            @Override
            public void onScoresLoaded(List<Player> players) {
                // Logica da eseguire dopo il caricamento di 'players'

                postLoadPlayers();
                mettiSfondo();
            }
        });
    }


    boolean blockSaving=true;  //se il load da db non è andato a buon fine, dati incostistenti, non permetto di salvarle
    static  DriveInteraction driveInteraction = new DriveInteraction();

    Context context=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uniqueID = getUniqueID();

        // Ottieni il riferimento al TextView

        // Ottieni il riferimento al TextView
        TextView versionTextView = findViewById(R.id.app_version);

// Ottieni la versione dell'app dalla configurazione del pacchetto
    //    String versionName = BuildConfig.VERSION_NAME;

// Imposta il testo del TextView con la versione dell'app



            try {
                packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {

            }


        try {

            String versionName = packageInfo.versionName;
            versionTextView.setText("Versione: " + versionName);
            // Usa il versionName come necessario
        } catch (Exception e) {
            e.printStackTrace();
            // Gestisci l'eccezione
        }


/*
        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.setUpdateFrom(UpdateFrom.GITHUB)
                .setGitHubUserAndRepo("AlexanderG84", "PingPongAmmorteRep")
                .start();*/

                try {
                    UpdateManager updateManager = new UpdateManager(getApplicationContext());
                    updateManager.checkForUpdates();
                } catch (Exception e) {

                }


        // FirebaseApp.initializeApp(getApplicationContext());

        driveInteraction.initializeFirebase();
        driveInteraction.setUpdateListener(this);
        driveInteraction.startListeningForUpdates();

        Log.d("DriveInte FirebaseInit", "Firebase inizializzato con successo");
        // Supponiamo che tu abbia una lista di giocatori predefinita
        players = new ArrayList<>(Arrays.asList(
                new Player("Talex", 0),
                new Player("Pompolus", 0),
                new Player("Strino", 0),
                new Player("Daniele", 0),

                new Player("Pompolus - Talex", 0),
                new Player("Strino - Daniele", 0),
                new Player("Talex - Daniele", 0),
                new Player("Strino - Pompolus", 0),
                new Player("Daniele - Pompolus", 0),
                new Player("Strino - Talex", 0)
        ));

        context=getApplicationContext();
        loadScoresFromPreferences(new LoadScoresCallback() {
            @Override
            public void onScoresLoaded(List<Player> players) {
                // Logica da eseguire dopo il caricamento di 'players'

                postLoadPlayers();
                foundImgSfondo();
                mettiSfondo();

            }
        });

    }

    public void downloadApk() throws IOException {
        // Download the APK file
        String apkUrl = "https://example.com/your-updated-app.apk"; // Replace with your APK URL
        URL url = new URL(apkUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = urlConnection.getInputStream();

            // Save the downloaded APK to a file
            File apkFile = new File(getExternalFilesDir(null), "update.apk");
            FileOutputStream outputStream = new FileOutputStream(apkFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.close();

            // Install the downloaded APK using PackageInstaller
            Uri apkUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", apkFile);
            Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            installIntent.setData(apkUri);
            installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(installIntent);
        }

    }


    public void foundImgSfondo(){
        try {
            List<LocalDate> matchDates = new ArrayList<>();
            for (Player player : players) {
                for (Match match : player.getMatches()) {
                    if(match.type.equals(matchType)){
                    LocalDate matchDate = LocalDate.parse(match.getDate());
                    if (!matchDates.contains(matchDate)) {
                        matchDates.add(matchDate);
                    }
                    }
                }
            }

// 2. Trova la data più recente che non sia la data odierna
            LocalDate today = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime nowMinusThreeHours = LocalDateTime.now(ZoneId.systemDefault()).minusHours(3);
                today = nowMinusThreeHours.toLocalDate();


            }
            LocalDate finalToday = today;
            LocalDate lastValidDate = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                lastValidDate = matchDates.stream()
                        .filter(date -> !date.equals(finalToday))
                        .max(LocalDate::compareTo)
                        .orElse(null);
            }

            List<Player> clonedPlayers=deepCloneList(players);
            if(matchType.toLowerCase().contains("2v2")){

                clonedPlayers=createIndividualPlayersList(clonedPlayers);
            }
            if (lastValidDate != null) {
                LocalDate finalLastValidDate = lastValidDate;
                int maxWins = clonedPlayers.stream()
                        .filter(player -> !player.getName().contains("-"))
                        .filter(player -> player.playedOnDate(finalLastValidDate.toString()))
                        .mapToInt(player -> countWinsOnDate(player, finalLastValidDate))
                        .max()
                        .orElse(0);

                Player playerWithMostWins = clonedPlayers.stream()
                        .filter(player -> !player.getName().contains("-"))
                        .filter(player -> player.playedOnDate(finalLastValidDate.toString()))
                        .filter(player -> countWinsOnDate(player, finalLastValidDate) == maxWins)
                        .max(Comparator.comparingDouble(player -> countPercWinsOnDate(player, finalLastValidDate)))
                        .orElse(null);

                if (playerWithMostWins != null) {
                    System.out.println("Il giocatore con più vittorie (e percentuale più alta in caso di parità): " + playerWithMostWins.getName());
                    playerName = playerWithMostWins.getName();
                } else {
                    playerName = "";
                }
            } else playerName = "";
        } catch (Exception e) {
            playerName="";
        }
    }
    public void mettiSfondo(){

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
                LinearLayout mainLayout = findViewById(R.id.layout_spinner);
                Drawable originalDrawable = ContextCompat.getDrawable(MainActivity.this, finalImageResourceId);

                // Crea una copia del Drawable originale
                Drawable playerImage = originalDrawable.getConstantState().newDrawable().mutate();


                playerImage.setAlpha(80); // Sostituisci con il valore di alpha desiderato

                mainLayout.setBackground(playerImage);
            });


    }

    // Metodo helper per contare le vittorie di un giocatore in una specifica data
    private int countWinsOnDate(Player player, LocalDate date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return (int) player.getMatches().stream()
                    .filter(match ->match.type.equals(matchType)&& match.getWinner().equals(player.getName()))
                    .filter(match ->match.type.equals(matchType)&& LocalDate.parse(match.getDate()).equals(date))
                    .count();
        }
        else return 0;
    }


    private double countPercWinsOnDate(Player player, LocalDate date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long wins = player.getMatches().stream()
                    .filter(match ->match.type.equals(matchType)&& match.getWinner().equals(player.getName()))
                    .filter(match ->match.type.equals(matchType)&&  LocalDate.parse(match.getDate()).equals(date))
                    .count();
            long played = player.getMatches().size();

            return played > 0 ? (double) wins / played * 100 : 0;
        } else return 0;
    }



    public void coloraSfondo(boolean res, String winnerName, String loserName){
        if(res){
            // Imposta il colore di sfondo verde
            layoutSpinner.setBackgroundColor(Color.GREEN);

            // Crea e mostra la Dialog
            final Dialog dialog = new Dialog(this); // Assicurati che 'this' sia un Context valido
            dialog.setContentView(R.layout.dialog_layout);

            // Ottieni le ImageView dal layout
            ImageView imageViewWinnerTop = dialog.findViewById(R.id.imageViewWinnerTop);
            ImageView imageViewWinnerBottom = dialog.findViewById(R.id.imageViewWinnerBottom);
            ImageView imageViewLoserTop = dialog.findViewById(R.id.imageViewLoserTop);
            ImageView imageViewLoserBottom = dialog.findViewById(R.id.imageViewLoserBottom);

            // Gestisci la visibilità e imposta le immagini per vincitori e perdenti
            handlePlayerImageViews(new ImageView[]{imageViewWinnerTop, imageViewWinnerBottom}, winnerName, true);
            handlePlayerImageViews(new ImageView[]{imageViewLoserTop, imageViewLoserBottom}, loserName, false);

            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            dialog.show();

            // Applica le animazioni
            applyAnimation(new ImageView[]{imageViewWinnerTop, imageViewWinnerBottom}, true);
            applyAnimation(new ImageView[]{imageViewLoserTop, imageViewLoserBottom}, false);

            // Handler per chiudere la dialog dopo un ritardo
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    layoutSpinner.setBackgroundColor(Color.TRANSPARENT);
                    mettiSfondo();
                }
            }, 1500); // Ritardo in millisecondi
        } else {
            // Imposta il colore di sfondo rosso e ritarda il ripristino del colore
            layoutSpinner.setBackgroundColor(Color.RED);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    layoutSpinner.setBackgroundColor(Color.TRANSPARENT);
                    mettiSfondo();
                }
            }, 500);
        }
        mettiSfondo();
    }

    private List<Player> createIndividualPlayersList(List<Player> originalPlayers) {
        List<Player> individualPlayers = new ArrayList<>();

        for (Player originalPlayer : originalPlayers) {
            if ( originalPlayer.getName().contains("-")) {
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

    // Metodo per gestire la visibilità e impostare le immagini dei giocatori
    private void handlePlayerImageViews(ImageView[] imageViews, String playerName, boolean isWinner) {
        String[] players = playerName.split("-");
        for (int i = 0; i < imageViews.length; i++) {
            if (i < players.length) {
                setPlayerImage(imageViews[i], players[i],isWinner);
                imageViews[i].setVisibility(View.VISIBLE);
            } else {
                imageViews[i].setVisibility(View.GONE);
            }
        }
    }

    // Metodo per impostare l'immagine di un singolo giocatore
    private void setPlayerImage(ImageView imageView, String playerName, boolean isWinner) {
        String playerImageName = playerName.toLowerCase().trim();
        String suffix = isWinner ? "win" : "lose";
        int resId = getApplication().getResources().getIdentifier(playerImageName + suffix, "drawable", getApplication().getPackageName());

        if (resId == 0) {
            // Se non trova l'immagine con il suffisso, cerca senza il suffisso
            resId = getApplication().getResources().getIdentifier(playerImageName, "drawable", getApplication().getPackageName());
        }

        imageView.setImageResource(resId != 0 ? resId : R.drawable.default_player_image);
    }

    // Metodo per applicare le animazioni
    private void applyAnimation(ImageView[] imageViews, boolean isWinner) {
        for (ImageView imageView : imageViews) {
            if (imageView.getVisibility() == View.VISIBLE) {
                animatePlayer(imageView, isWinner);
            }
        }
    }

    // Metodo per animare un singolo giocatore

    private void animatePlayer(ImageView imageView, boolean isWinner) {
        if (isWinner) {
            // Ingrandimento semplice
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 2f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 2f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(scaleX, scaleY);
            set.setDuration(2000);
            set.start();
        } else {
            // Rimpicciolimento con rotazione e dissolvenza
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0f);
            ObjectAnimator rotate = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 180f);
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(scaleX, scaleY, rotate, fadeOut);
            set.setDuration(2000);
            set.start();
        }
    }


    private String getUniqueID() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String id = prefs.getString(UNIQUE_ID_KEY, null);

        if (id == null) {
            // Genera un nuovo ID univoco
            id = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(UNIQUE_ID_KEY, id);
            editor.apply();
        }

        return id;
    }

    public String getMyUniqueID() {
        return uniqueID;
    }


    private void setupPlayerSpinners() {
        player1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshPlayer2Spinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Gestisci il caso in cui non viene selezionato nessun elemento
            }
        });
        refreshPlayer2Spinner();
    }

    private void refreshPlayer2Spinner() {
        Player selectedPlayer1 = (Player) player1Spinner.getSelectedItem();

        List<Player> filteredPlayers = new ArrayList<>();

        // Filtra i giocatori in base al tipo di partita
        for (Player player : players) {
            if (matchType.toLowerCase().contains("2v2") && player.getName().contains("-")) {
                filteredPlayers.add(player);
            } else if (matchType.toLowerCase().contains("1v1")&& !player.getName().contains("-")) {
                // Se il tipo di partita non è "2v2", aggiungi tutti i giocatori
                filteredPlayers.add(player);
            }
        }


        List<Player> filteredPlayersForPlayer2 = new ArrayList<>();

        for (Player player : filteredPlayers) {
            if (isDifferentPlayer(selectedPlayer1, player)) {
                filteredPlayersForPlayer2.add(player);
            }
        }

        ArrayAdapter<Player> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, filteredPlayersForPlayer2);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        player2Spinner.setAdapter(adapter);
    }

    private boolean isDifferentPlayer(Player selectedPlayer1, Player player2) {
        if (selectedPlayer1 == null || player2 == null) {
            return true;
        }

        String selectedPlayer1Name = selectedPlayer1.getName();
        String player2Name = player2.getName();

        if (selectedPlayer1Name.contains("-")) {
            String[] parts = selectedPlayer1Name.split(" - ");
            return !player2Name.contains(parts[0].trim()) && !player2Name.contains(parts[1].trim());
        } else {
            return !selectedPlayer1Name.equals(player2Name);
        }
    }



    public void refreshSpinner(){

      /*  List<Player> filteredPlayers = new ArrayList<>();

        // Filtra i giocatori in base al tipo di partita
        for (Player player : players) {
            if (matchType.toLowerCase().contains("2v2") && player.getName().contains("-")) {
                filteredPlayers.add(player);
            } else if (matchType.toLowerCase().contains("1v1")&& !player.getName().contains("-")) {
                // Se il tipo di partita non è "2v2", aggiungi tutti i giocatori
                filteredPlayers.add(player);
            }
        }

        ArrayAdapter<Player> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, filteredPlayers);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item); // Usa il layout personalizzato per il menu a tendina

        player1Spinner.setAdapter(adapter);
        player2Spinner.setAdapter(adapter);
*/

        List<Player> filteredPlayers = new ArrayList<>();

        // Filtra i giocatori in base al tipo di partita
        for (Player player : players) {
            if (matchType.toLowerCase().contains("2v2") && player.getName().contains("-")) {
                filteredPlayers.add(player);
            } else if (matchType.toLowerCase().contains("1v1")&& !player.getName().contains("-")) {
                // Se il tipo di partita non è "2v2", aggiungi tutti i giocatori
                filteredPlayers.add(player);
            }
        }

        ArrayAdapter<Player> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, filteredPlayers);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        player1Spinner.setAdapter(adapter);

        // Inizializza i listener e l'adapter per player2Spinner
        setupPlayerSpinners();

    }
    public void onAddPlayerClicked() {
        EditText playerNameEditText = findViewById(R.id.playerNameEditText);
        String newPlayerName = playerNameEditText.getText().toString().trim();

        if (!newPlayerName.isEmpty() && !playerNameExists(newPlayerName)) {
            players.add(new Player(newPlayerName,0));
            playerNameEditText.setText("");  // Pulisci l'EditText
            Toast.makeText(this, "Giocatore aggiunto!", Toast.LENGTH_SHORT).show();
            coloraSfondo(true, "","");
        } else if (playerNameExists(newPlayerName)) {
            Toast.makeText(this, "Il nome del giocatore esiste già!", Toast.LENGTH_SHORT).show();
            coloraSfondo(false,"","");
        } else {
            Toast.makeText(this, "Inserisci un nome valido!", Toast.LENGTH_SHORT).show();
            coloraSfondo(false, "","");
        }



    }

    private boolean playerNameExists(String name) {
        for (Player player : players) {
            if (player.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}