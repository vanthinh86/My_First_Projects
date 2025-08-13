const express = require('express');
const router = express.Router();
const { protect, admin } = require('../middleware/auth');
const datHangController = require('../controllers/datHangController');

// @route   GET api/dathang/debug
// @desc    Debug endpoint
// @access  Public
router.get('/debug', datHangController.debugDatHang);

// @route   GET api/dathang/test
// @desc    Test endpoint without auth (temporary)
// @access  Public
router.get('/test', datHangController.getAllDatHang);

// @route   POST api/dathang/test-payment-update
// @desc    Test payment method update emission
// @access  Public
router.post('/test-payment-update', (req, res) => {
    const io = req.app.get('socketio');
    const { emitToAll, emitToUserType } = require('../utils/socketEmitter');
    
    const testEventData = {
        id_dathang: 999,
        phuongthucthanhtoan: 1,
        paymentMethodText: 'Chuyển khoản',
        tenkh: 'Test Customer',
        msg: 'Test payment method update từ tiền mặt sang chuyển khoản'
    };
    
    console.log('🧪 Test emitting payment method update:', testEventData);
    
    if (io) {
        emitToUserType(io, 'admin', 'dathang_payment_method_updated', testEventData);
        emitToAll(io, 'dathang_payment_method_updated', testEventData);
        emitToUserType(io, 'admin', 'dathang_updated', testEventData);
        
        res.json({ success: true, message: 'Test events emitted', data: testEventData });
    } else {
        res.json({ success: false, message: 'Socket.io not available' });
    }
});

// @route   GET api/dathang
// @desc    Get all DatHang
// @access  Private/Admin
router.get('/', protect, admin, datHangController.getAllDatHang);

// @route   GET api/dathang/myorders
// @desc    Get logged in user's orders
// @access  Private
router.get('/myorders', protect, datHangController.getMyOrders);

// @route   GET api/dathang/with-images
// @desc    Get all DatHang with product images
// @access  Private/Admin
router.get('/with-images', protect, admin, datHangController.getAllDatHangWithImages);

// @route   GET api/dathang/khachhang/:tenkh
// @desc    Get orders by customer name
// @access  Public
router.get('/khachhang/:tenkh', datHangController.getDatHangByKhachHang);

// @route   GET api/dathang/khachhang/:tenkh/with-images
// @desc    Get orders by customer name with product images  
// @access  Public
router.get('/khachhang/:tenkh/with-images', datHangController.getDatHangByKhachHangWithImages);

// @route   GET api/dathang/:id
// @desc    Get DatHang by id
// @access  Private
router.get('/:id', protect, datHangController.getDatHangById);

// @route   POST api/dathang
// @desc    Create a DatHang
// @access  Private
router.post('/', protect, datHangController.createDatHang);

// @route   PUT api/dathang/:id
// @desc    Update DatHang status
// @access  Private/Admin
router.put('/:id', protect, admin, datHangController.updateDatHangStatus);

// @route   PUT api/dathang/:id/pay
// @desc    Update a DatHang to paid
// @access  Private/Admin
router.put('/:id/pay', protect, admin, datHangController.updateDatHangToPaid);

// @route   PUT api/dathang/:id/deliver
// @desc    Update a DatHang to delivered
// @access  Private/Admin
router.put('/:id/deliver', protect, admin, datHangController.updateDatHangToDelivered);

// @route   PUT api/dathang/:id/payment
// @desc    Update DatHang payment status
// @access  Private/Admin (for admin to update payment status)
router.put('/:id/payment', protect, admin, datHangController.updateDatHangPaymentStatus);

// @route   PUT api/dathang/:id/payment-method
// @desc    Update DatHang payment method
// @access  Private (for users to update payment method)
router.put('/:id/payment-method', protect, datHangController.updateDatHangPaymentMethod);


module.exports = router;