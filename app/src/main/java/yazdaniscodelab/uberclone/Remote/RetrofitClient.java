package yazdaniscodelab.uberclone.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Yazdani on 3/30/2018.
 */

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseURl)
    {
        if (retrofit==null){

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

        }

        return retrofit;
    }


}
