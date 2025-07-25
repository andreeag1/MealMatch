import mongoose from "mongoose";

const swipeSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
    },
    restaurantId: {
      type: String,
      required: true,
    },
    liked: {
      type: Boolean,
      required: true, // true for 'yes' (like), false for 'no' (dislike)
    },
  },
  { _id: false }
);

const matchSessionSchema = new mongoose.Schema(
  {
    group: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Group",
      required: false,
    },
    status: {
      type: String,
      enum: ["active", "completed", "aborted"],
      default: "active",
    },
    preferences: {
      cuisine: { type: String },
      dietary: { type: String },
      ambiance: { type: String },
      budget: { type: String },
    },
    restaurants: [
      {
        type: String,
      },
    ],
    swipes: [swipeSchema],
    // Array to track which members have submitted their swipes
    participants: [
      {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User",
      },
    ],
    result: {
      restaurantId: String,
    },
  },
  {
    timestamps: true,
  }
);

const MatchSession = mongoose.model("MatchSession", matchSessionSchema);
export default MatchSession;
