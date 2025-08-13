const express = require('express');
const router = express.Router();
const { protect, admin } = require('../middleware/auth');
const sanPhamController = require('../controllers/sanPhamController');

// @route   GET api/sanpham
// @desc    Get all SanPham
// @access  Public
router.get('/', sanPhamController.getAllSanPham);

// @route   GET api/sanpham/latest/:limit
// @desc    Get latest SanPham with limit
// @access  Public
router.get('/latest/:limit', sanPhamController.getLatestSanPham);

// @route   GET api/sanpham/inventory/status
// @desc    Get inventory status for all products
// @access  Private/Admin
router.get('/inventory/status', protect, admin, sanPhamController.getInventoryStatus);

// @route   PUT api/sanpham/:id/inventory
// @desc    Update product inventory directly
// @access  Private/Admin
router.put('/:id/inventory', protect, admin, sanPhamController.updateSanPhamInventory);

// @route   GET api/sanpham/search/:query
// @desc    Search SanPham by name
// @access  Public
router.get('/search/:query', sanPhamController.searchSanPham);

// @route   GET api/sanpham/nhom/:id
// @desc    Get SanPham by NhomSanPham id
// @access  Public
router.get('/nhom/:id', sanPhamController.getSanPhamByNhomId);

// @route   GET api/sanpham/inventory/status
// @desc    Get inventory status for all products
// @access  Private/Admin
router.get('/inventory/status', protect, admin, sanPhamController.getInventoryStatus);

// @route   PUT api/sanpham/:id/inventory
// @desc    Update product inventory directly
// @access  Private/Admin
router.put('/:id/inventory', protect, admin, sanPhamController.updateSanPhamInventory);

// @route   GET api/sanpham/:id
// @desc    Get SanPham by id
// @access  Public
router.get('/:id', sanPhamController.getSanPhamById);

// @route   POST api/sanpham
// @desc    Create a SanPham
// @access  Private/Admin
router.post('/', protect, admin, sanPhamController.createSanPham);

// @route   PUT api/sanpham/:id
// @desc    Update a SanPham
// @access  Private/Admin
router.put('/:id', protect, admin, sanPhamController.updateSanPham);

// @route   DELETE api/sanpham/:id
// @desc    Delete a SanPham
// @access  Private/Admin
router.delete('/:id', protect, admin, sanPhamController.deleteSanPham);

// @route   GET api/sanpham/inventory/history
// @desc    Get inventory history
// @access  Private/Admin
router.get('/inventory/history', protect, admin, sanPhamController.getInventoryHistory);

// @route   GET api/sanpham/inventory/low-stock-alert
// @desc    Check and notify low stock products
// @access  Private/Admin
router.get('/inventory/low-stock-alert', protect, admin, sanPhamController.checkLowStockAlert);

module.exports = router;