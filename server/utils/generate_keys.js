const bcrypt = require("bcrypt");
const fs = require('fs');
const crypto = require('crypto');

// Replace this with your private key
const privateKey = crypto.randomBytes(32).toString('hex');

// Generate a random passphrase and IV
const passphrase = crypto.randomBytes(32);
const iv = crypto.randomBytes(16);

fs.writeFileSync('passphrase.txt', passphrase.toString('hex'));
const salt_rounds = 10; 
const salt = bcrypt.genSaltSync(salt_rounds); 
const hashed_passphrase = bcrypt.hashSync(passphrase, salt);

// Encrypt the private key using the passphrase and IV
const cipher = crypto.createCipheriv('aes-256-ctr', passphrase, iv);
let encryptedKey = cipher.update(privateKey, 'utf8', 'hex');
encryptedKey += cipher.final('hex');

// Save the encrypted key, passphrase, and IV to files
fs.writeFileSync('encryptedkey.txt', encryptedKey);
fs.writeFileSync('hashed_passphrase.txt', hashed_passphrase.toString('hex'));
fs.writeFileSync('iv.txt', Buffer.from(iv).toString('hex'));
