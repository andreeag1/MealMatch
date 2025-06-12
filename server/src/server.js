const express = require("express");
const http = require("http");
const cors = require("cors");
const morgan = require("morgan");
const apiRoutes = require("./api/routes");
const connectDB = require("./config/database");
const setupWebSocket = require("./api/websocket/websocket");
const { port } = require("./config");

connectDB();

const app = express();
const server = http.createServer(app);

app.use(cors());
app.use(express.json());
app.use(morgan("dev"));

//API Routes
app.use("/api", apiRoutes);

// Setup WebSocket server
setupWebSocket(server);

server.listen(port, () => {
  console.log(`Server is running on port ${port}`);
  console.log("WebSocket server is listening on ws://localhost:3000/ws");
});
