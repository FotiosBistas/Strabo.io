package gr.aueb.straboio.model;

import android.content.Context;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Vocabulary {
    public static final String PUNCTUATION = "!\"#$%&'()*+,-./:;<=>?@[\\\\]^_`{|}~";
    public static final String WHITESPACE = " \\t\\n\\r\\x0b\\x0c";

    private Map vocab;
    private Map indexToToken;
    private int currIndex;

    public Vocabulary() {
        this.vocab = new HashMap<String, Integer>();
        this.indexToToken = new HashMap<Integer, String>();
        currIndex = 0;
    }

    public int index(String token){
        if(this.vocab.containsKey(token)){
            return (int) this.vocab.get(token);
        } else {
            return this.vocab.size();
        }
    }

    public void insertToken(String token){
        if(!this.vocab.containsKey(token)){
            this.vocab.put(token, currIndex);
            this.indexToToken.put(currIndex, token);
            currIndex++;
        }
    }

    public String token(int index){
        return (String) this.indexToToken.get(index);
    }

    public void load(String vocab_file_name){
        try {
            File f = new File(vocab_file_name);

            // Create a Scanner object to read from the file
            Scanner scanner = new Scanner(f);

            // Loop all the lines of the file to a single string
            StringBuilder text = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                text.append(line).append("\n");
            }
            // Close the Scanner object
            scanner.close();

            // Start extracting the keys and values:
            // Split on '‽' (row delimiter).
            String[] lines = text.toString().split("‽");
            for(String line : lines){
                try{
                    // Split on '⁂' key-value delimiter.
                    String[] parts = line.split("⁂");
                    if(parts.length == 2){
                        // Load vocabulary:
                        this.vocab.put(parts[0], Integer.parseInt(parts[1]));
                        this.indexToToken.put(Integer.parseInt(parts[1]), parts[0]);
                    }
                } catch(Exception e){
                    Log.e("ERR_LOAD_VOCAB", "Error: "+e);
                }
            }
        } catch (IOException e) {
            Log.d("ERR_IO_VOCAB", "Error: "+e);
        }
    }

    private static String getAssetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }
        try (InputStream is = context.getAssets().open(assetName) ) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
}