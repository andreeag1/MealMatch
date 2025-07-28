import express from "express";
import {
  getAllPosts,
  createPost,
  deletePost,
  updatePost
} from "../controllers/postController.js";
import authMiddleware from "../middlewares/authMiddleware.js";

const router = express.Router();

router.get("/", getAllPosts);
router.post("/", authMiddleware, createPost);
router.delete("/:id", authMiddleware, deletePost);
router.put("/:id", authMiddleware, updatePost);

export default router;
