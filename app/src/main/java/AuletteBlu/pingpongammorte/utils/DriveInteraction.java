package AuletteBlu.pingpongammorte.utils;

import static AuletteBlu.pingpongammorte.MainActivity.packageInfo;
import static AuletteBlu.pingpongammorte.MainActivity.players;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import AuletteBlu.pingpongammorte.MainActivity;

public class DriveInteraction {

    //private  DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    private boolean firstInteraction = true;

    public DatabaseReference databaseRef;

    public Long LastModificatedPlayers;

    public void initializeFirebase() {
        try {

            // databaseRef = database.getReference();

            databaseRef = FirebaseDatabase.getInstance().getReference();

            Log.e("firebase", "gg0");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startListeningForUpdates() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (updateListener != null) {
                    updateListener.onFirebaseDataChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Errore durante l'ascolto dei cambiamenti", databaseError.toException());
            }
        });
    }


    public static String convertTimestampToDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return formatter.format(date);
    }

    public  void testDrive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("DriveInteraction 2",getJsonSynchronously());
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

    /*public  void readFirebaseData() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.child("data").getValue(String.class);
                Long lastModified = dataSnapshot.child("lastModified").getValue(Long.class);
                Log.e("Firebase Read", "Data: " + (data != null ? data : "Dati non trovati"));
                Log.e("Firebase Read", "Last Modified: " + (lastModified != null ? lastModified : "Timestamp non trovato"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Errore durante la lettura dei dati", databaseError.toException());
            }
        });
    }*/

    public  boolean uploadText(String text,String actionType) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("data", text);
        if(actionType.equals("write"))
        updates.put("lastModified", ServerValue.TIMESTAMP);

        databaseRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> Log.e("Firebase Write", "Dati caricati con successo"))
                .addOnFailureListener(e -> Log.e("Firebase Write", "Errore durante il caricamento dei dati", e));
       return recordAccess(MainActivity.uniqueID, actionType);
    }

    public  void getLastModified(ValueEventListener callback) {
        DatabaseReference lastModifiedRef = databaseRef.child("lastModified");
        lastModifiedRef.addListenerForSingleValueEvent(callback);
    }

    public  void getData(ValueEventListener callback) {
        DatabaseReference dataRef = databaseRef.child("data");
        dataRef.addListenerForSingleValueEvent(callback);
    }






    public  String getJsonSynchronously()  {

        Long tmpLast=getLastModifiedSynchronously();
       /* if(LastModificatedPlayers==tmpLast&&players.size()>0)
            return "";
*/
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> jsonRef = new AtomicReference<>();

        DatabaseReference dataRef = databaseRef.child("data");
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                jsonRef.set(dataSnapshot.getValue(String.class));
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Gestire l'errore
                latch.countDown();
            }
        });

        try {
            latch.await(); // Attende che la chiamata al database sia completata
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        LastModificatedPlayers=tmpLast;
        recordAccess(MainActivity.uniqueID, "read");

        return jsonRef.get();
    }

    public  Long getLastModifiedSynchronously()  {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Long> lastModifiedRef = new AtomicReference<>();
        final Long[] gg = new Long[1];
        DatabaseReference lastModifiedDbRef = databaseRef.child("lastModified");
        lastModifiedDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gg[0] =dataSnapshot.getValue(Long.class);
               // lastModifiedRef.set(dataSnapshot.getValue(Long.class));
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Gestire l'errore
                latch.countDown();
            }
        });

        try {
            latch.await(); // Attende il completamento della chiamata al database
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
       // Long lastM=lastModifiedRef.get();
        Long lastM=gg[0];
        Log.e("Driveinteraction LastMod", lastM+"");
        return lastM;
    }


    private FirebaseUpdateListener updateListener;

    public void setUpdateListener(FirebaseUpdateListener listener) {
        this.updateListener = listener;
    }

    public interface FirebaseUpdateListener {
        void onFirebaseDataChanged();
    }



    public void startListeningToLastModified() {
        DatabaseReference lastModifiedRef = databaseRef.child("lastModified");
        lastModifiedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (updateListener != null) {
                    updateListener.onFirebaseDataChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    public boolean recordAccess(String userId, String actionType) {
       // if (!firstInteraction /*|| userId.equals("1442eea5-2d3c-42f8-a693-fd98f33d8549")*/)
      //      return false;
        try {
            Log.e("DriveInteraction",actionType);
           /* if(actionType.equals("write")&&LastModificatedPlayers!=null&&getLastModifiedSynchronously()>LastModificatedPlayers)
                return false;
*/

            firstInteraction = false;

            // DatabaseReference userLogRef = databaseRef.child("accessLogs").child(userId).child(formattedDate); // Usa il timestamp formattato come chiave diretta
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String formattedDate = formatter.format(new Date());

            long negTimestamp = Long.MAX_VALUE - System.currentTimeMillis();


            if (idNameMap.containsKey(userId)) {
                String nome = idNameMap.get(userId);
                // Restituisci la stringa composta da nome + "_" + id
                userId =  nome ;
            } else {
                // Se l'id non è presente nella lista, restituisci solo l'id

            }


            String key = negTimestamp + "___" + formattedDate;
            DatabaseReference userLogRef = databaseRef.child("accessLogs").child(userId).child(key);


            Map<String, Object> logData = new HashMap<>();
            logData.put("data", formattedDate); // Usa la data formattata
            logData.put("version", packageInfo.versionName);

            userLogRef.setValue(logData)
                    .addOnSuccessListener(aVoid -> Log.e("Firebase Access Log", "Accesso registrato con successo"))
                    .addOnFailureListener(e -> Log.e("Firebase Access Log", "Errore durante la registrazione dell'accesso", e));

            // Imposta la variabile su false dopo il primo accesso

            firstInteraction=false;
            return true;
        } catch (Exception e) {
return false;
        }
    }


    public Map<String, String> _getIdNameMapFromFirebaseSync() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Map<String, String>> idNameMapReference = new AtomicReference<>();

        DatabaseReference usersRef = databaseRef.child("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> idNameMap = new HashMap<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String userName = userSnapshot.child("name").getValue(String.class);

                    if (userName != null) {
                        idNameMap.put(userId, userName);
                    }
                }

                idNameMapReference.set(idNameMap);
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Gestisci eventuali errori di accesso al database
                latch.countDown(); // Assicurati che il latch venga comunque contato
            }
        });

        try {
            latch.await(); // Attendere finché i dati non sono stati recuperati da Firebase
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, String> idNameMap = idNameMapReference.get();
        if (idNameMap == null) {
            idNameMap = new HashMap<>(); // Inizializza la mappa vuota in caso di errore
        }

        return idNameMap;
    }


    public void getIdNameMapFromFirebaseSync() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Map<String, String>> idNameMapReference = new AtomicReference<>();

        DatabaseReference usersRef = databaseRef.child("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> idNameMap = new HashMap<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String userName = userSnapshot.getValue(String.class);

                    if (userId != null && userName != null) {
                        idNameMap.put(userId, userName);
                    }
                }

                idNameMapReference.set(idNameMap);
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Gestisci eventuali errori di accesso al database
                latch.countDown(); // Assicurati che il latch venga comunque contato
            }
        });

        try {
            latch.await(); // Attendere finché i dati non sono stati recuperati da Firebase
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

         idNameMap = idNameMapReference.get();
        if (idNameMap == null) {
            idNameMap = new HashMap<>(); // Inizializza la mappa vuota in caso di errore
        }




    }


    Map<String, String> idNameMap;

/*


    public void uploadApk(){
// Initialize Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

// Create a reference to the APK file in Firebase Storage
        StorageReference apkRef = storageRef.child("updates/your-updated-app.apk");

// Upload the APK file
        File apkFile = new File("path/to/your-local-apk.apk");
        Uri apkUri = Uri.fromFile(apkFile);
        UploadTask uploadTask = apkRef.putFile(apkUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // APK uploaded successfully
        }).addOnFailureListener(exception -> {
            // Handle the error
        });


    }
*/

}
