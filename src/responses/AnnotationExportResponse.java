package responses;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import objects.AnnotationExport;
import objects.Person;

import java.util.*;
import java.sql.*;

import com.google.gson.*;
import com.google.gson.stream.MalformedJsonException;

@Path("/AnnotationExport")
public class AnnotationExportResponse {

	public String executeQuery(String query, String type) throws SQLException{
		final String DB_URL="jdbc:mysql://mysql-db1.man.poznan.pl:3307/transcribathon?serverTimezone=CET";
		final String USER = "enrichingeuropeana";
		final String PASS = "Ke;u5De)u8sh";
		   List<AnnotationExport> annotationExports = new ArrayList<AnnotationExport>();
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
			  AnnotationExport annotationExport = new AnnotationExport();
			  annotationExport.setAnnotationId(rs.getInt("AnnotationId"));
			  annotationExport.setText(rs.getString("Text"));
			  annotationExport.setTimestamp(rs.getTimestamp("Timestamp"));
			  annotationExport.setX_Coord(rs.getFloat("X_Coord"));
			  annotationExport.setY_Coord(rs.getFloat("Y_Coord"));
			  annotationExport.setWidth(rs.getFloat("Width"));
			  annotationExport.setHeight(rs.getFloat("Height"));
			  annotationExport.setMotivation(rs.getString("Motivation"));
			  annotationExport.setItemId(rs.getString("ItemId"));
			  annotationExport.setStoryUrl(rs.getString("StoryUrl"));
			  annotationExport.setStoryId(rs.getString("StoryId"));
			  annotationExports.add(annotationExport);
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
	    String result = gsonBuilder.toJson(annotationExports);
	    return result;
	}

	//Get all Entries
	@Path("/all")
	@Produces("application/json;charset=utf-8")
	@POST
	public Response getAll() throws SQLException {
		String query = "SELECT * FROM (" + 
				"(SELECT  " + 
				"	 a.AnnotationId, " +
				"    a.Text, " + 
				"    a.Timestamp, " + 
				"    a.X_Coord, " + 
				"    a.Y_Coord, " + 
				"    a.Width, " + 
				"    a.Height, " + 
				"    m.Name AS Motivation, " + 
				"    i.ProjectItemId as ItemId, " + 
				"    s.ProjectStoryUrl as StoryUrl, " + 
				"    s.ProjectStoryId as StoryId " + 
				"FROM " + 
				"    Annotation a " + 
				"        LEFT JOIN " + 
				"    AnnotationType at ON a.AnnotationTypeId = at.AnnotationTypeId " + 
				"        LEFT JOIN " + 
				"    Motivation m ON at.MotivationId = m.MotivationId " + 
				"        LEFT JOIN " + 
				"    Item i ON i.ItemId = a.ItemId " + 
				"        LEFT JOIN " + 
				"    Story s ON s.StoryId = i.StoryId)  " + 
				"UNION ( " + 
				"	SELECT  " + 
				"	 t.TranscriptionId, " +
				"    t.Text, " + 
				"    t.Timestamp, " + 
				"    0 AS X_Coord, " + 
				"    0 AS Y_Coord, " + 
				"    0 AS Width, " + 
				"    0 AS Height, " + 
				"    'transcribing' AS Motivation, " + 
				"    i.ProjectItemId, " + 
				"    s.ProjectStoryUrl, " + 
				"    s.ProjectStoryId " + 
				"FROM " + 
				"    Transcription t " + 
				"        LEFT JOIN " + 
				"    Item i ON i.ItemId = t.ItemId " + 
				"        LEFT JOIN " + 
				"    Story s ON s.StoryId = i.StoryId " + 
				"WHERE " + 
				"    CurrentVersion = 1) "
				+ ") a WHERE 1";
		String resource = executeQuery(query, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
	
	
	//Search using custom filters
	@Path("/search")
	@Produces("application/json;charset=utf-8")
	@POST
	public Response search(@Context UriInfo uriInfo, String body) throws SQLException {
		JsonParser jsonParser = new JsonParser();
		JsonElement jsonTree = jsonParser.parse(body);
		JsonObject bodyObject = jsonTree.getAsJsonObject();
		String query = "SELECT * FROM (" + 
						"(SELECT  " + 
						"	 a.AnnotationId, " +
						"    a.Text, " + 
						"    a.Timestamp, " + 
						"    a.X_Coord, " + 
						"    a.Y_Coord, " + 
						"    a.Width, " + 
						"    a.Height, " + 
						"    m.Name AS Motivation, " + 
						"    i.ProjectItemId as ItemId, " + 
						"    s.ProjectStoryUrl as StoryUrl, " + 
						"    s.ProjectStoryId as StoryId " + 
						"FROM " + 
						"    Annotation a " + 
						"        LEFT JOIN " + 
						"    AnnotationType at ON a.AnnotationTypeId = at.AnnotationTypeId " + 
						"        LEFT JOIN " + 
						"    Motivation m ON at.MotivationId = m.MotivationId " + 
						"        LEFT JOIN " + 
						"    Item i ON i.ItemId = a.ItemId " + 
						"        LEFT JOIN " + 
						"    Story s ON s.StoryId = i.StoryId)  " + 
						"UNION ( " + 
						"	SELECT  " + 
						"	 t.TranscriptionId, " +
						"    t.Text, " + 
						"    t.Timestamp, " + 
						"    0 AS X_Coord, " + 
						"    0 AS Y_Coord, " + 
						"    0 AS Width, " + 
						"    0 AS Height, " + 
						"    'transcribing' AS Motivation, " + 
						"    i.ProjectItemId, " + 
						"    s.ProjectStoryUrl, " + 
						"    s.ProjectStoryId " + 
						"FROM " + 
						"    Transcription t " + 
						"        LEFT JOIN " + 
						"    Item i ON i.ItemId = t.ItemId " + 
						"        LEFT JOIN " + 
						"    Story s ON s.StoryId = i.StoryId " + 
						"WHERE " + 
						"    CurrentVersion = 1) " +
						") a WHERE 1";

		for(String key : bodyObject.keySet()){
			String[] values = bodyObject.get(key).toString().split(",");
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
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
}
