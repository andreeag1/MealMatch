const response = require("../helpers/response");
const User = require("../models/userModel");

/**
 * @desc Get all users
 * @route GET /api/users
 */
const getAllUsers = async (req, res) => {
  try {
    const users = await User.find({});
    return response(res, "List of Users", 200, true, { users });
  } catch (error) {
    console.log(error);
    return response(res, "Internal server error", 500, false, {
      error: err.message,
    });
  }
};

/**
 * @desc Get a single user by ID
 * @route GET /api/users/:id
 */
const getUserById = async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    if (user) {
      return response(res, "User Found", 200, true, { user });
    } else {
      return response(res, "User not Found", 404, false);
    }
  } catch (error) {
    console.log(error);
    return response(res, "Internal server error", 500, false, {
      error: err.message,
    });
  }
};

/**
 * @desc Create a new user
 * @route POST /api/users
 */
const createUser = async (req, res) => {
  try {
    const { username, email, password } = req.body;
    const newUser = new User({ username, email, password });
    const savedUser = await newUser.save();

    const userResponse = {
      _id: savedUser._id,
      username: savedUser.username,
      email: savedUser.email,
      createdAt: savedUser.createdAt,
      updatedAt: savedUser.updatedAt,
    };

    return response(res, "New User Created", 201, true, { userResponse });
  } catch (error) {
    console.log(error);
    return response(res, "Internal server error", 500, false, {
      error: err.message,
    });
  }
};

/**
 * @desc Update a user by ID
 * @route PUT /api/users/:id
 */
const updateUser = async (req, res) => {
  try {
    const updatedUser = await User.findByIdAndUpdate(req.params.id, req.body, {
      new: true,
      runValidators: true,
    });
    if (updatedUser) {
      return response(res, "User updated", 200, true, { updatedUser });
    } else {
      return response(res, "User not Found", 404, false);
    }
  } catch (error) {
    console.log(error);
    return response(res, "Internal server error", 500, false, {
      error: err.message,
    });
  }
};

/**
 * @desc Delete a user by ID
 * @route DELETE /api/users/:id
 */
const deleteUser = async (req, res) => {
  try {
    const deletedUser = await User.findByIdAndDelete(req.params.id);
    if (deletedUser) {
      return response(res, "User deleted successfully", 200, true);
    } else {
      return response(res, "User not Found", 404, false);
    }
  } catch (error) {
    console.log(error);
    return response(res, "Internal server error", 500, false, {
      error: err.message,
    });
  }
};

module.exports = {
  getAllUsers,
  getUserById,
  createUser,
  updateUser,
  deleteUser,
};
