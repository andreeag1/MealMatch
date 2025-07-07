import response from "../helpers/response.js";
import Post from "../models/postModel.js";

/**
 * @desc Get all posts
 * @route GET /api/posts
 */
export const getAllPosts = async (req, res) => {
  try {
    const posts = await Post.find({}).sort({ createdAt: -1 });
    return response(res, "List of Posts", 200, true, posts);
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Create a new post
 * @route POST /api/posts
 */
export const createPost = async (req, res) => {
  try {
    const { caption, rating = 0, imageUrl = null, user } = req.body;

    if (!caption || !user?.username) {
      return response(res, "Caption and username are required", 400, false);
    }

    const newPost = new Post({
      caption,
      rating,
      imageUrl,
      user,
    });

    const savedPost = await newPost.save();
    return response(res, "Post created successfully", 201, true, savedPost);
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Delete a post by ID
 * @route DELETE /api/posts/:id
 */
export const deletePost = async (req, res) => {
  try {
    const deletedPost = await Post.findByIdAndDelete(req.params.id);
    if (deletedPost) {
      return response(res, "Post deleted successfully", 200, true);
    } else {
      return response(res, "Post not found", 404, false);
    }
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};
