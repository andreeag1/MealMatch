import User from '../models/userModel.js';
import FriendRequest from '../models/FriendRequest.js';
import response  from "../helpers/response.js";

export const sendFriendRequest = async (req, res) => {
    try {
        const { username: recipientUsername } = req.body;
        const senderId = req.user._id;

        const recipient = await User.findOne({ username: recipientUsername });
        if (!recipient) {
            return response(res, "User not found", 404, false);
        }
        if (recipient._id.toString() === senderId.toString()) {
            return response(res, "You cannot send a friend request to yourself", 400, false);
        }

        const sender = await User.findById(senderId);
        if (sender.friends.includes(recipient._id)) {
            return response(res, "You are already friends", 400, false);
        }

        const existingRequest = await FriendRequest.findOne({
            $or: [{ fromUser: senderId, toUser: recipient._id }, { fromUser: recipient._id, toUser: senderId }],
            status: 'pending'
        });
        if (existingRequest) {
            return response(res, "A friend request is already pending", 400, false);
        }

        await FriendRequest.create({ fromUser: senderId, toUser: recipient._id });
        return response(res, "Friend request sent", 201, true);

    } catch (error) {
        return response(res, "Internal server error", 500, false, { error: error.message });
    }
};

export const getFriendRequests = async (req, res) => {
    try {
        const { type } = req.query;
        const currentUserId = req.user._id;

        let query = {};
        if (type === 'incoming') {
            query = { toUser: currentUserId, status: 'pending' };
        } else if (type === 'outgoing') {
            query = { fromUser: currentUserId, status: 'pending' };
        } else {
            return response(res, "Invalid request type specified", 400, false);
        }

        const requests = await FriendRequest.find(query)
            .populate('fromUser', 'username email')
            .populate('toUser', 'username email');

        return response(res, "Requests fetched successfully", 200, true, requests);

    } catch (error) {
        return response(res, "Internal server error", 500, false, { error: error.message });
    }
};

export const acceptFriendRequest = async (req, res) => {
    try {
        const { requestId } = req.body;
        const currentUserId = req.user._id;

        const request = await FriendRequest.findById(requestId);
        if (!request || request.toUser.toString() !== currentUserId.toString() || request.status !== 'pending') {
            return response(res, "Invalid request", 400, false);
        }

        request.status = 'accepted';
        await request.save();

        await User.findByIdAndUpdate(currentUserId, { $addToSet: { friends: request.fromUser } });
        await User.findByIdAndUpdate(request.fromUser, { $addToSet: { friends: currentUserId } });

        return response(res, "Friend request accepted", 200, true);

    } catch (error) {
        return response(res, "Internal server error", 500, false, { error: error.message });
    }
};

export const declineOrCancelRequest = async (req, res) => {
    try {
        const { requestId } = req.body;
        const currentUserId = req.user._id;

        const request = await FriendRequest.findById(requestId);
        if (!request) {
            return response(res, "Request not found", 404, false);
        }
        if (request.toUser.toString() !== currentUserId.toString() && request.fromUser.toString() !== currentUserId.toString()) {
            return response(res, "Not authorized to modify this request", 403, false);
        }

        await FriendRequest.findByIdAndDelete(requestId);
        return response(res, "Request removed", 200, true);

    } catch (error) {
        return response(res, "Internal server error", 500, false, { error: error.message });
    }
};