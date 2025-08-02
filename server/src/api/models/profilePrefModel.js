import mongoose from "mongoose";

const userPreferenceSchema = new mongoose.Schema(
  {
    cuisine: {
      type: String,
      required: true,
      default: "Any",
    },
    dietary: {
      type: String,
      required: true,
      default: "Any",
    },
    ambiance: {
      type: String,
      required: true,
      default: "Any",
    },
    budget: {
      type: String,
      required: true,
      default: "$$",
    },
  },
  {
    timestamps: true,
  }
);

const ProfilePref = mongoose.model("ProfilePref", userPreferenceSchema);

export { userPreferenceSchema, ProfilePref };
