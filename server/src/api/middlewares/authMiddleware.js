import jwt from "jsonwebtoken";
import User from "../models/userModel.js";
import response from "../helpers/response.js";
import config from "../../config/index.js";

const { jwtSecret } = config;

export default async (req, res, next) => {
  let token;
  if (
    req.headers.authorization &&
    req.headers.authorization.startsWith("Bearer")
  ) {
    token = req.headers.authorization.split(" ")[1];
  }

  if (!token) {
    return response(res, "Not authorized, no token", 401, false);
  }

  try {
    // Verify token
    const decoded = jwt.verify(token, jwtSecret);

    // Check if user still exists
    const currentUser = await User.findById(decoded.id);
    if (!currentUser) {
      return res.status(401).json({
        message: "The user belonging to this token no longer exists.",
      });
    }

    // Attach user to the request object
    req.user = currentUser;
    next();
  } catch (error) {
    return res.status(401).json({ message: "Not authorized, token failed" });
  }
};
