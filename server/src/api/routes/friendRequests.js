import express from 'express';
//import auth from '../middleware/auth.js';

import authMiddleware from "../middlewares/authMiddleware.js";

import {
    sendFriendRequest,
    getFriendRequests,
    acceptFriendRequest,
    declineOrCancelRequest
    } from '../controllers/friendRequestController.js';


const router = express.Router();

router.use(authMiddleware);

router.post('/', sendFriendRequest);
router.get('/', getFriendRequests);
router.post('/accept', acceptFriendRequest);
router.post('/decline', declineOrCancelRequest);

export default router;