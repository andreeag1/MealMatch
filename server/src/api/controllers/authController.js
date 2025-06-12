const User = require("../models/userModel");
const response = require("../helpers/response");
const jwt = require("jsonwebtoken");
const { jwtSecret, jwtExpiresIn } = require("../../config/index");

const login = async (req, res) => {
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

    return response(res, "Login Successful", 200, true, { token });
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: err.message,
    });
  }
};

module.exports = login;
