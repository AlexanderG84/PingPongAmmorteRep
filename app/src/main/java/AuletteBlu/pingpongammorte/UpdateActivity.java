package AuletteBlu.pingpongammorte;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class UpdateActivity extends AppCompatActivity {

    private static final String GITHUB_OWNER = "AlexanderG84";
    private static final String GITHUB_REPO = "PingPongAmmorteRep";
    private static final String APK_FILE_NAME = "pingpong.apk"; // Nome del tuo file APK
    private long downloadId;
    private DownloadManager downloadManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inizializza il gestore di download
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        // Registra un BroadcastReceiver per ricevere una notifica quando il download è completato
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        // Avvia il processo di download
        startDownload();
    }

    private void startDownload() {
        // Costruisci l'URL per il download del file APK dalla release più recente
        String downloadUrl = "https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO +
                "/releases/latest/download/" + APK_FILE_NAME;

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setTitle("Download dell'aggiornamento");
        request.setDescription("Scaricamento dell'aggiornamento in corso");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Imposta la destinazione del download (posizione in cui verrà salvato il file APK)
        request.setDestinationInExternalPublicDir("Download", APK_FILE_NAME);

        // Avvia il download
        downloadId = downloadManager.enqueue(request);
    }

    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == downloadId) {
                // Il download è stato completato, avvia l'installazione
                Uri apkUri = downloadManager.getUriForDownloadedFile(downloadId);

                if (apkUri != null) {
                    Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    installIntent.setData(apkUri);
                    installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(installIntent);
                }

                // Chiudi questa attività dopo l'avvio dell'installazione
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Deregistra il BroadcastReceiver
        unregisterReceiver(onDownloadComplete);
    }
}
