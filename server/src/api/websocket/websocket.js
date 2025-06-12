const Ws = require("ws");
const jwt = require("jsonwebtoken");
const Message = require("../models/messageModel");
const { jwtSecret } = require("../../config/index");
const User = require("../models/userModel");

const rooms = new Map();

function setupWebSocket(server) {
  const wss = new Ws.Server({ server, path: "/ws" });

  wss.on("connection", async (ws, req) => {
    const urlParams = new URLSearchParams(req.url.slice(req.url.indexOf("?")));
    const token = urlParams.get("token");

    if (!token) {
      ws.close(1008, "Token required");
      return;
    }

    try {
      console.log(token, jwtSecret);
      const decoded = jwt.verify(token, jwtSecret);
      const user = await User.findById(decoded.id);
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
            username: ws.username,
            content: content,
            createdAt: newMessage.createdAt,
          };

          // Broadcast to all clients in the room
          if (rooms.has(roomId)) {
            rooms.get(roomId).forEach((client) => {
              if (client.readyState === Ws.OPEN) {
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

module.exports = setupWebSocket;
