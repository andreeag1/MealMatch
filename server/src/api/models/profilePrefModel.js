import mongoose from "mongoose";


const UserPreferenceMessageSchema = new mongoose.Schema(
    {
        cuisine:{
            type: String, 
            required: true, 
        },
        dietary:{
            type: String, 
            required: true, 
        },
        ambiance:{
            type: String, 
            required: true, 
        },
        budget:{
            type: String, 
            required: true, 
        },  
    },
    {
        timestamps: true,
    }
);

const profilePrefSchema = new mongoose.Schema(
    {
        userID:{
            type: String, 
            required: true, 
        },
        username:{
            type: String, 
            required: true, 
        },
        email:{
            type: String, 
            required: true, 
        },
        userPreferenceMessage:{
            type: UserPreferenceMessageSchema, 
            required: true, 
        },  
    },
    {
        timestamps: true,
    }
);

const PrefMsg = mongoose.model("PrefMsg", UserPreferenceMessageSchema);
const ProfilePref = mongoose.model("ProfilePref", profilePrefSchema);

export { PrefMsg, ProfilePref };


