const mongoose = require('mongoose');
const DatHang = require('../models/DatHang');
const ChiTietDonHang = require('../models/ChiTietDonHang');
const SanPham = require('../models/SanPham');
const InventoryHistory = require('../models/InventoryHistory');
const { emitToAll, emitToUserType } = require('../utils/socketEmitter');
const { checkAndEmitLowStockAlert } = require('../utils/inventoryUtils');

// Helper function to log inventory changes
const logInventoryChange = async (masp, tensp, previousQuantity, newQuantity, changeType, reason, orderId = null, adminUser = null) => {
    try {
        const inventoryHistory = new InventoryHistory({
            masp: masp,
            tensp: tensp,
            previous_quantity: previousQuantity,
            new_quantity: newQuantity,
            change_amount: newQuantity - previousQuantity,
            change_type: changeType,
            reason: reason,
            order_id: orderId,
            admin_user: adminUser
        });
        
        await inventoryHistory.save();
        console.log(`📝 Logged inventory change for product ${masp}: ${previousQuantity} → ${newQuantity} (${reason})`);
    } catch (error) {
        console.error('Error logging inventory change:', error);
    }
};

// Helper function to update sold quantity
const updateSoldQuantity = async (masp, changeAmount, reason) => {
    try {
        const sanPham = await SanPham.findOne({ masp: masp });
        if (sanPham) {
            const oldSoldQuantity = sanPham.soLuongDaBan || 0;
            sanPham.soLuongDaBan = Math.max(0, oldSoldQuantity + changeAmount);
            await sanPham.save();
            console.log(`📈 Updated sold quantity for product ${masp}: ${oldSoldQuantity} -> ${sanPham.soLuongDaBan} (${reason})`);
        }
    } catch (error) {
        console.error('Error updating sold quantity:', error);
    }
};

// Helper function to update inventory when payment status changes
const updateInventoryForPaymentChange = async (id_dathang, oldPaymentStatus, newPaymentStatus, io) => {
    try {
        // Lấy thông tin đơn hàng để kiểm tra trạng thái
        const orderInfo = await DatHang.findOne({ id_dathang: id_dathang });
        if (!orderInfo) {
            console.log(`Không tìm thấy đơn hàng ${id_dathang}`);
            return;
        }
        
        const orderStatus = orderInfo.trangthai; // 0=Đã đặt, 1=Đang chuyển, 2=Thành công, 3=Hoàn hàng
        console.log(`Đơn hàng ${id_dathang} có trạng thái: ${orderStatus}, thanh toán: ${oldPaymentStatus} -> ${newPaymentStatus}`);
        
        // Lấy tất cả chi tiết đơn hàng
        const chiTietDonHangs = await ChiTietDonHang.find({ id_dathang: id_dathang });
        
        for (const chiTiet of chiTietDonHangs) {
            const sanPham = await SanPham.findOne({ masp: chiTiet.masp });
            if (sanPham) {
                const oldQuantity = sanPham.soluongkho;
                let updated = false;
                let reason = '';
                
                if (oldPaymentStatus === 0 && newPaymentStatus === 1) {
                    // Chưa thanh toán -> Đã thanh toán
                    // ĐIỀU KIỆN MỚI: Chỉ trừ kho nếu đơn hàng CHƯA HOÀN (trangthai !== 3)
                    if (orderStatus !== 3) {
                        sanPham.soluongkho = Math.max(0, sanPham.soluongkho - chiTiet.soluong);
                        await updateSoldQuantity(sanPham.masp, chiTiet.soluong, `Đơn hàng ${id_dathang} đã thanh toán`);
                        updated = true;
                        reason = `Đơn hàng ${id_dathang} được thanh toán - trừ kho`;
                        console.log(`Trừ kho sản phẩm ${sanPham.masp}: ${chiTiet.soluong}, còn lại: ${sanPham.soluongkho}`);
                    } else {
                        console.log(`KHÔNG trừ kho cho đơn hàng ${id_dathang} vì đã hoàn hàng (trạng thái 3)`);
                        reason = `Đơn hàng ${id_dathang} được thanh toán nhưng đã hoàn hàng - không thay đổi kho`;
                    }
                } else if (oldPaymentStatus === 1 && newPaymentStatus === 0) {
                    // Đã thanh toán -> Chưa thanh toán
                    // ĐIỀU KIỆN MỚI: Chỉ cộng lại kho nếu đơn hàng CHƯA HOÀN (trangthai !== 3)
                    if (orderStatus !== 3) {
                        sanPham.soluongkho += chiTiet.soluong;
                        await updateSoldQuantity(sanPham.masp, -chiTiet.soluong, `Đơn hàng ${id_dathang} hủy thanh toán`);
                        updated = true;
                        reason = `Đơn hàng ${id_dathang} hủy thanh toán - hoàn kho`;
                        console.log(`Cộng lại kho sản phẩm ${sanPham.masp}: ${chiTiet.soluong}, tổng: ${sanPham.soluongkho}`);
                    } else {
                        console.log(`KHÔNG cộng kho cho đơn hàng ${id_dathang} vì đã hoàn hàng (trạng thái 3)`);
                        reason = `Đơn hàng ${id_dathang} hủy thanh toán nhưng đã hoàn hàng - không thay đổi kho`;
                    }
                }
                
                if (updated) {
                    await sanPham.save();
                    
                    // Log inventory change
                    await logInventoryChange(
                        sanPham.masp,
                        sanPham.tensp,
                        oldQuantity,
                        sanPham.soluongkho,
                        'payment',
                        reason,
                        id_dathang
                    );
                    
                    // Emit realtime update for inventory change - in payment status change
                    if (io) {
                        emitToAll(io, 'sanpham_inventory_updated', {
                            masp: sanPham.masp,
                            tensp: sanPham.tensp,
                            soluongkho: sanPham.soluongkho,
                            updatedAt: new Date(),
                            reason: reason,
                            id_dathang: id_dathang,
                            changeType: 'payment_status_change'
                        });
                        
                        // Check and emit low stock alert after payment change
                        await checkAndEmitLowStockAlert(io, sanPham.masp, sanPham.tensp, sanPham.soluongkho);
                    }
                }
            }
        }
    } catch (error) {
        console.error('Lỗi khi cập nhật kho cho thay đổi thanh toán:', error);
    }
};

// Helper function to update inventory when order status changes
const updateInventoryForOrderStatusChange = async (id_dathang, oldStatus, newStatus, paymentStatus, io) => {
    try {
        // Lấy tất cả chi tiết đơn hàng
        const chiTietDonHangs = await ChiTietDonHang.find({ id_dathang: id_dathang });
        
        for (const chiTiet of chiTietDonHangs) {
            const sanPham = await SanPham.findOne({ masp: chiTiet.masp });
            if (sanPham) {
                const oldQuantity = sanPham.soluongkho;
                let updated = false;
                let reason = '';
                
                // Nếu đơn hàng bị hủy/hoàn trả (trangthai = 3) và đã thanh toán trước đó
                if (newStatus === 3 && paymentStatus === 1) {
                    // Hoàn lại số lượng vào kho
                    sanPham.soluongkho += chiTiet.soluong;
                    await updateSoldQuantity(sanPham.masp, -chiTiet.soluong, `Đơn hàng ${id_dathang} hoàn trả`);
                    updated = true;
                    reason = `Đơn hàng ${id_dathang} bị hủy/hoàn trả - hoàn kho`;
                    console.log(`Hoàn lại kho sản phẩm ${sanPham.masp}: ${chiTiet.soluong}, tổng: ${sanPham.soluongkho}`);
                }
                // Nếu đơn hàng từ trạng thái hủy (3) chuyển về trạng thái khác và đã thanh toán
                else if (oldStatus === 3 && newStatus !== 3 && paymentStatus === 1) {
                    // Trừ lại số lượng từ kho
                    sanPham.soluongkho = Math.max(0, sanPham.soluongkho - chiTiet.soluong);
                    await updateSoldQuantity(sanPham.masp, chiTiet.soluong, `Đơn hàng ${id_dathang} khôi phục từ hoàn trả`);
                    updated = true;
                    reason = `Đơn hàng ${id_dathang} khôi phục từ trạng thái hủy - trừ kho`;
                    console.log(`Trừ lại kho sản phẩm ${sanPham.masp}: ${chiTiet.soluong}, còn lại: ${sanPham.soluongkho}`);
                }
                
                if (updated) {
                    await sanPham.save();
                    
                    // Log inventory change
                    await logInventoryChange(
                        sanPham.masp,
                        sanPham.tensp,
                        oldQuantity,
                        sanPham.soluongkho,
                        'order_status',
                        reason,
                        id_dathang
                    );
                    
                    // Emit realtime update for inventory change - in order status change
                    if (io) {
                        emitToAll(io, 'sanpham_inventory_updated', {
                            masp: sanPham.masp,
                            tensp: sanPham.tensp,
                            soluongkho: sanPham.soluongkho,
                            updatedAt: new Date(),
                            reason: reason,
                            id_dathang: id_dathang,
                            changeType: 'order_status_change'
                        });
                        
                        // Check and emit low stock alert after order status change
                        await checkAndEmitLowStockAlert(io, sanPham.masp, sanPham.tensp, sanPham.soluongkho);
                    }
                }
            }
        }
    } catch (error) {
        console.error('Lỗi khi cập nhật kho cho thay đổi trạng thái đơn hàng:', error);
    }
};

// @desc    Test database connection
// @route   GET /api/dathang/debug
// @access  Public
exports.debugDatHang = async (req, res) => {
    try {
        console.log('Debug endpoint called');
        const totalCount = await DatHang.countDocuments();
        console.log('Total orders in DB:', totalCount);
        
        const allOrders = await DatHang.find().limit(5);
        console.log('Sample orders:', allOrders);
        
        res.json({
            success: true,
            totalCount,
            sampleOrders: allOrders,
            message: 'Debug info retrieved successfully'
        });
    } catch (err) {
        console.error('Debug error:', err);
        res.status(500).json({ 
            success: false, 
            error: err.message,
            message: 'Debug failed' 
        });
    }
};

// @desc    Get all DatHang
// @route   GET /api/dathang
// @access  Private/Admin
exports.getAllDatHang = async (req, res) => {
    try {
        console.log('getAllDatHang called with query:', req.query);
        console.log('User:', req.user);
        
        const trangthai = req.query.trangthai;
        let query = {};
        if (trangthai) {
            query.trangthai = parseInt(trangthai);
        }
        
        console.log('MongoDB query:', query);
        // Sort by ngaycapnhat first (most recently updated), then by ngaydathang (newest first)
        const orders = await DatHang.find(query).sort({ ngaycapnhat: -1, ngaydathang: -1 });
        console.log('Found orders:', orders.length);
        
        // Trả về format mà Android app mong đợi
        res.json({
            donhang: orders
        });
    } catch (err) {
        console.error('Error in getAllDatHang:', err.message);
        res.status(500).json({ msg: 'Server Error: ' + err.message });
    }
};

// @desc    Get logged in user's orders
// @route   GET /api/dathang/myorders
// @access  Private
exports.getMyOrders = async (req, res) => {
    try {
        const orders = await DatHang.find({ user: req.user.id });
        res.json(orders);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};
// @desc    Get orders by customer name
// @route   GET /api/dathang/khachhang/:tenkh
// @access  Public
exports.getDatHangByKhachHang = async (req, res) => {
    try {
        const { tenkh } = req.params;
        const { trangthai } = req.query;        let query = { tenkh: tenkh };
        if (trangthai) {
            query.trangthai = trangthai;
        }
        
        // Sort by ngaycapnhat first (most recently updated), then by ngaydathang (newest first)
        const orders = await DatHang.find(query).sort({ ngaycapnhat: -1, ngaydathang: -1 });
        res.json(orders);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};


// @desc    Get DatHang by id
// @route   GET /api/dathang/:id
// @access  Private
exports.getDatHangById = async (req, res) => {
    try {
        // Validate ObjectId format
        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({ msg: 'Invalid order ID format' });
        }

        const order = await DatHang.findById(req.params.id);
        if (!order) {
            return res.status(404).json({ msg: 'Order not found' });
        }
        // Add logic to check if user is admin or owner of the order
        res.json(order);
    } catch (err) {
        console.error('Error in getDatHangById:', err.message);
        res.status(500).json({ msg: 'Server Error: ' + err.message });
    }
};

// @desc    Create a DatHang
// @route   POST /api/dathang
// @access  Private
exports.createDatHang = async (req, res) => {
    const { id_dathang, tenkh, diachi, sdt, tongthanhtoan, trangthai, trangthaithanhtoan, phuongthucthanhtoan, chiTietDonHangs } = req.body;

    try {
        const newDatHang = new DatHang({
            user: req.user.id,
            id_dathang,
            tenkh,
            diachi,
            sdt,
            tongthanhtoan,
            trangthai,
            trangthaithanhtoan: trangthaithanhtoan || 0, // Default: chưa thanh toán
            phuongthucthanhtoan: phuongthucthanhtoan || 0, // Default: tiền mặt
            ngaycapnhat: new Date() // Set ngày cập nhật khi tạo đơn hàng mới
        });

        const datHang = await newDatHang.save();

        if (chiTietDonHangs && chiTietDonHangs.length > 0) {
            const chiTietToSave = chiTietDonHangs.map(item => {
                // Xử lý ảnh - chuyển đổi từ base64 string hoặc array thành Buffer
                let anhBuffer = null;
                if (item.anh) {
                    if (typeof item.anh === 'string') {
                        // Nếu là base64 string
                        anhBuffer = Buffer.from(item.anh, 'base64');
                    } else if (Array.isArray(item.anh)) {
                        // Nếu là byte array
                        anhBuffer = Buffer.from(item.anh);
                    } else if (Buffer.isBuffer(item.anh)) {
                        // Nếu đã là Buffer
                        anhBuffer = item.anh;
                    }
                }
                
                return {
                    ...item,
                    id_dathang: datHang.id_dathang,
                    anh: anhBuffer
                };
            });
            
            await ChiTietDonHang.insertMany(chiTietToSave);
            console.log(`Saved ${chiTietToSave.length} order details with images`);
        }

        res.status(201).json(datHang);
        
        // Emit real-time update to all connected clients with image info
        const io = req.app.get('socketio');
        
        // Get first product image for real-time event
        let orderWithImage = { ...datHang.toObject() };
        try {
            if (chiTietDonHangs && chiTietDonHangs.length > 0) {
                // Get the saved chi tiet don hang with image
                const savedChiTiet = await ChiTietDonHang.findOne({ 
                    id_dathang: datHang.id_dathang 
                }).lean();
                
                if (savedChiTiet && savedChiTiet.anh) {
                    orderWithImage.firstProductImage = {
                        data: savedChiTiet.anh
                    };
                } else {
                    // Fallback to product image if no image in chi tiet
                    const firstDetail = chiTietDonHangs[0];
                    if (firstDetail.masp) {
                        const SanPham = require('../models/SanPham');
                        const sanPham = await SanPham.findOne({ masp: firstDetail.masp }).lean();
                        
                        if (sanPham && sanPham.anh) {
                            orderWithImage.firstProductImage = {
                                data: sanPham.anh
                            };
                        }
                    }
                }
            }
        } catch (err) {
            console.error('Error getting image for real-time event:', err);
            // Continue without image if error
        }
        
        emitToAll(io, 'dathang_created', orderWithImage);
    } catch (err) {
        console.error('Error creating order:', err.message);
        console.error('Full error:', err);
        res.status(500).json({ 
            success: false, 
            msg: 'Server Error: ' + err.message 
        });
    }
};

// @desc    Update a DatHang to paid
// @route   PUT /api/dathang/:id/pay
// @access  Private/Admin
exports.updateDatHangToPaid = async (req, res) => {
    try {
        // Validate ObjectId format
        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({ msg: 'Invalid order ID format' });
        }

        let order = await DatHang.findById(req.params.id);
        if (!order) return res.status(404).json({ msg: 'Order not found' });

        // Assuming trangthai: 1 means pending, 2 means paid
        order.trangthai = 2; // Or whatever status code for 'paid' is
        await order.save();
        res.json(order);
    } catch (err) {
        console.error('Error in updateDatHangToPaid:', err.message);
        res.status(500).json({ msg: 'Server Error: ' + err.message });
    }
};

// @desc    Update a DatHang to delivered
// @route   PUT /api/dathang/:id/deliver
// @access  Private/Admin
exports.updateDatHangToDelivered = async (req, res) => {
    try {
        // Validate ObjectId format
        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({ msg: 'Invalid order ID format' });
        }

        let order = await DatHang.findById(req.params.id);
        if (!order) return res.status(404).json({ msg: 'Order not found' });

        // Assuming trangthai: 3 means delivered
        order.trangthai = 3; // Or whatever status code for 'delivered' is
        await order.save();
        res.json(order);
    } catch (err) {
        console.error('Error in updateDatHangToDelivered:', err.message);
        res.status(500).json({ msg: 'Server Error: ' + err.message });
    }
};

// @desc    Update DatHang status
// @route   PUT /api/dathang/:id
// @access  Private/Admin
exports.updateDatHangStatus = async (req, res) => {
    try {
        const { trangthai } = req.body;
        
        // Validate trangthai
        if (trangthai === undefined || trangthai === null) {
            return res.status(400).json({
                success: false,
                msg: 'Trạng thái không được để trống'
            });
        }
        
        // Validate trangthai values (0: Đã đặt hàng, 1: Đang vận chuyển, 2: Vận chuyển thành công, 3: Hoàn hàng)
        if (![0, 1, 2, 3].includes(parseInt(trangthai))) {
            return res.status(400).json({
                success: false,
                msg: 'Trạng thái không hợp lệ'
            });
        }
        
        const order = await DatHang.findOne({ id_dathang: req.params.id });
        
        if (!order) {
            return res.status(404).json({
                success: false,
                msg: 'Không tìm thấy đơn hàng'
            });
        }
        
        const oldStatus = order.trangthai;
        const newStatus = parseInt(trangthai);
        
        // Update status and update timestamp
        order.trangthai = newStatus;
        order.ngaycapnhat = new Date();
        const updatedOrder = await order.save();
        
        // Cập nhật số lượng kho khi hủy đơn hàng (trangthai = 3)
        if (oldStatus !== newStatus) {
            const io = req.app.get('socketio');
            await updateInventoryForOrderStatusChange(order.id_dathang, oldStatus, newStatus, order.trangthaithanhtoan, io);
        }
        
        let statusText = '';
        switch (newStatus) {
            case 0: statusText = 'Đã đặt hàng'; break;
            case 1: statusText = 'Đang vận chuyển'; break;
            case 2: statusText = 'Vận chuyển thành công'; break;
            case 3: statusText = 'Hoàn hàng'; break;
        }
        
        res.json({
            success: true,
            msg: `Cập nhật trạng thái thành công: ${statusText}`,
            data: updatedOrder
        });
        
        // Emit real-time update to all connected clients
        const io = req.app.get('socketio');
        if (io) {
            // Emit main order status event
            emitToAll(io, 'dathang_status_updated', {
                id_dathang: updatedOrder.id_dathang,
                trangthai: updatedOrder.trangthai,
                statusText: statusText
            });
            
            // Also emit chi tiet don hang status event for detailed views
            emitToAll(io, 'chitietdonhang_status_updated', {
                id_dathang: updatedOrder.id_dathang,
                trangthai: updatedOrder.trangthai,
                statusText: statusText,
                tenkh: updatedOrder.tenkh || 'Khách hàng',
                timestamp: new Date()
            });
            
            console.log(`✅ Emitted both dathang_status_updated and chitietdonhang_status_updated for order ${updatedOrder.id_dathang}`);
        }
        
    } catch (err) {
        console.error('Error updating order status:', err);
        res.status(500).json({
            success: false,
            msg: 'Lỗi server khi cập nhật trạng thái',
            error: err.message
        });
    }
};

// @desc    Update DatHang payment status
// @route   PUT /api/dathang/:id/payment
// @access  Private
exports.updateDatHangPaymentStatus = async (req, res) => {
    try {
        const { trangthaithanhtoan } = req.body;
        
        // Validate trangthaithanhtoan
        if (trangthaithanhtoan === undefined || trangthaithanhtoan === null) {
            return res.status(400).json({
                success: false,
                msg: 'Trạng thái thanh toán không được để trống'
            });
        }
        
        // Validate trangthaithanhtoan values (0: Chưa thanh toán, 1: Đã thanh toán)
        if (![0, 1].includes(parseInt(trangthaithanhtoan))) {
            return res.status(400).json({
                success: false,
                msg: 'Trạng thái thanh toán không hợp lệ'
            });
        }
        
        const order = await DatHang.findOne({ id_dathang: req.params.id });
        
        if (!order) {
            return res.status(404).json({
                success: false,
                msg: 'Không tìm thấy đơn hàng'
            });
        }
        
        const oldPaymentStatus = order.trangthaithanhtoan;
        const newPaymentStatus = parseInt(trangthaithanhtoan);
        
        // Update payment status and update timestamp
        order.trangthaithanhtoan = newPaymentStatus;
        order.ngaycapnhat = new Date();
        const updatedOrder = await order.save();
        
        // Cập nhật số lượng kho khi thanh toán thành công hoặc hoàn thanh toán
        if (oldPaymentStatus !== newPaymentStatus) {
            const io = req.app.get('socketio');
            await updateInventoryForPaymentChange(order.id_dathang, oldPaymentStatus, newPaymentStatus, io);
        }
        
        let paymentStatusText = newPaymentStatus === 0 ? 'Chưa thanh toán' : 'Đã thanh toán';
        
        res.json({
            success: true,
            msg: `Cập nhật trạng thái thanh toán thành công: ${paymentStatusText}`,
            data: updatedOrder
        });
        
        // Emit real-time update with slight delay to ensure database consistency
        const io = req.app.get('socketio');
        if (io) {
            // Add small delay to ensure database transaction is fully committed
            setTimeout(() => {
                emitToAll(io, 'dathang_payment_status_updated', {
                    id_dathang: updatedOrder.id_dathang,
                    trangthaithanhtoan: updatedOrder.trangthaithanhtoan,
                    paymentStatusText: paymentStatusText
                });
                console.log(`✅ Emitted payment status update event for order ${updatedOrder.id_dathang}: ${paymentStatusText}`);
            }, 100); // 100ms delay to ensure DB consistency
        }
        
    } catch (err) {
        console.error('Error updating payment status:', err);
        res.status(500).json({
            success: false,
            msg: 'Lỗi server khi cập nhật trạng thái thanh toán'
        });
    }
};

// @desc    Update DatHang payment method
// @route   PUT /api/dathang/:id/payment-method
// @access  Private
exports.updateDatHangPaymentMethod = async (req, res) => {
    try {
        const { phuongthucthanhtoan } = req.body;
        
        // Validate phuongthucthanhtoan
        if (phuongthucthanhtoan === undefined || phuongthucthanhtoan === null) {
            return res.status(400).json({
                success: false,
                msg: 'Phương thức thanh toán không được để trống'
            });
        }
        
        // Validate phuongthucthanhtoan values (0: Tiền mặt, 1: Chuyển khoản)
        if (![0, 1].includes(parseInt(phuongthucthanhtoan))) {
            return res.status(400).json({
                success: false,
                msg: 'Phương thức thanh toán không hợp lệ'
            });
        }
        
        const order = await DatHang.findOne({ id_dathang: req.params.id });
        
        if (!order) {
            return res.status(404).json({
                success: false,
                msg: 'Không tìm thấy đơn hàng'
            });
        }
        
        const oldPaymentMethod = order.phuongthucthanhtoan;
        const newPaymentMethod = parseInt(phuongthucthanhtoan);
        console.log(`🔄 Payment method change: Order ${req.params.id} - ${oldPaymentMethod} → ${newPaymentMethod}`);
        console.log(`📊 Change direction: ${oldPaymentMethod === 0 ? 'Tiền mặt' : 'Chuyển khoản'} → ${newPaymentMethod === 0 ? 'Tiền mặt' : 'Chuyển khoản'}`);
        
        // Update payment method and update timestamp
        order.phuongthucthanhtoan = newPaymentMethod;
        order.ngaycapnhat = new Date();
        const updatedOrder = await order.save();
        
        let paymentMethodText = parseInt(phuongthucthanhtoan) === 0 ? 'Tiền mặt' : 'Chuyển khoản';
        
        res.json({
            success: true,
            msg: `Cập nhật phương thức thanh toán thành công: ${paymentMethodText}`,
            data: updatedOrder
        });

        // Emit real-time update to notify admin when user updates payment method
        const io = req.app.get('socketio');
        console.log(`🔍 Socket.io instance available: ${!!io}`);
        
        if (io) {
            const eventData = {
                id_dathang: updatedOrder.id_dathang,
                phuongthucthanhtoan: updatedOrder.phuongthucthanhtoan,
                paymentMethodText: paymentMethodText,
                tenkh: updatedOrder.tenkh || 'Khách hàng',
                msg: `Khách hàng ${updatedOrder.tenkh || 'không rõ'} đã cập nhật phương thức thanh toán đơn hàng ${updatedOrder.id_dathang} thành: ${paymentMethodText}`
            };
            
            console.log('🔔 About to emit payment method update event:', JSON.stringify(eventData, null, 2));
            
            // Emit to admin specifically for notifications
            console.log('📡 Emitting to admin room...');
            emitToUserType(io, 'admin', 'dathang_payment_method_updated', eventData);
            
            // Also emit to all clients (admin + user) for realtime data updates
            console.log('📡 Emitting to all clients...');
            emitToAll(io, 'dathang_payment_method_updated', eventData);
            
            // Additionally emit a generic order update event to ensure all admin pages refresh
            console.log('📡 Emitting generic order update...');
            emitToUserType(io, 'admin', 'dathang_updated', {
                id_dathang: updatedOrder.id_dathang,
                type: 'payment_method_change',
                ...eventData
            });
            
            // Force emit with slight delay to ensure delivery
            setTimeout(() => {
                console.log('📡 Force emitting payment method update (delayed)...');
                emitToAll(io, 'dathang_payment_method_updated', eventData);
                emitToUserType(io, 'admin', 'dathang_updated', eventData);
            }, 100);
            
            console.log('✅ Payment method update event emitted successfully to all clients');
        } else {
            console.error('❌ Socket.io instance not found');
        }
        
    } catch (err) {
        console.error('Error updating payment method:', err);
        res.status(500).json({
            success: false,
            msg: 'Lỗi server khi cập nhật phương thức thanh toán'
        });
    }
};

// @desc    Get all DatHang with product images
// @route   GET /api/dathang/with-images
// @access  Private/Admin
exports.getAllDatHangWithImages = async (req, res) => {
    try {
        console.log('getAllDatHangWithImages called with query:', req.query);
        
        const trangthai = req.query.trangthai;
        let query = {};
        if (trangthai) {
            query.trangthai = parseInt(trangthai);
        }
        
        console.log('MongoDB query:', query);
        // Sort by ngaycapnhat first (most recently updated), then by ngaydathang (newest first)
        const orders = await DatHang.find(query).sort({ ngaycapnhat: -1, ngaydathang: -1 });
        console.log('Found orders:', orders.length);
        
        // Lấy ảnh sản phẩm đầu tiên cho mỗi đơn hàng
        const ordersWithImages = await Promise.all(orders.map(async (order) => {
            try {
                // Lấy chi tiết đơn hàng đầu tiên
                const chiTiet = await ChiTietDonHang.findOne({ 
                    id_dathang: order.id_dathang 
                }).lean();
                
                let productImage = null;
                if (chiTiet) {
                    // Lấy thông tin sản phẩm và ảnh
                    const sanPham = await SanPham.findOne({ 
                        masp: chiTiet.masp 
                    }).lean();
                    
                    if (sanPham && sanPham.anh) {
                        productImage = {
                            data: sanPham.anh
                        };
                    }
                }
                
                return {
                    ...order.toObject(),
                    firstProductImage: productImage
                };
            } catch (err) {
                console.error('Error getting image for order:', order.id_dathang, err);
                return {
                    ...order.toObject(),
                    firstProductImage: null
                };
            }
        }));
        
        // Trả về format mà Android app mong đợi
        res.json({
            donhang: ordersWithImages
        });
    } catch (err) {
        console.error('Error in getAllDatHangWithImages:', err.message);
        res.status(500).json({ msg: 'Server Error: ' + err.message });
    }
};

// @desc    Get orders by customer name with product images
// @route   GET /api/dathang/khachhang/:tenkh/with-images
// @access  Public
exports.getDatHangByKhachHangWithImages = async (req, res) => {
    try {
        const { tenkh } = req.params;
        const { trangthai } = req.query;

        let query = { tenkh: tenkh };
        if (trangthai) {
            query.trangthai = trangthai;
        }
        
        // Sort by ngaycapnhat first (most recently updated), then by ngaydathang (newest first)
        const orders = await DatHang.find(query).sort({ ngaycapnhat: -1, ngaydathang: -1 });
        
        // Lấy ảnh sản phẩm đầu tiên cho mỗi đơn hàng
        const ordersWithImages = await Promise.all(orders.map(async (order) => {
            try {
                // Lấy chi tiết đơn hàng đầu tiên
                const chiTiet = await ChiTietDonHang.findOne({ 
                    id_dathang: order.id_dathang 
                }).lean();
                
                let productImage = null;
                if (chiTiet) {
                    // Lấy thông tin sản phẩm và ảnh
                    const sanPham = await SanPham.findOne({ 
                        masp: chiTiet.masp 
                    }).lean();
                    
                    if (sanPham && sanPham.anh) {
                        productImage = {
                            data: sanPham.anh
                        };
                    }
                }
                
                return {
                    ...order.toObject(),
                    firstProductImage: productImage
                };
            } catch (err) {
                console.error('Error getting image for order:', order.id_dathang, err);
                return {
                    ...order.toObject(),
                    firstProductImage: null
                };
            }
        }));
        
        res.json(ordersWithImages);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};