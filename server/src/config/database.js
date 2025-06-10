const mongoose = require("mongoose");
const { MONGO_URI } = process.env;

const connectDB = async () => {
  try {
    // Mongoose connection logic
    await mongoose.connect(MONGO_URI);
    console.log("MongoDB connected successfully.");
  } catch (error) {
    console.error("MongoDB connection failed:", error.message);
    // Exit process with failure
    process.exit(1);
  }
};

module.exports = connectDB;
