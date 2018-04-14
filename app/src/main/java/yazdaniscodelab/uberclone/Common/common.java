package yazdaniscodelab.uberclone.Common;

import yazdaniscodelab.uberclone.Remote.IGoogleAPI;
import yazdaniscodelab.uberclone.Remote.RetrofitClient;

/**
 * Created by Yazdani on 3/30/2018.
 */

public class common {

    public static final String baseURL="https://maps.googleapis.com";

    public static IGoogleAPI getGoogleAPI(){

        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);

    }

}
