import { Router } from "express";
const router = Router();
import {
  createGroup,
  getGroupMessages,
  getUserGroups,
} from "../controllers/groupController.js";
import authMiddleware from "../middlewares/authMiddleware.js";

router.use(authMiddleware);

router.post("/", createGroup);
router.get("/:groupId", getGroupMessages);
router.get("/", getUserGroups);

export default router;
