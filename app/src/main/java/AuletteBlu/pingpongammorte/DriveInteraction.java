package AuletteBlu.pingpongammorte;

import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class DriveInteraction {

    //private  DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    private DatabaseReference databaseRef;

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

    public  void readFirebaseData() {
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
    }

    public  void uploadText(String text) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("data", text);
        updates.put("lastModified", ServerValue.TIMESTAMP);

        databaseRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> Log.e("Firebase Write", "Dati caricati con successo"))
                .addOnFailureListener(e -> Log.e("Firebase Write", "Errore durante il caricamento dei dati", e));
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
        return jsonRef.get();
    }

    public  Long getLastModifiedSynchronously()  {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Long> lastModifiedRef = new AtomicReference<>();

        DatabaseReference lastModifiedDbRef = databaseRef.child("lastModified");
        lastModifiedDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lastModifiedRef.set(dataSnapshot.getValue(Long.class));
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
        return lastModifiedRef.get();
    }


}
