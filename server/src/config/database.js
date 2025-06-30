import { connect } from "mongoose";

const connectDB = async () => {
  try {
    // Mongoose connection logic
    const mongoURI = process.env.MONGO_URI;
    await connect(mongoURI);
    console.log("MongoDB connected successfully.");
  } catch (error) {
    console.error("MongoDB connection failed:", error.message);
    // Exit process with failure
    process.exit(1);
  }
};

export default connectDB;
