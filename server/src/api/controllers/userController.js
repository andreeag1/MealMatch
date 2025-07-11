import response from "../helpers/response.js";
import User from "../models/userModel.js";

/**
 * @desc Get all users
 * @route GET /api/users
 */
export const getAllUsers = async (req, res) => {
  try {
    const users = await User.find({});
    return response(res, "List of Users", 200, true, users);
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Get a single user by ID
 * @route GET /api/users/:id
 */
export const getUserById = async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    if (user) {
      return response(res, "User Found", 200, true, user);
    } else {
      return response(res, "User not Found", 404, false);
    }
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Update a user by ID
 * @route PUT /api/users/:id
 */
export const updateUser = async (req, res) => {
  try {
    const updatedUser = await User.findByIdAndUpdate(req.params.id, req.body, {
      new: true,
      runValidators: true,
    });
    if (updatedUser) {
      return response(res, "User updated", 200, true, updatedUser);
    } else {
      return response(res, "User not Found", 404, false);
    }
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Delete a user by ID
 * @route DELETE /api/users/:id
 */
export const deleteUser = async (req, res) => {
  try {
    const deletedUser = await User.findByIdAndDelete(req.params.id);
    if (deletedUser) {
      return response(res, "User deleted successfully", 200, true);
    } else {
      return response(res, "User not Found", 404, false);
    }
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};
