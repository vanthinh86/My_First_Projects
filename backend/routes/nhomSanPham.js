const express = require('express');
const router = express.Router();
const { protect, admin } = require('../middleware/auth');
const nhomSanPhamController = require('../controllers/nhomSanPhamController');

// @route   GET api/nhomsanpham
// @desc    Get all NhomSanPham
// @access  Public
router.get('/', nhomSanPhamController.getAllNhomSanPham);

// @route   GET api/nhomsanpham/:id
// @desc    Get NhomSanPham by id
// @access  Public
router.get('/:id', nhomSanPhamController.getNhomSanPhamById);

// @route   POST api/nhomsanpham
// @desc    Create a NhomSanPham
// @access  Private/Admin
router.post('/', protect, admin, nhomSanPhamController.createNhomSanPham);

// @route   PUT api/nhomsanpham/:id
// @desc    Update a NhomSanPham
// @access  Private/Admin
router.put('/:id', protect, admin, nhomSanPhamController.updateNhomSanPham);

// @route   DELETE api/nhomsanpham/:id
// @desc    Delete a NhomSanPham
// @access  Private/Admin
router.delete('/:id', protect, admin, nhomSanPhamController.deleteNhomSanPham);

module.exports = router;