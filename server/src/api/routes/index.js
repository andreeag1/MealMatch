import { Router } from "express";
const router = Router();

import userRoutes from "./userRoute.js";
import groupRoutes from "./groupRoute.js";
import authRoutes from "./authRoute.js";
import sessionRoutes from "./matchSessionRoute.js";

router.use("/users", userRoutes);
router.use("/groups", groupRoutes);
router.use("/auth", authRoutes);
router.use("/sessions", sessionRoutes);

export default router;
