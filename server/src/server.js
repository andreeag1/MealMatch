import config from "./config/index.js";
const { port } = config;

import express, { json } from "express";
import { createServer } from "http";
import cors from "cors";
import morgan from "morgan";
import apiRoutes from "./api/routes/index.js";
import connectDB from "./config/database.js";
import setupWebSocket from "./api/websocket/websocket.js";

connectDB();

const app = express();
const server = createServer(app);

app.use(cors());
app.use(json());
app.use(morgan("dev"));

//API Routes
app.use("/api", apiRoutes);

// Setup WebSocket server
setupWebSocket(server);

server.listen(port, () => {
  console.log(`Server is running on port ${port}`);
  console.log("WebSocket server is listening on ws://localhost:3000/ws");
});
