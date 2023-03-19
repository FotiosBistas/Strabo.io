const { MongoClient , ServerApiVersion} = require('mongodb'); 
const ObjectId = require('mongodb').ObjectId;
const uri = "mongodb+srv://Fotis:MQN5IGV2uTI0Pe0n@dictionarycluster.mkmtbvi.mongodb.net/?retryWrites=true&w=majority";
//hi
function log(text){
    var time = new Date();
    console.log("[" + time.toLocaleTimeString() + "] " + text);
}

const options = {
    useNewUrlParser:true, 
    useUnifiedTopology:true, 
    serverSelectionTimeoutMS: 5000,
    serverApi: ServerApiVersion.v1
};

// Create a new MongoClient instance
const client = new MongoClient(uri, options);


module.exports = {

    connectToDatabase: async function(){
        try{
            await client.connect();
            log("Opened new connection successfully");
        }catch(err){
            log("Couldn't connect to database: " + err);
        }    
    },

    /**
     * Creates a mongodb index based on the date given as input.
     * Mongodb indexes are created based on date. Can be used alternatively 
     * with the timestamp index.  
     * @param {*} timestamp a date or a string that represents a date.  
     * @returns the constructed BJSON object ID.  
     */
    objectIdWithTimestamp: function(timestamp) {
        /* Convert string date to Date object (otherwise assume timestamp is a date) */
        if (typeof(timestamp) == 'string') {
            timestamp = new Date(timestamp);
        }

        /* Convert date object to hex seconds since Unix epoch */
        var hexSeconds = Math.floor(timestamp/1000).toString(16);

        /* Create an ObjectId with that hex timestamp */
        var constructedObjectId = new ObjectId(hexSeconds + "0000000000000000");

        return constructedObjectId
    },



    /**
     * Retrieves data based on the date given as a parameter. 
     * This will probably be used to train the model.  
     * @param {*} date 
     */
    retrieveDataBasedOnDate: async function(date){
        const result = await client.db('UserData').collection('Translated_and_non').find({ _id: { $lt: this.objectIdWithTimestamp(date) } }).explain();
        //TODO PROCESS RESULT 
        log(JSON.stringify(result))
    },


    closeConnection: async function(){
        try{
            await client.close();
            log("Closed connection successfully");
        }catch(err){
            log("Couldn't close database connection");
        }
    },

    addBitmapToDatabase: async function(bitmaps){
        
        try{                    
            const result = await client.db("UserData").collection("Bitmaps").insertMany(bitmaps); 
            log("Documents inserted: " + result.insertedCount);
        } catch (err) {
            log("Documents were not inserted into the database error: " + err);
        } 
    },

    /**
     * Inserts the metadata and the batch content inside the database.  
     * @param {*} database_structs batch content received from POST batch request.  
     */
    addStructToDatabase: async function(database_structs){
        try {
            const result = await client.db("UserData").collection("Translated_and_non").insertMany(database_structs);
            log("Documents inserted: " + result.insertedCount);
        } catch (err) {
            log("Documents were not inserted into the database error: " + err);
        } 
    }  

}