package AuletteBlu.pingpongammorte.utils;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query; // Aggiungi questa importazione
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface GitHubApiService {
    @GET("repos/{owner}/{repo}/releases/latest")
    @Headers("Accept: application/vnd.github.v3+json")
    Call<UpdateManager.GitHubRelease> getLatestRelease(@Path("owner") String owner, @Path("repo") String repo, @Query("branch") String branchName);

    @GET
    @Streaming
    Call<ResponseBody> downloadFile(@Url String fileUrl);
}
