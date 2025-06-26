const response = (res, message, status = 200, success = true, payload) => {
  const responseBody = {
    success,
    message: message || "Success",
  };

  // If the response is successful and there is a payload, add it under a 'data' key
  if (success && payload) {
    responseBody.data = payload;
  }
  // If the response is an error and there is a payload (e.g., error details), spread it at the top level
  else if (!success && payload) {
    Object.assign(responseBody, payload);
  }

  return res.status(status).send(responseBody);
};

export default response;
