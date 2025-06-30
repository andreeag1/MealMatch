import { WebSocketServer, WebSocket } from "ws";
import jwt from "jsonwebtoken";
import Message from "../models/messageModel.js";
import User from "../models/userModel.js";
import config from "../../config/index.js";

const { jwtSecret } = config;

const rooms = new Map();

function setupWebSocket(server) {
  const wss = new WebSocketServer({ server, path: "/ws" });

  wss.on("connection", async (ws, req) => {
    const urlParams = new URLSearchParams(req.url.slice(req.url.indexOf("?")));
    const token = urlParams.get("token");

    if (!token) {
      ws.close(1008, "Token required");
      return;
    }

    try {
      const decoded = jwt.verify(token, jwtSecret);
      const user = await User.findById(decoded.id);

      if (!user) {
        ws.close(1008, "User not found");
        return;
      }

      ws.userId = user._id;
      ws.username = user.username;

      ws.on("message", async (message) => {
        const data = JSON.parse(message);

        // Joining a room
        if (data.type === "join") {
          const { roomId } = data;
          if (!rooms.has(roomId)) {
            rooms.set(roomId, new Set());
          }
          rooms.get(roomId).add(ws);
          ws.roomId = roomId; // Associate ws connection with a room
          console.log(`${ws.username} joined room ${roomId}`);
        }

        //Broadcasting a message
        if (data.type === "message") {
          const { roomId, content } = data;

          // Save message to DB
          const newMessage = new Message({
            group: roomId,
            user: ws.userId,
            content,
          });
          await newMessage.save();

          const messageToSend = {
            userId: ws.userId.toString(),
            username: ws.username,
            content: content,
            createdAt: newMessage.createdAt,
          };

          // Broadcast to all clients in the room
          if (rooms.has(roomId)) {
            rooms.get(roomId).forEach((client) => {
              if (client.readyState === WebSocket.OPEN) {
                client.send(JSON.stringify(messageToSend));
              }
            });
          }
        }
      });

      ws.on("close", () => {
        // Remove client from the room on disconnect
        if (ws.roomId && rooms.has(ws.roomId)) {
          rooms.get(ws.roomId).delete(ws);
          console.log(`${ws.username} left room ${ws.roomId}`);
        }
      });
    } catch (err) {
      console.error("WebSocket Authentication Error:", err.name, err.message);
      ws.close(1008, "Invalid token");
    }
  });
}

export default setupWebSocket;
