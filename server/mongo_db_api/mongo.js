const { MongoClient , ServerApiVersion} = require('mongodb'); 
const ObjectId = require('mongodb').ObjectId;
const uri = `mongodb+srv://${process.env.mongouser}:${process.env.mongopass}@dictionarycluster.mkmtbvi.mongodb.net/?retryWrites=true&w=majority`;
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
            log("Opened new database connection successfully");
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
     *  Retrieves all documents from dbname and collection 
     * that match the given query  
     * @param {*} dbname the database name  
     * @param {*} collection the collection we are going to retrieve the documents from  
     * @param {*} query the query that filters the documents chosen  
     * @param {*} projection receive the only data needed from the database  
     * @param {*} options extra options for the query behavior
     * @returns the documents matching the query 
     */
    retrieveData: async function(dbname, collectionName, query, projection, options){
        try{
            const cursor = await client.db(dbname).collection(collectionName).find(query,projection,options);
            const documents = await cursor.toArray(); 
            return documents;
        }catch(err){
            log("Error: " + err + "occured while trying to retrieve documents from database");
            return err; 
        }

    },

    /**
     *  Inserts the data given in the parameter into the database.  
     * @param {*} dbname database name to insert the data to 
     * @param {*} collection collection name to insert the data to 
     * @param {*} data data in the array form of a JSON format 
     */
    insertData: async function(dbname, collectionName, data){
        try{
            if(data.length == 0){
                log("Received empty data to insert");
                return 
            }
            const db = client.db(dbname);
            const coll = db.collection(collectionName);
            const result = await coll.insertMany(data);
            log("Documents inserted into db: " + result.count);
        }catch(err){
            log("Error while inserting documents into database: " + err);
        }    
    },
    

    isIPcontained: async function(ip) {
        try {
            const db = client.db('UserData');
            const collection = db.collection('SpammerIPS');

            const query = { IP: ip };
            const result = await collection.findOne(query);

            return result !== null; // Return true if the IP is contained in the database, false otherwise
        } catch (err) {
            log("Error while searching for IP in the database: " + err);
            return false; // Return false in case of an error
        }
    },

    /**
     * When server shutdowns it closes connection with the mongodb database. 
     */
    closeConnection: async function(){
        try{
            await client.close();
            log("Closed connection successfully");
        }catch(err){
            log("Couldn't close database connection");
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