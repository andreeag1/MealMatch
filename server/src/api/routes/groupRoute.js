const express = require("express");
const router = express.Router();
const {
  createGroup,
  getGroupMessages,
} = require("../controllers/groupController");
const authMiddleware = require("../middlewares/authMiddleware");

router.use(authMiddleware);

router.post("/", createGroup);
router.get("/:roomId", getGroupMessages);

module.exports = router;
