import { Router } from "express";
const router = Router();

import userRoutes from "./userRoute.js";
import groupRoutes from "./groupRoute.js";
import authRoutes from "./authRoute.js";

router.use("/users", userRoutes);
router.use("/groups", groupRoutes);
router.use("/auth", authRoutes);

export default router;
