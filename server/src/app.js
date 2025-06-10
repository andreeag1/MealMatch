const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
const apiRoutes = require("./api/routes");
const { port } = require("./config");
const connectDB = require("./config/database");

connectDB();

const app = express();

app.use(cors());
app.use(express.json());
app.use(morgan("dev"));

//API Routes
app.use("/api", apiRoutes);

// Root endpoint
app.get("/", (req, res) => {
  res.status(200).json({ message: "Welcome to the Express server!" });
});

app.listen(port, () => {
  console.log(`Server is running on port ${port}`);
});
