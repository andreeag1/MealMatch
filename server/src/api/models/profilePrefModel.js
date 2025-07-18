import mongoose from "mongoose";

const userPreferenceSchema = new mongoose.Schema(
  {
    cuisine: {
      type: String,
      required: true,
    },
    dietary: {
      type: String,
      required: true,
    },
    ambiance: {
      type: String,
      required: true,
    },
    budget: {
      type: String,
      required: true,
    },
  },
  {
    timestamps: true,
  }
);

const ProfilePref = mongoose.model("ProfilePref", userPreferenceSchema);

export { userPreferenceSchema, ProfilePref };
