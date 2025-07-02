import Group from "../models/groupModel.js";
import Message from "../models/messageModel.js";
import User from "../models/userModel.js";
import response from "../helpers/response.js";

/**
 * @desc Create a Group
 * @route POST /api/groups
 */
export const createGroup = async (req, res) => {
  try {
    const { name, members: membersUsernames } = req.body;
    const creatorId = req.user._id;

    const usersToAdd = await User.find({ username: { $in: membersUsernames } });
    const memberIds = usersToAdd.map((user) => user._id);

    const allMemberIds = new Set(
      [creatorId, ...memberIds].map((id) => id.toString())
    );
    console.log(allMemberIds);
    const newGroup = new Group({ name, members: Array.from(allMemberIds) });
    await newGroup.save();
    return response(res, "Group Created", 200, true);
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Get messages for a specific gorup
 * @route GET /api/groups/:id
 */
export const getGroupMessages = async (req, res) => {
  try {
    const messages = await Message.find({ group: req.params.groupId })
      .populate("user", "username")
      .sort({ createdAt: "asc" });
    return response(res, "List Group Messages", 200, true, messages);
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Get all groups for the authenticated user
 * @route GET /api/groups
 */
export const getUserGroups = async (req, res) => {
  try {
    const userId = req.user._id;

    const groups = await Group.find({ members: userId })
      .populate("members", "username email")
      .sort({ updatedAt: -1 }); //show most recently active groups first

    return response(res, "User groups fetched successfully", 200, true, groups);
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};
