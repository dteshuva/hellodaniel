package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;




public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File baseDir;
    private Gson gson;
    public DocumentPersistenceManager(File baseDir){
        gson=new Gson();
        if (baseDir == null) {
            this.baseDir = new File(System.getProperty("user.dir"));
        }
        else {
            this.baseDir = baseDir;
        }
    }
    private class DocumentSerializer implements JsonSerializer<Document> {
        @Override
        public JsonElement serialize(Document document, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            String docTxt = document.getDocumentTxt();
            URI uri = document.getKey();   
            Map<String,Integer> wordCounter=document.getWordMap();
            String map=gson.toJson(wordCounter);
            
            jsonObject.addProperty("uri", gson.toJson(uri));
            jsonObject.addProperty("map", map);
            if(docTxt!=null){
                jsonObject.addProperty("text", gson.toJson(docTxt));
            }
            else{
                jsonObject.addProperty("byte", gson.toJson(DatatypeConverter.printBase64Binary(document.getDocumentBinaryData())));
            }
            return jsonObject;
        }
    }
    @Override
    public void serialize(URI uri, Document val) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        if(val==null){
            return;
        }
        String filePath=pathCreator(uri);
        DocumentSerializer ser=new DocumentSerializer();
        JsonElement jsonEl=ser.serialize(val, Document.class, null);
        File file=new File(filePath);
        file.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(file);
        writer.write(gson.toJson(val));
        writer.close();
    }
    private class DocumentDeserializer implements JsonDeserializer<Document> {

        @Override
        public Document deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            URI uri=null;
                uri = gson.fromJson(jsonObject.get("uri"), URI.class);
            Map<String, Integer> wordMap = gson.fromJson(jsonObject.get("map"), Map.class);
            if(jsonObject.get("text")==null){
                byte[]arr=gson.fromJson(jsonObject.get("byte"), byte[].class);
                Document d=new DocumentImpl(uri, arr);
                d.setWordMap(wordMap);
                return d;
            }
            String text=gson.fromJson(jsonObject.get("text"), String.class);
             Document d=new DocumentImpl(uri, text);
             d.setWordMap(wordMap);
            return d;
        }
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("uri was null");
        }
        String filePath=pathCreator(uri);
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
         System.out.println("stops here: "+filePath);
            return null;
        }
        FileReader reader = new FileReader(file);
        JsonElement json = JsonParser.parseReader(reader);
        DocumentDeserializer des=new DocumentDeserializer();
        Document doc = des.deserialize(json, Document.class, null);
        System.out.println(doc.getKey()+" "+doc.getDocumentTxt());
        file.delete();
        reader.close();
        this.delete(uri);
        return doc;
    }
    private String pathCreator(URI uri){
        String host = uri.getHost();
        String path = uri.getPath();
        String main=host+path+".json";
        String filePath=this.baseDir+File.separator;
        int leng=filePath.length();
        filePath=filePath.substring(0,leng)+"/"+main;
      /*  System.out.println("*******");
        System.out.println(filePath);
        System.out.println(this.baseDir+uri.toString().replace("http:/","")+".json");
        System.out.println("*******"); */
        return filePath;
    }
    @Override
    public boolean delete(URI uri) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("uri was null");
        }
        String host = uri.getHost();
        String path = uri.getPath();
        
        String filePath=this.baseDir.getAbsolutePath()+File.separator;
        if (host == null) {
            filePath = path + ".json";
        }
        else if(path==null){
            filePath=host+".json";
        }
        else {
            filePath =  host + path + ".json";
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        file.delete();
        return true;
    }
}