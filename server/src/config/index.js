require("dotenv").config();

const config = {
  port: process.env.PORT,
  mongoURI: process.env.MONGO_URI,
  jwtSecret: process.env.JWT_SECRET,
  jwtExpiresIn: process.env.JWT_EXPIRES_IN,
};

// Validate that all required environment variables are set
for (const key in config) {
  if (!config[key]) {
    throw new Error(`FATAL ERROR: Environment variable ${key} is not defined.`);
  }
}

module.exports = config;
