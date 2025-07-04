/**
 * A placeholder matching algorithm
 * @param {Array} allSwipes - An array of all swipe objects from the session.
 * @returns {object} The result of the match.
 */
export const runMatchingAlgorithm = (allSwipes) => {
  console.log("Running matching algorithm...");

  const likeCounts = {};
  allSwipes.forEach((swipe) => {
    if (swipe.liked) {
      likeCounts[swipe.restaurantId] =
        (likeCounts[swipe.restaurantId] || 0) + 1;
    }
  });

  let bestMatchId = null;
  let maxLikes = 0;
  for (const restaurantId in likeCounts) {
    if (likeCounts[restaurantId] > maxLikes) {
      maxLikes = likeCounts[restaurantId];
      bestMatchId = restaurantId;
    }
  }

  // for now, just return the most liked restaurant.
  return {
    restaurantId: bestMatchId,
  };
};
