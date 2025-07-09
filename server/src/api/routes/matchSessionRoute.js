import express from "express";
import {
  createMatchSession,
  submitSwipes,
  createSoloMatchSession,
  getSessionResult,
} from "../controllers/matchSessionController.js";
import authMiddleware from "../middlewares/authMiddleware.js";

const router = express.Router();

router.use(authMiddleware);

router.post("/group/:groupId", createMatchSession);
router.post("/swipes/:sessionId", submitSwipes);
router.post("/solo", createSoloMatchSession);
router.get("/result/:sessionId", getSessionResult);

export default router;
