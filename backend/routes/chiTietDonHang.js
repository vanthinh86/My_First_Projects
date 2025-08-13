const express = require('express');
const router = express.Router();
const { protect, admin } = require('../middleware/auth');
const chiTietDonHangController = require('../controllers/chiTietDonHangController');

// @route   GET api/chitietdonhang/debug/all
// @desc    Debug endpoint to check database contents
// @access  Private/Admin
router.get('/debug/all', protect, admin, chiTietDonHangController.debugChiTietDonHang);

// @route   POST api/chitietdonhang
// @desc    Add new chi tiet don hang item
// @access  Private
router.post('/', protect, chiTietDonHangController.addChiTietDonHang);

// @route   PUT api/chitietdonhang/status/:id
// @desc    Update chi tiet don hang status
// @access  Private/Admin
router.put('/status/:id', protect, admin, chiTietDonHangController.updateChiTietDonHangStatus);

// @route   PUT api/chitietdonhang/:id
// @desc    Update chi tiet don hang item
// @access  Private
router.put('/:id', protect, chiTietDonHangController.updateChiTietDonHang);

// @route   DELETE api/chitietdonhang/:id
// @desc    Delete chi tiet don hang item
// @access  Private/Admin
router.delete('/:id', protect, admin, chiTietDonHangController.deleteChiTietDonHang);

// @route   GET api/chitietdonhang/:orderId
// @desc    Get all ChiTietDonHang for a specific order
// @access  Private
router.get('/:orderId', protect, chiTietDonHangController.getChiTietDonHangByOrderId);

module.exports = router;