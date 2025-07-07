import express from "express";
import {
  getAllPosts,
  createPost,
  deletePost,
} from "../controllers/postController.js";

const router = express.Router();

router.get("/", getAllPosts);
router.post("/", createPost);
router.delete("/:id", deletePost);

export default router;
