import response from "../helpers/response.js";
import { ProfilePref } from "../models/profilePrefModel.js";
import User from "../models/userModel.js";

/**
 * @desc Create or update user profile preferences
 * @route POST /api/user_profiles
 */
export const setProfilePref = async (req, res) => {
  try {
    const { cuisine, dietary, ambiance, budget } = req.body;
    const userID = req.user._id;
    const user = await User.findById(userID);

    const newPreferences = new ProfilePref({
      cuisine,
      dietary,
      ambiance,
      budget,
    });

    user.preferences = { ...user.preferences, ...req.body };

    await user.save();

    return response(
      res,
      "Profile preferences saved",
      200,
      true,
      newPreferences
    );
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Get user profile preferences
 * @route GET /api/user_profiles
 */
export const getProfilePref = async (req, res) => {
  try {
    const userID = req.user._id;
    const user = await User.findById(userID);

    if (user.preferences != null) {
      return response(
        res,
        "Profile preferences found",
        200,
        true,
        user.preferences
      );
    } else {
      return response(res, "Profile preferences not found", 404, false);
    }
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};
