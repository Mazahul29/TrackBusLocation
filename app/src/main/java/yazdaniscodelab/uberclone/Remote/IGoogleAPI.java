package yazdaniscodelab.uberclone.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Yazdani on 3/30/2018.
 */

public interface IGoogleAPI {

    @GET
    Call<String> getPath(@Url String  url);



}
