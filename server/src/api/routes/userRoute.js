import { Router } from "express";
import {
  getAllUsers,
  getUserById,
  updateUser,
  deleteUser,
} from "../controllers/userController.js";
import authMiddleware from "../middlewares/authMiddleware.js";

const router = Router();

router.get("/", getAllUsers);
router.get("/me", authMiddleware, getUserById);
router.put("/:id", updateUser);
router.delete("/:id", deleteUser);

export default router;
