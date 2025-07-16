import express from "express";
import {
    setProfilePref,
    getProfilePref
} from "../controllers/profilePrefController.js"; // adjust path as needed
import authMiddleware from "../middlewares/authMiddleware.js";

const router = express.Router();

// GET user preferences (requires auth)
router.get("/", authMiddleware, getProfilePref);

// POST new preferences or update existing (requires auth)
router.post("/", authMiddleware, setProfilePref);

export default router;
