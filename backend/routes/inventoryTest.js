const express = require('express');
const router = express.Router();
const { protect, admin } = require('../middleware/auth');
const SanPham = require('../models/SanPham');
const DatHang = require('../models/DatHang');
const ChiTietDonHang = require('../models/ChiTietDonHang');
const InventoryHistory = require('../models/InventoryHistory');
const { emitToAll } = require('../utils/socketEmitter');

// @desc    Test inventory management system
// @route   GET /api/test/inventory
// @access  Private/Admin
router.get('/inventory', protect, admin, async (req, res) => {
    try {
        const inventoryStats = {
            totalProducts: await SanPham.countDocuments(),
            outOfStock: await SanPham.countDocuments({ soluongkho: 0 }),
            lowStock: await SanPham.countDocuments({ soluongkho: { $lte: 5, $gt: 0 } }),
            totalOrders: await DatHang.countDocuments(),
            paidOrders: await DatHang.countDocuments({ trangthaithanhtoan: 1 }),
            totalInventoryChanges: await InventoryHistory.countDocuments()
        };

        const recentInventoryChanges = await InventoryHistory.find()
            .sort({ timestamp: -1 })
            .limit(10);

        res.json({
            success: true,
            message: 'Inventory management system is working!',
            stats: inventoryStats,
            recentChanges: recentInventoryChanges,
            features: [
                'Automatic inventory deduction when orders are paid',
                'Automatic inventory restoration when orders are cancelled',
                'Real-time inventory updates via WebSocket',
                'Low stock alerts for admin',
                'Inventory history tracking',
                'Manual inventory adjustment by admin'
            ]
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: 'Error testing inventory system',
            error: error.message
        });
    }
});

// @desc    Simulate order payment for testing
// @route   POST /api/test/simulate-payment
// @access  Private/Admin
router.post('/simulate-payment', protect, admin, async (req, res) => {
    try {
        const { id_dathang } = req.body;
        
        if (!id_dathang) {
            return res.status(400).json({
                success: false,
                message: 'id_dathang is required'
            });
        }

        const order = await DatHang.findOne({ id_dathang: id_dathang });
        if (!order) {
            return res.status(404).json({
                success: false,
                message: 'Order not found'
            });
        }

        // Simulate payment
        const oldPaymentStatus = order.trangthaithanhtoan;
        order.trangthaithanhtoan = 1; // Paid
        await order.save();

        // Update inventory
        const chiTietDonHangs = await ChiTietDonHang.find({ id_dathang: id_dathang });
        const inventoryUpdates = [];

        for (const chiTiet of chiTietDonHangs) {
            const sanPham = await SanPham.findOne({ masp: chiTiet.masp });
            if (sanPham) {
                const oldQuantity = sanPham.soluongkho;
                sanPham.soluongkho = Math.max(0, sanPham.soluongkho - chiTiet.soluong);
                await sanPham.save();
                
                inventoryUpdates.push({
                    masp: sanPham.masp,
                    tensp: sanPham.tensp,
                    oldQuantity: oldQuantity,
                    newQuantity: sanPham.soluongkho,
                    change: chiTiet.soluong
                });
            }
        }

        // Emit real-time update
        const io = req.app.get('socketio');
        if (io) {
            emitToAll(io, 'test_payment_processed', {
                id_dathang: id_dathang,
                inventoryUpdates: inventoryUpdates,
                timestamp: new Date()
            });
        }

        res.json({
            success: true,
            message: 'Payment simulation completed',
            order: order,
            inventoryUpdates: inventoryUpdates
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: 'Error simulating payment',
            error: error.message
        });
    }
});

module.exports = router;
