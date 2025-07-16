import response from "../helpers/response.js";
import { PrefMsg, ProfilePref } from "../models/profilePrefModel.js";

/**
 * @desc Create or update user profile preferences
 * @route POST /api/user_profiles
 */
export const setProfilePref = async (req, res) => {
  try {
    const { userID, username, email, userPreferenceMessage } = req.body;
    const { cuisine, dietary, ambiance, budget } = userPreferenceMessage;

    // const userId = req.user._id;
    // console.log("Incoming req.body:", req.body); // Log what you receive

    // const { cuisine, dietary, ambiance, budget, username, email } = req.body;


    console.log("Parsed values:", { userID, cuisine, dietary, ambiance, budget, username, email });

    const newPrefMsg = new PrefMsg({
      cuisine,
      dietary,
      ambiance,
      budget,
    });

    const newProfilePref = new ProfilePref({
      userID: userID,
      username,
      email,
      userPreferenceMessage: newPrefMsg,
    });

    const savedProfilePref = await newProfilePref.save();
    console.log("Saving newProfilePref:", newProfilePref);

    return response(res, "Profile preferences saved", 200, true, savedProfilePref);
  } catch (error) {
    console.error("Error saving profile preferences:", error);

    return response(res, "Internal server error", 500, false, { error: error.message });
  }
};

/**
 * @desc Get user profile preferences
 * @route GET /api/user_profiles
 */
export const getProfilePref = async (req, res) => {
  try {

    const {userID} = req.body;
    const profile = await ProfilePref.findOne({ userID: userID });

    if (profile) {
      return response(res, "Profile preferences found", 200, true, profile);
    } else {
      return response(res, "Profile preferences not found", 404, false);
    }
  } catch (error) {
    return response(res, "Internal server error", 500, false, { error: error.message });
  }
};
