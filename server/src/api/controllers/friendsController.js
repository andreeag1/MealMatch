// src/api/controllers/friendsController.js
import User from '../models/userModel.js';

export const addFriend = async (req, res) => {
  const userId = req.user._id.toString(); // authenticated user ID
  const { friendUsername } = req.body;

  if (!friendUsername) {
    return res.status(400).json({ message: "Friend username is required" });
  }

  try {
    const friend = await User.findOne({ username: friendUsername });

    if (!friend) {
      return res.status(404).json({ message: "User not found" });
    }

    const friendId = friend._id.toString();

    if (userId === friendId) {
      return res.status(400).json({ message: "You cannot add yourself as a friend" });
    }

    // Mutual friendship is assumed here maybe later we can do request and accept...
    await User.findByIdAndUpdate(userId, { $addToSet: { friends: friendId } });
    await User.findByIdAndUpdate(friendId, { $addToSet: { friends: userId } });

    return res.status(200).json({ message: `You are now friends with ${friendUsername}` });
  } catch (error) {
    return res.status(500).json({ error: error.message });
  }
};

export const removeFriend = async (req, res) => {
  const userId = req.user._id.toString();
  const { friendUsername } = req.body;

  if (!friendUsername) {
    return res.status(400).json({ message: "Friend username is required" });
  }

  try {
    const friend = await User.findOne({ username: friendUsername });

    if (!friend) {
      return res.status(404).json({ message: "User not found" });
    }

    const friendId = friend._id.toString();
    const user = await User.findById(userId);

    if (!user.friends.includes(friendId)) {
      return res.status(400).json({ message: "This user is not in your friends list" });
    }
    await User.findByIdAndUpdate(userId, { $pull: { friends: friendId } });
    await User.findByIdAndUpdate(friendId, { $pull: { friends: userId } });

    return res.status(200).json({ message: `You are no longer friends with ${friendUsername}` });

  } catch (err) {
    return res.status(500).json({ error: err.message });
  }
};


export const getFriendsList = async (req, res) => {
  const userId = req.user._id.toString();

  try {
    const user = await User.findById(userId).populate('friends', 'username email');
    if (!user) return res.status(404).json({ message: "User not found" });

    res.status(200).json({ friends: user.friends });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};
