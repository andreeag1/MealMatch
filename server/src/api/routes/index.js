import express from "express";
const router = express.Router();

import userRoutes from "./userRoute.js";
import groupRoutes from "./groupRoute.js";
import authRoutes from "./authRoute.js";
import sessionRoutes from "./matchSessionRoute.js";

import friendsRoutes from './friendsRoute.js';

import postRoutes from "./postRoute.js"; 

import profilePrefRoutes from "./profilePrefRoute.js";

router.use("/users", userRoutes);
router.use("/groups", groupRoutes);
router.use("/auth", authRoutes);
router.use("/sessions", sessionRoutes);

router.use("/friends", friendsRoutes);

router.use("/posts", postRoutes);

router.use("/user_profiles", profilePrefRoutes);

export default router;
