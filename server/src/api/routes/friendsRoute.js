import express from 'express';
import protect from '../middlewares/authMiddleware.js';

import {
  removeFriend,
  getFriendsList,
} from '../controllers/friendsController.js';

const router = express.Router();


router.post('/remove', protect, removeFriend);
router.get('/list', protect, getFriendsList);

export default router;