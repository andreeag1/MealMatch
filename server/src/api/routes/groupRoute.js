import { Router } from "express";
const router = Router();
import {
  createGroup,
  getGroupMessages,
} from "../controllers/groupController.js";
import authMiddleware from "../middlewares/authMiddleware.js";

router.use(authMiddleware);

router.post("/", createGroup);
router.get("/:groupId", getGroupMessages);

export default router;
