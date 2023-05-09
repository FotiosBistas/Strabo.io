


let spammer_ips = new Map(); 

module.exports = {
  spammer_ips: spammer_ips
};

const schedule = require('node-schedule');
const fs = require('fs');
const path = require('path');
// Schedule the findSpammers function to run every rate limiter time 
const job = schedule.scheduleJob('*/1 * * * *', findSpammers);
const insert_spammers_into_db = schedule.scheduleJob('*/1 * * * *',insertSpammersIntoDb)

const parent_dir = path.dirname(path.dirname(__dirname))


function insertSpammersIntoDb(){

}

// Function to read the access log file and find spammer IPs
function findSpammers() {

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
                console.log(`Blocked IP: ${ip}`);
            }
        }
    } 
    // Clear the access log file
    fs.truncateSync(parent_dir + '/access.log', 0);
}
