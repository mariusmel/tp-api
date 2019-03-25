package responses;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import objects.CompletionStatus;

import java.util.*;
import java.sql.*;

import com.google.gson.*;

@Path("/CompletionStatus")
public class CompletionStatusResponse {


	public String executeQuery(String query, String type) throws SQLException{
		final String DB_URL="jdbc:mysql://mysql-db1.man.poznan.pl:3307/transcribathon";
		final String USER = "enrichingeuropeana";
		final String PASS = "Ke;u5De)u8sh";
		   List<CompletionStatus> completionStatusList = new ArrayList<CompletionStatus>();
		   // Register JDBC driver
		   try {
			Class.forName("com.mysql.jdbc.Driver");
		
		   // Open a connection
		   Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		   // Execute SQL query
		   Statement stmt = conn.createStatement();
		   if (type != "Select") {
			   int success = stmt.executeUpdate(query);
			   if (success > 0) {
				   return type +" succesful";
			   }
			   else {
				   return type +" could not be executed";
			   }
		   }
		   ResultSet rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
		      //Retrieve by column name
			  CompletionStatus CompletionStatus = new CompletionStatus();
			  CompletionStatus.setCompletionStatusId(rs.getInt("CompletionStatusId"));
			  CompletionStatus.setName(rs.getString("Name"));
			  completionStatusList.add(CompletionStatus);
		   }
		
		   // Clean-up environment
		   rs.close();
		   stmt.close();
		   conn.close();
		   } catch(SQLException se) {
		       //Handle errors for JDBC
			   se.printStackTrace();
		   } catch (ClassNotFoundException e) {
			   e.printStackTrace();
		}
	    Gson gsonBuilder = new GsonBuilder().create();
	    String result = gsonBuilder.toJson(completionStatusList);
	    return result;
	}

	//Get all Entries
	@Path("/all")
	@GET
	public String getAll() throws SQLException {
		String query = "SELECT * FROM CompletionStatus WHERE 1";
		String resource = executeQuery(query, "Select");
		return resource;
	}
	

	//Add new entry
	@Path("/add")
	@POST
	public String add(String body) throws SQLException {	
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    CompletionStatus completionStatus = gson.fromJson(body, CompletionStatus.class);
	    
	    //Check if all mandatory fields are included
	    if (completionStatus.CompletionStatusId != null && completionStatus.Name != null) {
			String query = "INSERT INTO CompletionStatus (CompletionStatusId, Name) "
							+ "VALUES (" + completionStatus.CompletionStatusId
							+ ", '" + completionStatus.Name + "')";
			String resource = executeQuery(query, "Insert");
			return resource;
	    } else {
	    	return "Fields missing";
	    }
	}
	

	//Edit entry by id
	@Path("/{id}")
	@POST
	public String update(@PathParam("id") int id, String body) throws SQLException {
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    JsonObject  changes = gson.fromJson(body, JsonObject.class);
	    
	    //Check if field is allowed to be changed
	    if (changes.get("CompletionStatusId") != null) {
	    	return "Prohibited change attempt";
	    }
	    
	    //Check if NOT NULL field is attempted to be changed to NULL
	    if (changes.get("Name") == null || !changes.get("Name").isJsonNull()) {
		    String query = "UPDATE CompletionStatus SET ";
		    
		    int keyCount = changes.entrySet().size();
		    int i = 1;
			for(Map.Entry<String, JsonElement> entry : changes.entrySet()) {
			    query += entry.getKey() + " = " + entry.getValue();
			    if (i < keyCount) {
			    	query += ", ";
			    }
			    i++;
			}
			query += " WHERE CompletionStatusId = " + id;
			String resource = executeQuery(query, "Update");
			return resource;
	    } else {
	    	return "Prohibited change to null";
	    }
	}
	

	//Delete entry by id
	@Path("/{id}")
	@DELETE
	public String delete(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("DELETE FROM CompletionStatus WHERE CompletionStatusId = " + id, "Delete");
		return resource;
	}
	

	//Get entry by id
	@Path("/{id}")
	@GET
	public String getEntry(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("SELECT * FROM CompletionStatus WHERE CompletionStatusId = " + id, "Select");
		return resource;
	}

	//Search using custom filters
	@Path("/search")
	@GET
	public String search(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT * FROM Campaign WHERE 1";
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		
		for(String key : queryParams.keySet()){
			String[] values = queryParams.getFirst(key).split(",");
			query += " AND (";
		    int valueCount = values.length;
		    int i = 1;
		    for(String value : values) {
		    	query += key + " = " + value;
			    if (i < valueCount) {
			    	query += " OR ";
			    }
			    i++;
		    }
		    query += ")";
		}
		String resource = executeQuery(query, "Select");
		return resource;
	}
}


