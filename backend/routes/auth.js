const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

// @route   POST api/auth/register
// @desc    Register user
// @access  Public
router.post('/register', authController.register);

// @route   POST api/auth/login
// @desc    Login user / Return JWT token
// @access  Public
router.post('/login', authController.login);

// @route   POST api/auth/change-password
// @desc    Change password
// @access  Public
router.post('/change-password', authController.changePassword);

// Get list of users
router.get('/users', authController.getUsers);
// Get list of roles
router.get('/roles', authController.getRoles);

// @route   PUT api/auth/users/:tendn
// @desc    Update user
// @access  Private/Admin
router.put('/users/:tendn', authController.updateUser);

// @route   DELETE api/auth/users/:tendn
// @desc    Delete user
// @access  Private/Admin
router.delete('/users/:tendn', authController.deleteUser);
module.exports = router;