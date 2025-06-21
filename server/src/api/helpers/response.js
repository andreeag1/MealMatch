export default (res, message, status = 200, success = true, additionalData) => {
  return res.status(status).send({
    success,
    message: message || "Success",
    ...additionalData,
  });
};
