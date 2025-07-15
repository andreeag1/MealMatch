import express from 'express';
import protect from '../middlewares/authMiddleware.js';

import {
  addFriend,
  removeFriend,
  getFriendsList,
} from '../controllers/friendsController.js';

const router = express.Router();

// POST /api/friends/add
router.post('/add',protect, addFriend);

// DELETE /api/friends/remove
router.post('/remove', protect, removeFriend);

// GET /api/friends/list
router.get('/list', protect, getFriendsList);  // changed to /list

export default router;