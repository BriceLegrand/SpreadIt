package spreadit;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.*;

import spreadit.GcmException;
import spreadit.SqlHandler;

import java.util.*;

import static java.lang.System.out;

@RestController
public class SpreadItController {
	
	private static Map<Integer, String> gcm_ids = new HashMap<Integer, String>();
    private static int current_index = 0;
    // return server_id
	private static synchronized int add_gcm_id(String gcm_id) {
        for (Map.Entry<Integer, String> entry : gcm_ids.entrySet()) {
            if (entry.getValue().equals(gcm_id)) {
                return entry.getKey();
            }
        }
        current_index++;
        gcm_ids.put(current_index, gcm_id);
        return current_index;
	}
    private static synchronized void remove_gcm_id(int server_id) {
        gcm_ids.remove(server_id);
    }
    private static synchronized void remove_gcm_id(String gcm_id) {
        for (Map.Entry<Integer, String> entry : gcm_ids.entrySet()) {
            if (entry.getValue().equals(gcm_id)) {
                gcm_ids.remove(entry.getKey());
                return;
            }
        }
    }
    private static synchronized String get_gcm_id(int server_id) {
        return gcm_ids.get(server_id);
    }
	
    @RequestMapping("/")
    @ResponseBody
    public String index() {
        return "SpreadIt server is working.";
    }

    // Returns the server ID
	@RequestMapping(value="/login", method=RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam("gcm_id") String gcm_id) {
        //String server_id = Integer.toString(add_gcm_id(gcm_id));
		String server_id = Integer.toString(SqlHandler.login(gcm_id));
        out.println("/login : saved gcm_id="+gcm_id+" with server_id="+server_id);
        return server_id;
    }
	
	@RequestMapping(value="/logout", method=RequestMethod.POST)
    @ResponseBody
    public String logout(@RequestParam("server_id") int server_id) {
		SqlHandler.logout(server_id);
        out.println("/logout : server_id="+server_id+" logged out");
        return "server_id="+server_id+" logged out";
    }

	@RequestMapping(value="/position", method=RequestMethod.POST)
    @ResponseBody
    public String update_position(@RequestParam("server_id") int server_id,
    		@RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude) {
		
		//TODO notify other users in the zone that they should update their list
		String result;
		try {
			SqlHandler.update_location(server_id, latitude, longitude);
	        out.println("/position : server_id="+server_id+" position updated");
			result = "/position : server_id="+server_id+" position updated";
		} catch (TtlSqlException e) {
	        out.println("/position : server_id="+server_id+" "+e.getMessage());
			result = e.getMessage();
		}
        return result;
    }

	//TODO /reset_ttl
	//TODO /users -> retrieve users within distance -> update everyone
	
	//TODO rewrite
    @RequestMapping(value="/send", method=RequestMethod.POST)
    @ResponseBody
    public String send(@RequestParam("server_id") int server_id, @RequestParam("msg") String msg) {
        String gcm_id = get_gcm_id(server_id);
        if (gcm_id==null) {
            out.println("/send : Failure with server_id="+server_id+" not logged in (<=>no gcm_id saved)");
            return "Failure : server_id="+server_id+" not logged in (<=>no gcm_id saved)";
        }


        try {
            sendMsgToGcm(gcm_ids.values(), msg);
            out.println("/send : Successfully msg=\""+msg+"\" sent");
            return "Successfully msg=\""+msg+"\" sent";
        }
        catch (GcmException e) {
            out.println("Exception at SpreadItController.sendMsgToGcm static method :");
            out.println(e.getMessage());
            out.println("/send : Failure of the HTTP response from GCM servers");
            return "Failure of the HTTP response to GCM servers";
        }
        catch (Exception e) {
            e.printStackTrace();
            out.println("/send : Failure of the HTTP request to GCM servers");
            return "Failure of the HTTP request to GCM servers";
        }

    }


    private static void sendMsgToGcm(Collection<String> gcm_ids, String msg) throws Exception {
        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();

            HttpPost post = new HttpPost("https://android.googleapis.com/gcm/send");
            post.setHeader("Authorization", "key=AIzaSyCSY51ac5k331oQlLHFwjkaf6IDzIj1YhU");
            post.setHeader("Content-Type", "application/json");

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{ \"registration_ids\" : [");
            for (String gcm_id : gcm_ids) {
                jsonBuilder.append("\"");
                jsonBuilder.append(gcm_id);
                jsonBuilder.append("\",");
            }
            jsonBuilder.setLength(jsonBuilder.length() - 1); // remove last comma
            jsonBuilder.append("], \"data\": { \"msg\": \"");
            jsonBuilder.append(msg);
            jsonBuilder.append("\" }, \"time_to_live\":300");
            jsonBuilder.append("}");

            String json = jsonBuilder.toString();
            HttpEntity entity = new ByteArrayEntity(json.getBytes("UTF-8"));
            post.setEntity(entity);

            CloseableHttpResponse response = client.execute(post);
            out.println("Request sent to GCM with body=" + json);
            try {
                if (!response.getStatusLine().toString().contains("200")) {
                    throw new GcmException("HTTP response : statusLine=" + response.getStatusLine().toString() +
                            "\nbody=" + EntityUtils.toString(response.getEntity()));
                }
            }
            catch (Exception e) {
                throw e;
            }
            finally {
                response.close();
            }
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            if (client!=null)
                client.close();
        }
    }
}
