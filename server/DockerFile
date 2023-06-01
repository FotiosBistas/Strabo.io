# Use an official Node.js runtime as the base image
FROM node:18

# Set the working directory in the Docker image
WORKDIR /usr/src/app

# Copy package.json and package-lock.json to the Docker image
COPY package*.json ./

# Install the dependencies in the Docker image
RUN npm install

# Copy the rest of your app's source code to the Docker image
COPY . .

# Expose the port that your app runs on
EXPOSE 443

# Define the command to run your app
CMD [ "node", "index.js" ]
