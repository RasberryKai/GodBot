import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import utils.apis.spotify.SpotApi;
import utils.customExceptions.LinkInterpretation.InvalidURLException;
import utils.customExceptions.LinkInterpretation.RequestException;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException, RequestException, InvalidURLException {
//        long start = System.currentTimeMillis();
//        long end = System.currentTimeMillis();
//        System.out.println(end - start);
        SpotApi apiObj = SpotApi.getObj();
    }
}
