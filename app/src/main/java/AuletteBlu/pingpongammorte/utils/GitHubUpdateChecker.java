package AuletteBlu.pingpongammorte.utils;

import static AuletteBlu.pingpongammorte.utils.UpdateManager.GITHUB_BRANCH;

import com.github.javiersantos.appupdater.BuildConfig;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GitHubUpdateChecker {
    private static final String GITHUB_BASE_URL = "https://api.github.com/";
    private static final String GITHUB_OWNER = "AlexanderG84";
    private static final String GITHUB_REPO = "pingpongAmmorte";

    public void checkForUpdates() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GITHUB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GitHubApiService apiService = retrofit.create(GitHubApiService.class);

        Call<UpdateManager.GitHubRelease> call = apiService.getLatestRelease(GITHUB_OWNER, GITHUB_REPO,GITHUB_BRANCH);

        call.enqueue(new Callback<UpdateManager.GitHubRelease>() {
            @Override
            public void onResponse(Call<UpdateManager.GitHubRelease> call, Response<UpdateManager.GitHubRelease> response) {
                if (response.isSuccessful()) {
                    UpdateManager.GitHubRelease latestRelease = response.body();
                    String latestVersion = latestRelease.getTagName();
                    String currentVersion = BuildConfig.VERSION_NAME;

                    if (compareVersions(latestVersion, currentVersion) > 0) {
                        // Mostra una notifica all'utente e chiedi se desidera aggiornare.
                        // Se l'utente accetta, puoi aprire l'URL del tuo repository GitHub per il download.
                    }
                }
            }

            @Override
            public void onFailure(Call<UpdateManager.GitHubRelease> call, Throwable t) {
                // Gestisci l'errore in caso di problemi di connessione o di altre eccezioni.
            }
        });
    }

    private int compareVersions(String version1, String version2) {
        // Implementa la logica per confrontare le versioni (ad esempio, 1.0.0 vs 1.1.0).
        // Restituisci 0 se le versioni sono uguali, un numero positivo se la versione1 è maggiore della versione2,
        // e un numero negativo se la versione1 è minore della versione2.
        return version1.compareTo(version2);
    }
}
