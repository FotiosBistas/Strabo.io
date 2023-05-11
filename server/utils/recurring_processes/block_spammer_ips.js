


let spammer_ips = new Map(); 

module.exports = {
  spammer_ips: spammer_ips
};


function log(text){
    let time = new Date(); 
    console.log("[" + time + "] " + " " + text)
}

const schedule = require('node-schedule');
const fs = require('fs');
const path = require('path');
// Schedule the findSpammers function to run every rate limiter time 
const job = schedule.scheduleJob('*/1 * * * *', findSpammers);
const insert_spammers_into_db = schedule.scheduleJob('*/1 * * * *',insertSpammersIntoDb)
const parent_dir = path.dirname(path.dirname(__dirname))
const mongo_db_interactions = require( parent_dir + "\\mongo_db_api\\mongo.js");



// Inserts spammers from the map into the database 
async function insertSpammersIntoDb() {
  log("Inserting spammers into the database");
  const spammerJSON = Object.fromEntries(spammer_ips);
  const dataArray = Object.entries(spammerJSON).map(([key, value]) => ({ IP: key, spammer: value }));

  filteredArray = []; 
  
  for(const element of dataArray){
    result = await mongo_db_interactions.isIPcontained(element.IP); 
    if(!result){
      filteredArray.push(element);
    }
  }


  mongo_db_interactions.insertData("UserData", "SpammerIPS", filteredArray);
}

// Function to read the access log file and find spammer IPs
function findSpammers() {

    log("Reading access log file to block spammers");
   // Read the access log file
    const accessLog = fs.readFileSync(parent_dir + '\\access.log', 'utf8');

    // Find IPs that made too many requests in a short period of time
    const ipCounts = {};
    const lines = accessLog.split('\n');
    for (const element of lines) {
        const fields = element.split(' ');
        const ip = fields[0];
        const status_code = fields[3];
        if (status_code === '429') {
            // Increment the count for this IP
            ipCounts[ip] = (ipCounts[ip] || 0) + 1;
            if (ipCounts[ip] >= 3 && !spammer_ips.has(ip)) {
                // Add the IP to the map if it has exceeded the threshold
                spammer_ips.set(ip, true);
                log(`Blocked IP due to spam: ${ip}`);
            }
        }
    } 
    // Clear the access log file
    fs.truncateSync(parent_dir + '/access.log', 0);
}
