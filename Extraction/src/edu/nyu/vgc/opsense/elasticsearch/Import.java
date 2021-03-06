package edu.nyu.vgc.opsense.elasticsearch;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;


public class Import {
	
	public String source;
	public String index;
	public String type;
	public int skip;
	public int limit;
	
	public static void main(String [] args){
		System.out.println("Starting...");
		Import imp = new Import();
		if(args.length < 4){
			System.out.println("params: source index skip limit");
		}
		imp.source = args[0];
		imp.index = args[1];
		imp.skip = Integer.parseInt(args[2]);
		imp.limit = Integer.parseInt(args[3]);
		imp.type = "documents";
		System.out.println("Runing for: ");
		System.out.println(" " + imp.source);
		System.out.println(" " + imp.index);
		System.out.println(" " + imp.skip);
		System.out.println(" " + imp.limit);
		System.out.println(" " + imp.type);
		imp.go();
	}
	
	public String fixDate(String sDate){
		Date dateD = null;
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		DateFormat dof = new SimpleDateFormat("yyy-MM-dd");
		
		try {
			dateD = df.parse(sDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dof.format(dateD);
	}
	
	public void go(){
            System.out.println("go");
        Settings settings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name", "opsensedb")
                    .put("shield.user", "es_admin:NYU2015")
                .build();
        
		@SuppressWarnings("resource")
		Client client = new TransportClient(settings)
    		.addTransportAddress(new InetSocketTransportAddress("128.238.182.64", 9300));
		
                
                System.out.println(client.settings().getAsMap());
                
		Integer[] count = new Integer[1];
		count[0] = 0;
		Path path = Paths.get(this.source); 
		
		try (Stream<String> lines = Files.lines(path, Charset.defaultCharset())) {
			  lines.skip(this.skip).limit(this.limit).forEach(line -> {
				  
			  	JsonReader jsonReader = Json.createReader(new StringReader(line));
				JsonObject object = jsonReader.readObject();
				jsonReader.close();
				
				count[0]++;
				
				System.out.println(count[0]);
				
				client.prepareIndex(this.index, this.type)
					.setSource(object.toString())
					.setId(object.getJsonObject("document").get("id").toString())
					.execute()
					.actionGet();
				
			  });
		} catch (Exception ex){
			ex.printStackTrace();
			System.err.println("Line: " + count[0] + "\n" + ex.getMessage());
		}
		System.out.println("done");
		client.close();
	}
	
	public void printJson(JsonObject obj){
		 Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        JsonWriter jsonWriter = writerFactory.createWriter(System.out);
        jsonWriter.writeObject(obj);
        
	}
	
	public void printJson(JsonArray obj){
		obj.forEach(o -> printJson((JsonObject)o));
	}
}
























