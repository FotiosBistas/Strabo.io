require('dotenv').config();
const bcrypt = require("bcrypt");
const fs = require('fs');
const crypto = require('crypto');

// Replace this with your private key
const privateKey = crypto.randomBytes(32).toString('hex');

// Generate a random passphrase and IV
const passphrase = crypto.randomBytes(16).toString('hex');
const iv = crypto.randomBytes(16);

// Save the passphrase to the .env file
fs.writeFileSync('passphrase.txt', passphrase, 'utf-8');

const salt_rounds = 10;
const salt = bcrypt.genSaltSync(salt_rounds);
const hashed_passphrase = bcrypt.hashSync(passphrase, salt);

// Encrypt the private key using the passphrase and IV
const cipher = crypto.createCipheriv('aes-256-ctr', passphrase, iv);
let encryptedKey = cipher.update(privateKey, 'utf8', 'hex');
encryptedKey += cipher.final('hex');
fs.writeFileSync('.env', '');
// Save the encrypted key and IV to the .env file
fs.appendFileSync('.env', `enkey=${encryptedKey}\n`, 'utf-8');
fs.appendFileSync('.env', `iv=${iv.toString('hex')}\n`, 'utf-8');
fs.appendFileSync('.env', `pass=${hashed_passphrase}\n`, 'utf-8');
//change here for your mongo db password
fs.appendFileSync('.env', `mongopass=${'MQN5IGV2uTI0Pe0n\n'}`, 'utf-8');
//change here for your mongo db username
fs.appendFileSync('.env', `mongouser=${'Fotis\n'}`, 'utf-8'); 
