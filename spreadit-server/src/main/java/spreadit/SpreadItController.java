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
        try {
            SqlHandler.create_tables();
        } catch (Exception e) {
            out.println(e.getMessage());
            out.println("/ : exception !");
            return "/ : exception !";
        }
        return "SpreadIt server is working.";
    }

    // Returns the server ID
	@RequestMapping(value="/login", method=RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam("gcm_id") final String gcm_id) {
        String result = "";
        try {
            result = Integer.toString(SqlHandler.login(gcm_id));
            out.println("/login : saved gcm_id="+gcm_id+" with server_id="+result);
        } catch (Exception e) {
            result = "/login : exception for gcm_id="+gcm_id;
            out.println(e.getMessage());
            out.println(result);
        }
        return result;
    }
	
	@RequestMapping(value="/logout", method=RequestMethod.POST)
    @ResponseBody
    public String logout(@RequestParam("server_id") final int server_id) {
        // send the server_id of this user to other connected users in the zone
        List<User> users;
        try {
            users = SqlHandler.retrieve_users(server_id, Application.rayon_diffusion_km);
            sendMsgToGcm(users, "LOSTUSER|"+server_id);
        }
        catch (Exception e) {
            out.println("/logout : LOSTUSER update of other users failed "+e.getMessage()+" for server_id="+server_id);
        }

        String result = "";
        try {
            SqlHandler.logout(server_id);
            result = "server_id="+server_id+" logged out";
            out.println("/logout : "+result);
        } catch (Exception e) {
            result = "exception for server_id="+server_id;
            out.println(e.getMessage());
            out.println("/logout : "+result);
        }
        return result;
    }

	@RequestMapping(value="/position", method=RequestMethod.POST)
    @ResponseBody
    public String update_position(@RequestParam("server_id") final int server_id,
    		@RequestParam("latitude") final double latitude, @RequestParam("longitude") final double longitude) {
		
		String result;
		try {
			SqlHandler.update_location(server_id, latitude, longitude);
	        out.println("/position : server_id="+server_id+" position updated");
			result = "server_id="+server_id+" position updated";
		} catch (TtlSqlException e) {
	        out.println("/position : server_id="+server_id+" "+e.getMessage());
			return "ttl exception when updating position";
		} catch (Exception e) {
            out.println("/position : server_id="+server_id+" "+e.getMessage());
            return "exception when updating position, that is not a ttl exception";
        }

        // send the server_id of this user to other connected users in the zone
        List<User> users;
        try {
            users = SqlHandler.retrieve_users(server_id, Application.rayon_diffusion_km);
            sendMsgToGcm(users, "NEWUSER|"+server_id);
        }
        catch (Exception e) {
            out.println("/position : NEWUSER update of other users failed "+e.getMessage()+" for server_id="+server_id);
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
        } catch (Exception e) {
            out.println("/reset_ttl : server_id="+server_id+" "+e.getMessage());
            return "exception when resetting ttl, that is not a ttl exception";
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
        } catch (Exception e) {
            out.println("/users : server_id="+server_id+" "+e.getMessage());
            return "exception when fetching users, that is not a ttl exception";
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
        } catch (Exception e) {
            out.println("/reset_ttl : server_id="+server_id+" "+e.getMessage());
            return "exception when resetting ttl, that is not a ttl exception";
        }

        try {
            sendMsgToGcm(SqlHandler.retrieve_users(server_id, Application.rayon_diffusion_km), server_id+"|"+msg);
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
