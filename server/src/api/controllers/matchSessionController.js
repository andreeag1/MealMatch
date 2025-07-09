import MatchSession from "../models/matchSessionModel.js";
import Group from "../models/groupModel.js";
import response from "../helpers/response.js";
import { runMatchingAlgorithm } from "../helpers/matchingAlgorithm.js";

//mock function to get restaurants
const getRestaurantsForSession = async () => {
  // Returning a mock list of restaurant IDs for now
  return [
    "restaurant_id_1",
    "restaurant_id_2",
    "restaurant_id_3",
    "restaurant_id_4",
  ];
};

/**
 * @desc Create a new match session for a group
 * @route POST /api/sessions/group/:groupId
 */
export const createMatchSession = async (req, res) => {
  try {
    const { groupId } = req.params;

    const group = await Group.findById(groupId);
    if (!group) {
      return response(res, "Group not found", 404, false);
    }

    // Fetch restaurants for this new session
    const restaurants = await getRestaurantsForSession();

    const newSession = new MatchSession({
      group: groupId,
      restaurants: restaurants,
      participants: [],
    });
    await newSession.save();

    return response(
      res,
      "New match session started successfully",
      201,
      true,
      newSession
    );
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Submit a user's swipes for a session
 * @route POST /api/sessions/:sessionId/swipes
 */
export const submitSwipes = async (req, res) => {
  try {
    const { sessionId } = req.params;
    const userId = req.user._id;
    const { swipes } = req.body;

    if (!swipes || !Array.isArray(swipes)) {
      return response(res, "A 'swipes' array is required.", 400, false);
    }

    const session = await MatchSession.findById(sessionId).populate("group");
    if (!session) {
      return response(res, "Session not found", 404, false);
    }
    if (session.status !== "active") {
      return response(res, "This session is no longer active.", 400, false);
    }
    if (session.participants.includes(userId)) {
      return response(
        res,
        "You have already submitted your swipes for this session.",
        400,
        false
      );
    }

    // add the user's swipes to the session
    const userSwipes = swipes.map((swipe) => ({
      user: userId,
      restaurantId: swipe.restaurantId,
      liked: swipe.liked,
    }));
    session.swipes.push(...userSwipes);

    // mark this user as having participated
    session.participants.push(userId);

    const requiredParticipants = session.group
      ? session.group.members.length
      : 1;

    // check if all group members have participated
    if (session.participants.length >= requiredParticipants) {
      const matchResult = runMatchingAlgorithm(session.swipes);
      session.result = matchResult;
      session.status = "completed";
      console.log(
        `Session ${sessionId} completed. Match: ${matchResult.restaurantName}`
      );
    }

    await session.save();

    return response(res, "Swipes submitted successfully", 200, true, session);
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Create a new solo match session
 * @route POST /api/sessions/solo
 */
export const createSoloMatchSession = async (req, res) => {
  try {
    const userId = req.user._id;
    const restaurants = await getRestaurantsForSession();

    const newSession = new MatchSession({
      restaurants: restaurants,
      participants: [],
    });
    await newSession.save();

    return response(
      res,
      "New solo session started successfully",
      201,
      true,
      newSession
    );
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Get the result of a match session
 * @route GET /api/sessions/result/:sessionId
 */
export const getSessionResult = async (req, res) => {
  try {
    const { sessionId } = req.params;

    const session = await MatchSession.findById(sessionId).populate("group");
    if (!session) {
      return response(res, "Session not found", 404, false);
    }

    if (session.status !== "completed") {
      return response(
        res,
        "Session is still active. Not all members have voted.",
        202,
        true,
        { status: session.status }
      );
    }

    return response(
      res,
      "Session result fetched successfully",
      200,
      true,
      session.result
    );
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};

/**
 * @desc Get all active match sessions for a specific group
 * @route GET /api/sessions/group/:groupId/active
 */
export const getActiveSessions = async (req, res) => {
  try {
    const { groupId } = req.params;

    // Find all sessions for this group that are still active
    const activeSessions = await MatchSession.find({
      group: groupId,
      status: "active",
    }).sort({ createdAt: -1 }); // Show newest sessions first

    return response(
      res,
      "Active sessions fetched successfully",
      200,
      true,
      activeSessions
    );
  } catch (error) {
    return response(res, "Internal server error", 500, false, {
      error: error.message,
    });
  }
};
