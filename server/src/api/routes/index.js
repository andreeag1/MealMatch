const express = require("express");
const router = express.Router();

const userRoutes = require("./userRoute.js");
const groupRoutes = require("./groupRoute.js");
const authRoutes = require("./authRoute.js");

router.use("/users", userRoutes);
router.use("/groups", groupRoutes);
router.use("/auth", authRoutes);

module.exports = router;
