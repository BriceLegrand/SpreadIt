package spreadit;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static java.lang.System.out;

@RestController
public class SpreadItController {

    @RequestMapping("/")
    @ResponseBody
    public String index() {
        SqlHandler.create_tables();
        return "SpreadIt server is working.";
    }

    // Returns the server ID
	@RequestMapping(value="/login", method=RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam("gcm_id") final String gcm_id) {
		String server_id = Integer.toString(SqlHandler.login(gcm_id));
        out.println("/login : saved gcm_id="+gcm_id+" with server_id="+server_id);
        return server_id;
    }
	
	@RequestMapping(value="/logout", method=RequestMethod.POST)
    @ResponseBody
    public String logout(@RequestParam("server_id") final int server_id) {
		SqlHandler.logout(server_id);
        out.println("/logout : server_id="+server_id+" logged out");
        return "server_id="+server_id+" logged out";
    }

	@RequestMapping(value="/position", method=RequestMethod.POST)
    @ResponseBody
    public String update_position(@RequestParam("server_id") final int server_id,
    		@RequestParam("latitude") final double latitude, @RequestParam("longitude") final double longitude) {
		
		//TODO notify other users in the zone that they should update their list
		String result;
		try {
			SqlHandler.update_location(server_id, latitude, longitude);
	        out.println("/position : server_id="+server_id+" position updated");
			result = "server_id="+server_id+" position updated";
		} catch (TtlSqlException e) {
	        out.println("/position : server_id="+server_id+" "+e.getMessage());
			result = e.getMessage();
		}
        return result;
    }

    // Returns the server ID
    @RequestMapping(value="/reset_ttl", method=RequestMethod.POST)
    @ResponseBody
    public String reset_ttl(@RequestParam("server_id") final int server_id) {
        try {
            SqlHandler.reset_ttl_if_living(server_id);
        }
        catch (TtlSqlException e) {
            out.println("/reset_ttl : "+e.getMessage()+" for server_id="+server_id);
            return e.getMessage();
        }

        out.println("/reset_ttl : success for server_id="+server_id);
        return "Success reset_ttl for server_id="+server_id;
    }

    @RequestMapping("/users")
    @ResponseBody
    public String users(@RequestParam("server_id") final int server_id) {
        List<User> users;
        try {
            users = SqlHandler.retrieve_users(server_id, Application.rayon_diffusion_km);
        }
        catch (TtlSqlException e) {
            out.println("/users : "+e.getMessage()+" for server_id="+server_id);
            return e.getMessage();
        }

        StringBuilder usersBuilder = new StringBuilder();
        if(!users.isEmpty()) {
            for (User user : users) {
                usersBuilder.append(user.getServer_id());
                usersBuilder.append(",");
            }
            usersBuilder.setLength(usersBuilder.length() - 1); // remove last comma
        }

        out.println("/users : success for server_id="+server_id);
        return usersBuilder.toString();
    }
	
    @RequestMapping(value="/send", method=RequestMethod.POST)
    @ResponseBody
    public String send(@RequestParam("server_id") final int server_id, @RequestParam("msg") final String msg) {
        String gcm_id;
        try {
            gcm_id = SqlHandler.get_gcm_id(server_id);
        }
        catch (TtlSqlException e) {
            out.println("/users : "+e.getMessage()+" for server_id="+server_id);
            return e.getMessage();
        }

        try {
            sendMsgToGcm(SqlHandler.retrieve_users(server_id, Application.rayon_diffusion_km), msg);
            out.println("/send : Successfully msg=\""+msg+"\" sent");
            return "Successfully msg=\"" + msg + "\" sent";
        }
        catch (GcmException e) {
            out.println("Exception at SpreadItController.sendMsgToGcm static method :");
            out.println(e.getMessage());
            out.println("/send : Failure of the HTTP response from GCM servers");
            return "Failure of the HTTP response from GCM servers";
        }
        catch (TtlSqlException e) {
            out.println("/send : "+e.getMessage()+" for server_id="+server_id);
            return e.getMessage()+" for server_id="+server_id;
        }
        catch (Exception e) {
            e.printStackTrace();
            out.println("/send : Failure of the HTTP request to GCM servers");
            return "Failure of the HTTP request to GCM servers";
        }

    }


    private static void sendMsgToGcm(final Collection<User> users, final String msg) throws Exception {
        if (users==null || users.isEmpty()) return;

        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();

            HttpPost post = new HttpPost("https://android.googleapis.com/gcm/send");
            post.setHeader("Authorization", "key=AIzaSyCSY51ac5k331oQlLHFwjkaf6IDzIj1YhU");
            post.setHeader("Content-Type", "application/json");

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{ \"registration_ids\" : [");
            if (!users.isEmpty()) {
                for (User user : users) {
                    jsonBuilder.append("\"");
                    jsonBuilder.append(user.getGcm_id());
                    jsonBuilder.append("\",");
                }
                jsonBuilder.setLength(jsonBuilder.length() - 1); // remove last comma
            }
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
