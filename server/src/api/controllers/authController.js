import User from "../models/userModel.js";
import response from "../helpers/response.js";
import jwt from "jsonwebtoken";
import config from "../../config/index.js";

const { jwtSecret, jwtExpiresIn } = config;

/**
 * @desc Create a new user
 * @route POST /api/users/login
 */
export const login = async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return response(res, "Please provide email and password", 400, false);
    }

    const user = await User.findOne({ email }).select("+password");

    if (!user || !(await user.matchPassword(password))) {
      return response(res, "Invalid Credentials", 401, false);
    }

    const token = jwt.sign({ id: user._id }, jwtSecret, {
      expiresIn: jwtExpiresIn,
    });

    return response(res, "Login Successful", 200, true, {
      token,
      username: user.username,
    });
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Create a new user
 * @route POST /api/users/register
 */
export const register = async (req, res) => {
  try {
    const { username, email, password } = req.body;
    const newUser = new User({ username, email, password });
    const savedUser = await newUser.save();

    const token = jwt.sign({ id: savedUser._id }, jwtSecret, {
      expiresIn: jwtExpiresIn,
    });

    return response(res, "New User Created", 201, true, {
      token,
      username: username,
    });
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};
