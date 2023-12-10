package AuletteBlu.pingpongammorte.utils;

import static AuletteBlu.pingpongammorte.MainActivity.packageInfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import androidx.core.content.FileProvider;

//import com.github.javiersantos.appupdater.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class UpdateManager {
    private static final String GITHUB_OWNER = "AlexanderG84";
    private static final String GITHUB_REPO = "PingPongAmmorteRep";

    public static final String GITHUB_BRANCH = "Release";

    private Context context;
    private Retrofit retrofit;
    private GitHubApiService apiService;

    public UpdateManager(Context context) {
        this.context = context;

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(GitHubApiService.class);
    }

    public void checkForUpdates() {
        Call<GitHubRelease> call = apiService.getLatestRelease(GITHUB_OWNER, GITHUB_REPO,GITHUB_BRANCH);
        call.enqueue(new Callback<GitHubRelease>() {
            @Override
            public void onResponse(Call<GitHubRelease> call, Response<GitHubRelease> response) {
                if (response.isSuccessful()) {
                    GitHubRelease latestRelease = response.body();
                    if (latestRelease != null) {
                        String latestVersion = latestRelease.getTagName();
                        //PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        String currentVersion = packageInfo.versionName;

                        if (compareVersions(latestVersion, currentVersion) > 0) {
                            // Se è disponibile un aggiornamento, scaricalo e installalo
                            downloadAndInstallUpdate(latestRelease.getAssets().get(0).getBrowserDownloadUrl());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<GitHubRelease> call, Throwable t) {
                Log.e("UpdateManager", "Error checking for updates: " + t.getMessage());
            }
        });
    }

    private void downloadAndInstallUpdate(String downloadUrl) {
        Call<ResponseBody> call = apiService.downloadFile(downloadUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    // Scrivi il file APK nel file system esterno
                                    File apkFile = writeResponseBodyToDisk(response.body());
// Dopo aver scaricato l'APK, chiamalo con il percorso del file scaricato
                                    if(apkFile!=null)
                                        installApk(apkFile);
                                       // moveApkToPublicDirectory(apkFile);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    // Gestisci eventuali eccezioni
                                }
                            }
                        }).start();


                        // Avvia l'installazione
                    //    installApk(apkFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("UpdateManager", "Error downloading update: " + t.getMessage());
            }
        });
    }

    private File writeResponseBodyToDisk(ResponseBody body) throws IOException {
        File file;
        try {
            file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "app-update.apk");
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                }

                outputStream.flush();
                return file;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (Exception e) {
           // throw e;
            return null;
        }
    }

    private void installApk(File apkFile) {
        Log.e("InstallAPK", "Attempting to install APK");
        Uri apkUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", apkFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("InstallAPK", "Error installing APK: " + e.getMessage());
        }

    }


    private void moveApkToPublicDirectory(File apkFile) {
        File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File newFile = new File(publicDir, apkFile.getName());

        try {
            FileInputStream fis = new FileInputStream(apkFile);
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fis.close();
            fos.close();

            // Ora puoi utilizzare "newFile" per l'installazione
            installApk(newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private int compareVersions(String version1, String version2) {
        // Implementa la logica per confrontare le versioni (ad esempio, 1.0.0 vs 1.1.0).
        // Restituisci 0 se le versioni sono uguali, un numero positivo se la versione1 è maggiore della versione2,
        // e un numero negativo se la versione1 è minore della versione2.
        return version1.compareTo(version2);
    }

    public class GitHubRelease {
        @SerializedName("tag_name")
        private String tagName;

        @SerializedName("assets")
        private List<GitHubAsset> assets;

        public String getTagName() {
            return tagName;
        }

        public List<GitHubAsset> getAssets() {
            return assets;
        }
    }

    public class GitHubAsset {
        @SerializedName("browser_download_url")
        private String browserDownloadUrl;

        public String getBrowserDownloadUrl() {
            return browserDownloadUrl;
        }
    }
}
