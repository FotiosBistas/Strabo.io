package gr.aueb.straboio.keyboard.storage;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Singleton class to Manage the training data entries.
 */
public class CollectedDataManager {

    private static CollectedDataManager instance;

    public synchronized static CollectedDataManager getInstance(){
        if(instance == null){
            instance = new CollectedDataManager();
        }
        return instance;
    }

    /**
     * Adds JSONObject of training data to the internal storage.
     * @param c the application context.
     * @param trainingData training pair (raw, translated) of sentence in JSONObject format.
     */
    public synchronized void add(Context c, JSONObject trainingData){
        File dir = new File(c.getFilesDir(), "cache");

        if(!dir.exists()){
            dir.mkdir();
        }

        try {
            File trainingDataFile = new File(dir, "training_data");
            if (!trainingDataFile.exists()) {
                trainingDataFile.createNewFile();
            }

            StringBuilder contentBuilder = new StringBuilder();

            // Load from file
            try (BufferedReader reader = new BufferedReader(new FileReader(trainingDataFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new Exception("Problem with reading file:\n"+e.getMessage());
            }

            JSONArray contentsJSON;

            if(contentBuilder.toString().isEmpty()){
                contentsJSON = new JSONArray();
            } else{
                contentsJSON = new JSONArray(contentBuilder.toString());
            }

            contentsJSON.put(trainingData);

            // Save back to file.
            try (FileWriter fileWriter = new FileWriter(trainingDataFile)) {
                fileWriter.write(contentsJSON.toString());
                fileWriter.flush();
            } catch (IOException e) {
                throw new Exception("Problem with writing file:\n"+e.getMessage());
            }

        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    /**
     * Retrieves all the sentences stored this far.
     * @param c the application context.
     * @return JSONObject sentences in JSONArray format.
     */
    public synchronized JSONArray retrieveData(Context c){
        File dir = new File(c.getFilesDir(), "cache");

        if(!dir.exists()){
            dir.mkdir();
        }

        JSONArray contentsJSON = null;

        try {

            File trainingDataFile = new File(dir, "training_data");
            if (!trainingDataFile.exists()) {
                trainingDataFile.createNewFile();
            }
            StringBuilder contentBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(trainingDataFile));
            // Load from file
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }

            } catch (Exception e) {
                System.out.println(e);
                throw new Exception("Problem with reading file:\n"+e.getMessage());
            }


            if(contentBuilder.toString().isEmpty()){
                contentsJSON = new JSONArray();
            } else{
                contentsJSON = new JSONArray(contentBuilder.toString());
            }

        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        return contentsJSON;
    }

    /**
     * Flushes (deletes) the stored sentences.
     * @param c the application context.
     */
    public synchronized void flush(Context c){
        File dir = new File(c.getFilesDir(), "cache");

        if(!dir.exists()){
            dir.mkdir();
        }

        try {
            File trainingDataFile = new File(dir, "training_data");

            if (!trainingDataFile.exists()) {
                trainingDataFile.createNewFile();
            }

            // Save back to file.
            try (FileWriter fileWriter = new FileWriter(trainingDataFile)) {
                fileWriter.write("");
                fileWriter.flush();
            } catch (IOException e) {
                throw new Exception("Problem with writing file:\n"+e.getMessage());
            }

        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}
