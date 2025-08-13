const ChiTietDonHang = require('../models/ChiTietDonHang');
const SanPham = require('../models/SanPham');
const DatHang = require('../models/DatHang');
const { emitToAll, emitToUserType } = require('../utils/socketEmitter');

// @desc    Get all ChiTietDonHang for a specific order
// @route   GET /api/chitietdonhang/:orderId
// @access  Private
exports.getChiTietDonHangByOrderId = async (req, res) => {
    try {
        const orderIdParam = req.params.orderId;
        console.log(`Fetching chi tiet don hang for order ID: ${orderIdParam}`);
        console.log(`Original order ID from params: ${orderIdParam}`);
        
        // Try both string and number searches to handle large negative numbers
        let orderId;
        if (orderIdParam.includes('-') && orderIdParam.length > 8) {
            // For large negative numbers, keep as string first
            console.log(`Trying to find with string ID: ${orderIdParam}`);
            orderId = orderIdParam;
        } else {
            orderId = parseInt(orderIdParam);
            if (isNaN(orderId)) {
                console.log(`Invalid Order ID: ${orderIdParam}`);
                return res.status(400).json({ success: false, msg: 'Invalid Order ID' });
            }
        }

        // Try multiple query approaches for better compatibility
        let chiTietDonHangs = [];
        
        // First try: exact numeric match
        if (typeof orderId === 'number') {
            chiTietDonHangs = await ChiTietDonHang.find({ id_dathang: orderId }).lean();
            console.log(`Found ${chiTietDonHangs ? chiTietDonHangs.length : 0} chi tiet don hang items with numeric ID`);
        }
        
        // Second try: string match if numeric failed
        if (chiTietDonHangs.length === 0) {
            chiTietDonHangs = await ChiTietDonHang.find({ id_dathang: orderIdParam }).lean();
            console.log(`Found ${chiTietDonHangs ? chiTietDonHangs.length : 0} chi tiet don hang items with string ID`);
        }
        
        // Third try: convert string to number for large numbers
        if (chiTietDonHangs.length === 0 && typeof orderId === 'string') {
            const numericId = parseInt(orderIdParam);
            if (!isNaN(numericId)) {
                chiTietDonHangs = await ChiTietDonHang.find({ id_dathang: numericId }).lean();
                console.log(`Found ${chiTietDonHangs ? chiTietDonHangs.length : 0} chi tiet don hang items with converted numeric ID`);
            }
        }

        if (!chiTietDonHangs || chiTietDonHangs.length === 0) {
            console.log(`No chi tiet don hang found for order ID: ${orderIdParam}`);
            
            // Debug: Check if the order exists at all
            const orderExists = await DatHang.findOne({ 
                $or: [
                    { id_dathang: parseInt(orderIdParam) },
                    { id_dathang: orderIdParam },
                    { _id: orderIdParam }
                ]
            });
            
            console.log(`Order exists in DatHang collection: ${!!orderExists}`);
            if (orderExists) {
                console.log('Order details:', { 
                    id_dathang: orderExists.id_dathang, 
                    tenkh: orderExists.tenkh,
                    _id: orderExists._id 
                });
            }
            
            return res.status(200).json({
                success: false,
                msg: `Không tìm thấy chi tiết đơn hàng cho order ID: ${orderIdParam}`,
                data: [],
                debug: {
                    orderExists: !!orderExists,
                    searchedIds: [parseInt(orderIdParam), orderIdParam],
                    orderDetails: orderExists ? { 
                        id_dathang: orderExists.id_dathang, 
                        tenkh: orderExists.tenkh 
                    } : null
                }
            });
        }

        const sanPhamIds = [...new Set(chiTietDonHangs.map(item => item.masp))];
        console.log(`Looking up products with IDs: ${sanPhamIds.join(', ')}`);
        
        const sanPhams = await SanPham.find({ masp: { $in: sanPhamIds } }).lean();
        console.log(`Found ${sanPhams ? sanPhams.length : 0} products`);
        
        // Debug: log first product's image info
        if (sanPhams && sanPhams.length > 0) {
            const firstProduct = sanPhams[0];
            const imageSize = firstProduct.anh ? (typeof firstProduct.anh.length === 'function' ? firstProduct.anh.length() : firstProduct.anh.length) : 0;
            console.log(`First product (${firstProduct.masp}) has image: ${!!firstProduct.anh}, image size: ${imageSize} bytes`);
        }
        
        const sanPhamMap = sanPhams.reduce((map, product) => {
            map[product.masp] = product;
            return map;
        }, {});

        // Lấy thông tin đơn hàng để có trạng thái chính xác
        const orderInfo = await DatHang.findOne({ 
            $or: [
                { id_dathang: parseInt(orderIdParam) },
                { id_dathang: orderIdParam }
            ]
        }).lean();
        
        const orderTrangThai = orderInfo ? orderInfo.trangthai : 0;
        const orderPaymentStatus = orderInfo ? orderInfo.trangthaithanhtoan : 0;
        const orderPaymentMethod = orderInfo ? orderInfo.phuongthucthanhtoan : 0;
        console.log(`Order status from DatHang: ${orderTrangThai}, Payment status: ${orderPaymentStatus}, Payment method: ${orderPaymentMethod}`);

        const formattedChiTiet = chiTietDonHangs.map(item => {
            const product = sanPhamMap[item.masp];
            
            // Xử lý ảnh an toàn - ưu tiên ảnh từ sản phẩm gốc
            let anhData = null;
            
            // Trước tiên, thử sử dụng ảnh từ sản phẩm gốc
            const hasImage = product && product.anh && (typeof product.anh.length === 'function' ? product.anh.length() > 0 : product.anh.length > 0);
            if (hasImage) {
                try {
                    anhData = { data: product.anh.toString('base64') };
                    console.log(`Using product image for item ${item.id_chitiet}`);
                } catch (error) {
                    console.error(`Error converting product image to base64 for item ${item.id_chitiet}:`, error);
                    anhData = null;
                }
            }
            
            // Nếu không có ảnh sản phẩm, thử sử dụng ảnh từ chi tiết đơn hàng
            if (!anhData && item.anh && item.anh.length > 0) {
                try {
                    anhData = { data: item.anh.toString('base64') };
                    console.log(`Using order detail image for item ${item.id_chitiet}`);
                } catch (error) {
                    console.error(`Error converting order detail image to base64 for item ${item.id_chitiet}:`, error);
                    anhData = { data: '' };
                }
            }
            
            // Nếu không có ảnh nào, sử dụng chuỗi rỗng
            if (!anhData) {
                anhData = { data: '' };
                console.log(`No image available for item ${item.id_chitiet}`);
            }
            
            return {
                id_chitiet: item.id_chitiet,
                id_dathang: item.id_dathang,
                masp: item.masp,
                tensp: product ? product.tensp : 'Sản phẩm không xác định',
                soluong: item.soluong,
                dongia: item.dongia,
                anh: anhData,
                trangthai: orderTrangThai, // Sử dụng trạng thái từ đơn hàng chính
            };
        });

        console.log(`Returning ${formattedChiTiet.length} formatted chi tiet items`);
        
        // Emit realtime event when chi tiet don hang is fetched
        const io = req.app.get('socketio');
        if (io) {
            emitToAll(io, 'chitietdonhang_fetched', {
                orderId: orderIdParam,
                items: formattedChiTiet,
                orderInfo: {
                    id_dathang: orderInfo ? orderInfo.id_dathang : null,
                    trangthai: orderTrangThai,
                    trangthaithanhtoan: orderPaymentStatus,
                    phuongthucthanhtoan: orderPaymentMethod,
                    tenkh: orderInfo ? orderInfo.tenkh : null
                }
            });
        }
        
        res.json({
            success: true,
            msg: 'Lấy chi tiết đơn hàng thành công',
            data: formattedChiTiet,
            orderInfo: {
                id_dathang: orderInfo ? orderInfo.id_dathang : null,
                trangthai: orderTrangThai,
                trangthaithanhtoan: orderPaymentStatus,
                phuongthucthanhtoan: orderPaymentMethod,
                tenkh: orderInfo ? orderInfo.tenkh : null,
                diachi: orderInfo ? orderInfo.diachi : null,
                sdt: orderInfo ? orderInfo.sdt : null,
                tongthanhtoan: orderInfo ? orderInfo.tongthanhtoan : null,
                ngaydathang: orderInfo ? orderInfo.ngaydathang : null,
                ngaycapnhat: orderInfo ? orderInfo.ngaycapnhat : null
            }
        });
    } catch (err) {
        console.error('Error in getChiTietDonHangByOrderId:', err.message);
        console.error('Full error:', err);
        res.status(500).json({ success: false, msg: 'Server Error', error: err.message });
    }
};

// @desc    Debug endpoint to check database contents
// @route   GET /api/chitietdonhang/debug/all
// @access  Private/Admin
exports.debugChiTietDonHang = async (req, res) => {
    try {
        console.log('Debug endpoint called for ChiTietDonHang');
        
        const totalChiTiet = await ChiTietDonHang.countDocuments();
        console.log('Total chi tiet don hang in DB:', totalChiTiet);
        
        const sampleChiTiet = await ChiTietDonHang.find().limit(5).lean();
        console.log('Sample chi tiet don hang:', sampleChiTiet);
        
        // Also check DatHang collection
        const totalOrders = await DatHang.countDocuments();
        const sampleOrders = await DatHang.find().limit(5).lean();
        
        res.json({
            success: true,
            totalChiTiet,
            sampleChiTiet: sampleChiTiet.map(item => ({
                id_chitiet: item.id_chitiet,
                id_dathang: item.id_dathang,
                masp: item.masp,
                soluong: item.soluong
            })),
            totalOrders,
            sampleOrders: sampleOrders.map(order => ({
                id_dathang: order.id_dathang,
                tenkh: order.tenkh,
                _id: order._id
            })),
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

// @desc    Update chi tiet don hang status
// @route   PUT /api/chitietdonhang/status/:id
// @access  Private/Admin
exports.updateChiTietDonHangStatus = async (req, res) => {
    try {
        const { id } = req.params;
        const { trangthai } = req.body;

        console.log(`Updating chi tiet don hang ${id} status to ${trangthai}`);

        // Find the chi tiet don hang item
        const chiTietItem = await ChiTietDonHang.findOne({ id_chitiet: id });
        if (!chiTietItem) {
            return res.status(404).json({
                success: false,
                msg: 'Chi tiết đơn hàng không tìm thấy'
            });
        }

        // Update the main order status
        const updatedOrder = await DatHang.findOneAndUpdate(
            { id_dathang: chiTietItem.id_dathang },
            { trangthai: trangthai },
            { new: true }
        );

        if (!updatedOrder) {
            return res.status(404).json({
                success: false,
                msg: 'Đơn hàng không tìm thấy'
            });
        }

        // Emit realtime event for status update
        const io = req.app.get('socketio');
        if (io) {
            const eventData = {
                id_chitiet: id,
                id_dathang: chiTietItem.id_dathang,
                trangthai: trangthai,
                tenkh: updatedOrder.tenkh,
                timestamp: new Date()
            };
            
            emitToAll(io, 'chitietdonhang_status_updated', eventData);
            console.log(`Emitted chitietdonhang_status_updated event for order ${chiTietItem.id_dathang}`);
        }

        res.json({
            success: true,
            msg: 'Cập nhật trạng thái chi tiết đơn hàng thành công',
            data: {
                id_chitiet: id,
                id_dathang: chiTietItem.id_dathang,
                trangthai: trangthai
            }
        });

    } catch (err) {
        console.error('Error in updateChiTietDonHangStatus:', err.message);
        res.status(500).json({ 
            success: false, 
            msg: 'Server Error', 
            error: err.message 
        });
    }
};

// @desc    Add new chi tiet don hang item
// @route   POST /api/chitietdonhang
// @access  Private
exports.addChiTietDonHang = async (req, res) => {
    try {
        const { id_dathang, masp, soluong, dongia } = req.body;

        console.log(`Adding new chi tiet don hang for order ${id_dathang}`);

        // Check if order exists
        const order = await DatHang.findOne({ id_dathang: id_dathang });
        if (!order) {
            return res.status(404).json({
                success: false,
                msg: 'Đơn hàng không tồn tại'
            });
        }

        // Check if product exists
        const product = await SanPham.findOne({ masp: masp });
        if (!product) {
            return res.status(404).json({
                success: false,
                msg: 'Sản phẩm không tồn tại'
            });
        }

        // Generate new chi tiet ID
        const lastChiTiet = await ChiTietDonHang.findOne().sort({ id_chitiet: -1 });
        const newId = lastChiTiet ? lastChiTiet.id_chitiet + 1 : 1;

        // Create new chi tiet don hang
        const newChiTiet = new ChiTietDonHang({
            id_chitiet: newId,
            id_dathang: id_dathang,
            masp: masp,
            soluong: soluong,
            dongia: dongia
        });

        const savedChiTiet = await newChiTiet.save();

        // Emit realtime event for new chi tiet item
        const io = req.app.get('socketio');
        if (io) {
            const eventData = {
                id_chitiet: savedChiTiet.id_chitiet,
                id_dathang: id_dathang,
                masp: masp,
                tensp: product.tensp,
                soluong: soluong,
                dongia: dongia,
                tenkh: order.tenkh,
                timestamp: new Date()
            };
            
            emitToAll(io, 'chitietdonhang_created', eventData);
            console.log(`Emitted chitietdonhang_created event for order ${id_dathang}`);
        }

        res.status(201).json({
            success: true,
            msg: 'Thêm chi tiết đơn hàng thành công',
            data: savedChiTiet
        });

    } catch (err) {
        console.error('Error in addChiTietDonHang:', err.message);
        res.status(500).json({ 
            success: false, 
            msg: 'Server Error', 
            error: err.message 
        });
    }
};

// @desc    Update chi tiet don hang item
// @route   PUT /api/chitietdonhang/:id
// @access  Private
exports.updateChiTietDonHang = async (req, res) => {
    try {
        const { id } = req.params;
        const { soluong, dongia } = req.body;

        console.log(`Updating chi tiet don hang ${id}`);

        const updatedChiTiet = await ChiTietDonHang.findOneAndUpdate(
            { id_chitiet: parseInt(id) },
            { soluong: soluong, dongia: dongia },
            { new: true }
        );

        if (!updatedChiTiet) {
            return res.status(404).json({
                success: false,
                msg: 'Chi tiết đơn hàng không tìm thấy'
            });
        }

        // Get product and order info for realtime event
        const product = await SanPham.findOne({ masp: updatedChiTiet.masp });
        const order = await DatHang.findOne({ id_dathang: updatedChiTiet.id_dathang });

        // Emit realtime event for chi tiet update
        const io = req.app.get('socketio');
        if (io) {
            const eventData = {
                id_chitiet: updatedChiTiet.id_chitiet,
                id_dathang: updatedChiTiet.id_dathang,
                masp: updatedChiTiet.masp,
                tensp: product ? product.tensp : 'Sản phẩm không xác định',
                soluong: soluong,
                dongia: dongia,
                tenkh: order ? order.tenkh : null,
                timestamp: new Date()
            };
            
            emitToAll(io, 'chitietdonhang_updated', eventData);
            console.log(`Emitted chitietdonhang_updated event for order ${updatedChiTiet.id_dathang}`);
        }

        res.json({
            success: true,
            msg: 'Cập nhật chi tiết đơn hàng thành công',
            data: updatedChiTiet
        });

    } catch (err) {
        console.error('Error in updateChiTietDonHang:', err.message);
        res.status(500).json({ 
            success: false, 
            msg: 'Server Error', 
            error: err.message 
        });
    }
};

// @desc    Delete chi tiet don hang item
// @route   DELETE /api/chitietdonhang/:id
// @access  Private/Admin
exports.deleteChiTietDonHang = async (req, res) => {
    try {
        const { id } = req.params;

        console.log(`Deleting chi tiet don hang ${id}`);

        const chiTietToDelete = await ChiTietDonHang.findOne({ id_chitiet: parseInt(id) });
        if (!chiTietToDelete) {
            return res.status(404).json({
                success: false,
                msg: 'Chi tiết đơn hàng không tìm thấy'
            });
        }

        // Get order info before deletion
        const order = await DatHang.findOne({ id_dathang: chiTietToDelete.id_dathang });

        await ChiTietDonHang.deleteOne({ id_chitiet: parseInt(id) });

        // Emit realtime event for chi tiet deletion
        const io = req.app.get('socketio');
        if (io) {
            const eventData = {
                id_chitiet: parseInt(id),
                id_dathang: chiTietToDelete.id_dathang,
                tenkh: order ? order.tenkh : null,
                timestamp: new Date()
            };
            
            emitToAll(io, 'chitietdonhang_deleted', eventData);
            console.log(`Emitted chitietdonhang_deleted event for order ${chiTietToDelete.id_dathang}`);
        }

        res.json({
            success: true,
            msg: 'Xóa chi tiết đơn hàng thành công'
        });

    } catch (err) {
        console.error('Error in deleteChiTietDonHang:', err.message);
        res.status(500).json({ 
            success: false, 
            msg: 'Server Error', 
            error: err.message 
        });
    }
};