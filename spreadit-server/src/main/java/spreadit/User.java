package spreadit;

/**
 * Created by greyna on 04/12/2014.
 */
public class User {

    private final int server_id;
    private final String gcm_id;

    public User(int server_id, String gcm_id) {
        this.server_id = server_id;
        this.gcm_id = gcm_id;
    }

    public String getGcm_id() {
        return gcm_id;
    }

    public int getServer_id() {
        return server_id;
    }

}
