const SanPham = require('../models/SanPham');
const InventoryHistory = require('../models/InventoryHistory');
const { emitToAll, emitToUserType } = require('../utils/socketEmitter');
const { checkAndEmitLowStockAlert } = require('../utils/inventoryUtils');

// Helper function to emit inventory updates
const emitInventoryUpdate = (io, sanPham) => {
    if (io) {
        emitToAll(io, 'sanpham_inventory_updated', {
            masp: sanPham.masp,
            tensp: sanPham.tensp,
            soluongkho: sanPham.soluongkho,
            updatedAt: new Date()
        });
    }
};

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

// @desc    Get all SanPham
// @route   GET /api/sanpham
// @access  Public
const getAllSanPham = async (req, res) => {
    try {
        let sortOptions = { createdAt: -1 }; // Default sort: newest first

        // Check for sort query parameter
        if (req.query.sort === 'sales') {
            sortOptions = { soLuongDaBan: -1, tensp: 1 }; // Sort by sales and then name
        }

        const sanPhams = await SanPham.find().populate({
            path: 'maso',
            select: 'tennsp',
            model: 'NhomSanPham',
            foreignField: 'maso'
        }).sort(sortOptions);
        
        const sanPhamsWithBase64Image = sanPhams.map(sp => {
            const spObject = sp.toObject();
            if (spObject.anh) {
                spObject.anh = spObject.anh.toString('base64');
            }
            return spObject;
        });
        res.json(sanPhamsWithBase64Image);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

// @desc    Get SanPham by id
// @route   GET /api/sanpham/:id
// @access  Public
const getSanPhamById = async (req, res) => {
    try {
        const sanPham = await SanPham.findOne({ masp: req.params.id }).populate({
            path: 'maso',
            select: 'tennsp',
            model: 'NhomSanPham',
            foreignField: 'maso'
        });
        if (!sanPham) {
            return res.status(404).json({ msg: 'SanPham not found' });
        }
        const spObject = sanPham.toObject();
        if (spObject.anh) {
            spObject.anh = spObject.anh.toString('base64');
        }
        res.json(spObject);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

// @desc    Get SanPham by NhomSanPham id
// @route   GET /api/sanpham/nhom/:id
// @access  Public
const getSanPhamByNhomId = async (req, res) => {
  try {
    const sanPhams = await SanPham.find({ maso: req.params.id }).sort({ createdAt: -1 }); // Sắp xếp theo thời gian tạo mới nhất trước
    if (sanPhams) {
        const sanPhamsWithBase64Image = sanPhams.map(sp => {
            const spObject = sp.toObject();
            if (spObject.anh) {
                spObject.anh = spObject.anh.toString('base64');
            }
            return spObject;
        });
      res.json(sanPhamsWithBase64Image);
    } else {
      res.status(404).json({ message: 'Không có sản phẩm nào trong nhóm này' });
    }
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Create a SanPham
// @route   POST /api/sanpham
// @access  Private/Admin
const createSanPham = async (req, res) => {
    const { masp, tensp, dongia, mota, ghichu, soluongkho, maso, anh } = req.body;

    // Validate required fields
    if (!tensp || tensp.trim() === '') {
        return res.status(400).json({ message: 'Tên sản phẩm không được để trống' });
    }
    if (!dongia || dongia <= 0) {
        return res.status(400).json({ message: 'Đơn giá không được để trống và phải lớn hơn 0' });
    }
    if (!maso) {
        return res.status(400).json({ message: 'Mã nhóm sản phẩm không được để trống' });
    }
    if (soluongkho === undefined || soluongkho === null || soluongkho < 0) {
        return res.status(400).json({ message: 'Số lượng kho không được để trống và phải lớn hơn hoặc bằng 0' });
    }

    try {
        // Handle data URI or object.data
        let imgData = null;
        if (anh) {
            if (anh.data) imgData = anh.data;
            else if (typeof anh === 'string') imgData = anh;
        }
        if (imgData && imgData.includes(',')) imgData = imgData.split(',')[1];
        const imageBuffer = imgData ? Buffer.from(imgData, 'base64') : null;

        if (!imageBuffer) {
            return res.status(400).json({ message: 'Vui lòng cung cấp ảnh' });
        }

        const newSanPham = new SanPham({
            masp,
            tensp: tensp.trim(),
            dongia,
            mota: mota ? mota.trim() : '',
            ghichu: ghichu ? ghichu.trim() : '',
            soluongkho,
            maso,
            anh: imageBuffer
        });

        const sanPham = await newSanPham.save();
        // Convert to Base64 for response, similar to nhomSanPham
        const spObj = sanPham.toObject();
        if (spObj.anh) spObj.anh = spObj.anh.toString('base64');
        
        // Emit real-time update to all connected clients
        const io = req.app.get('socketio');
        emitToAll(io, 'sanpham_created', spObj);
        
        res.json(spObj);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

// @desc    Update a SanPham
// @route   PUT /api/sanpham/:id
// @access  Private/Admin
const updateSanPham = async (req, res) => {
    const { tensp, dongia, mota, ghichu, soluongkho, maso, anh } = req.body;

    // Validate required fields - không cho phép trường trống
    if (!tensp || tensp.trim() === '') {
        return res.status(400).json({ msg: 'Tên sản phẩm không được để trống' });
    }
    if (!dongia || dongia <= 0) {
        return res.status(400).json({ msg: 'Đơn giá không được để trống và phải lớn hơn 0' });
    }
    if (!maso) {
        return res.status(400).json({ msg: 'Mã nhóm sản phẩm không được để trống' });
    }
    if (soluongkho === undefined || soluongkho === null || soluongkho < 0) {
        return res.status(400).json({ msg: 'Số lượng kho không được để trống và phải lớn hơn hoặc bằng 0' });
    }

    // Build SanPham object
    const sanPhamFields = {};
    sanPhamFields.tensp = tensp.trim();
    sanPhamFields.dongia = dongia;
    if (mota) sanPhamFields.mota = mota.trim();
    if (ghichu) sanPhamFields.ghichu = ghichu.trim();
    sanPhamFields.soluongkho = soluongkho;
    sanPhamFields.maso = maso;
    
    // Handle image update similar to nhomSanPham
    if (anh && anh.data) {
        const imageBuffer = Buffer.from(anh.data, 'base64');
        sanPhamFields.anh = imageBuffer;
    }

    try {
        let sanPham = await SanPham.findOne({ masp: req.params.id });

        if (!sanPham) return res.status(404).json({ msg: 'SanPham not found' });

        const oldQuantity = sanPham.soluongkho;

        sanPham = await SanPham.findOneAndUpdate(
            { masp: req.params.id },
            { $set: sanPhamFields },
            { new: true }
        );

        // Convert image buffer to Base64 before sending, similar to nhomSanPham
        const spObject = sanPham.toObject();
        if (spObject.anh) {
            spObject.anh = spObject.anh.toString('base64');
        }
        
        // Emit real-time update to all connected clients
        const io = req.app.get('socketio');
        emitToAll(io, 'sanpham_updated', spObject);
        
        // Emit inventory update
        emitInventoryUpdate(io, spObject);
        
        // Log inventory change if quantity changed
        if (oldQuantity !== spObject.soluongkho) {
            logInventoryChange(spObject.masp, spObject.tensp, oldQuantity, spObject.soluongkho, 'update', 'Cập nhật sản phẩm');
        }
        
        res.json(spObject);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

// @desc    Delete a SanPham
// @route   DELETE /api/sanpham/:id
// @access  Private/Admin
const deleteSanPham = async (req, res) => {
    try {
        let sanPham = await SanPham.findOne({ masp: req.params.id });

        if (!sanPham) return res.status(404).json({ msg: 'SanPham not found' });

        // Import ChiTietDonHang model to check for existing orders
        const ChiTietDonHang = require('../models/ChiTietDonHang');
        
        // Check if there are any order details (orders) containing this product
        const orderCount = await ChiTietDonHang.countDocuments({ masp: parseInt(req.params.id) });

        if (orderCount > 0) {
            return res.status(400).json({ msg: 'Không thể xóa sản phẩm đã có trong đơn hàng' });
        }

        await SanPham.findOneAndDelete({ masp: req.params.id });

        // Emit real-time update to all connected clients
        const io = req.app.get('socketio');
        emitToAll(io, 'sanpham_deleted', { masp: req.params.id });

        res.json({ msg: 'SanPham removed' });
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

// @desc    Search SanPham by name
// @route   GET /api/sanpham/search/:query
// @access  Public
const searchSanPham = async (req, res) => {
    try {
        const query = req.params.query;
        console.log('Search query:', query);
        
        // Tìm kiếm không phân biệt chữ hoa chữ thường và sắp xếp theo thời gian tạo mới nhất trước
        const sanPhams = await SanPham.find({
            tensp: { $regex: query, $options: 'i' }
        }).populate({
            path: 'maso',
            select: 'maso tennsp',
            model: 'NhomSanPham',
            foreignField: 'maso'
        }).sort({ createdAt: -1 }); // Sắp xếp theo thời gian tạo mới nhất trước
        
        console.log(`Found ${sanPhams.length} products for query: ${query}`);
        
        const sanPhamsWithBase64Image = sanPhams.map(sp => {
            const spObject = sp.toObject();
            if (spObject.anh) {
                spObject.anh = spObject.anh.toString('base64');
            }
            return spObject;
        });
        
        res.json(sanPhamsWithBase64Image);
    } catch (err) {
        console.error('Search error:', err.message);
        res.status(500).send('Server Error');
    }
};

// @desc    Get latest SanPham with limit for homepage
// @route   GET /api/sanpham/latest/:limit
// @access  Public
const getLatestSanPham = async (req, res) => {
    try {
        const limit = parseInt(req.params.limit) || 10; // Mặc định lấy 10 sản phẩm mới nhất
        const sanPhams = await SanPham.find().populate({
            path: 'maso',
            select: 'tennsp',
            model: 'NhomSanPham',
            foreignField: 'maso'
        }).sort({ createdAt: -1 }).limit(limit); // Sắp xếp theo thời gian tạo mới nhất trước và giới hạn số lượng
        
        const sanPhamsWithBase64Image = sanPhams.map(sp => {
            const spObject = sp.toObject();
            if (spObject.anh) {
                spObject.anh = spObject.anh.toString('base64');
            }
            return spObject;
        });
        res.json(sanPhamsWithBase64Image);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

// @desc    Update product inventory directly
// @route   PUT /api/sanpham/:id/inventory
// @access  Private/Admin
const updateSanPhamInventory = async (req, res) => {
    const { soluongkho } = req.body;
    
    // Validate required fields
    if (soluongkho === undefined || soluongkho === null || soluongkho < 0) {
        return res.status(400).json({ 
            success: false,
            message: 'Số lượng kho không được để trống và phải lớn hơn hoặc bằng 0' 
        });
    }

    try {
        const sanPham = await SanPham.findOne({ masp: req.params.id });
        
        if (!sanPham) {
            return res.status(404).json({ 
                success: false,
                message: 'Không tìm thấy sản phẩm' 
            });
        }

        const oldQuantity = sanPham.soluongkho;
        sanPham.soluongkho = parseInt(soluongkho);
        
        await sanPham.save();

        // Log inventory change
        await logInventoryChange(
            sanPham.masp,
            sanPham.tensp,
            oldQuantity,
            sanPham.soluongkho,
            'manual',
            'Cập nhật trực tiếp bởi admin',
            null,
            req.user ? req.user.id : null
        );

        res.json({
            success: true,
            message: `Cập nhật số lượng kho thành công từ ${oldQuantity} thành ${sanPham.soluongkho}`,
            data: {
                masp: sanPham.masp,
                tensp: sanPham.tensp,
                soluongkho: sanPham.soluongkho,
                oldQuantity: oldQuantity
            }
        });

        // Emit real-time update for inventory change
        const io = req.app ? req.app.get('socketio') : null;
        if (io) {
            emitToAll(io, 'sanpham_inventory_updated', {
                masp: sanPham.masp,
                tensp: sanPham.tensp,
                soluongkho: sanPham.soluongkho,
                oldQuantity: oldQuantity,
                updatedAt: new Date(),
                reason: 'Cập nhật trực tiếp bởi admin'
            });
            
            // Check and emit low stock alert after manual inventory update
            await checkAndEmitLowStockAlert(io, sanPham.masp, sanPham.tensp, sanPham.soluongkho);
        }

    } catch (err) {
        console.error(err.message);
        res.status(500).json({ 
            success: false,
            message: 'Lỗi server khi cập nhật số lượng kho',
            error: err.message 
        });
    }
};

// @desc    Get inventory status for all products
// @route   GET /api/sanpham/inventory/status
// @access  Private/Admin
const getInventoryStatus = async (req, res) => {
    try {
        const { low_stock } = req.query;
        let query = {};
        
        // Nếu có tham số low_stock, chỉ lấy sản phẩm có số lượng thấp
        if (low_stock === 'true') {
            const threshold = parseInt(req.query.threshold) || 5; // Mặc định ngưỡng là 5
            query.soluongkho = { $lte: threshold };
        }
        
        const sanPhams = await SanPham.find(query)
            .select('masp tensp soluongkho dongia maso')
            .populate({
                path: 'maso',
                select: 'tennsp',
                model: 'NhomSanPham',
                foreignField: 'maso'
            })
            .sort({ soluongkho: 1 }); // Sắp xếp theo số lượng tăng dần
        
        const inventoryStats = {
            totalProducts: await SanPham.countDocuments(),
            outOfStock: await SanPham.countDocuments({ soluongkho: 0 }),
            lowStock: await SanPham.countDocuments({ soluongkho: { $lte: 5, $gt: 0 } }),
            inStock: await SanPham.countDocuments({ soluongkho: { $gt: 5 } })
        };

        res.json({
            success: true,
            stats: inventoryStats,
            products: sanPhams
        });
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ 
            success: false,
            message: 'Lỗi server khi lấy thông tin kho',
            error: err.message 
        });
    }
};

// @desc    Get inventory history
// @route   GET /api/sanpham/inventory/history
// @access  Private/Admin
const getInventoryHistory = async (req, res) => {
    try {
        const { masp, limit = 50, page = 1 } = req.query;
        let query = {};
        
        if (masp) {
            query.masp = parseInt(masp);
        }
        
        const skip = (parseInt(page) - 1) * parseInt(limit);
        
        const history = await InventoryHistory.find(query)
            .sort({ timestamp: -1 })
            .limit(parseInt(limit))
            .skip(skip)
            .populate('admin_user', 'email');
        
        const total = await InventoryHistory.countDocuments(query);
        
        res.json({
            success: true,
            data: history,
            pagination: {
                current_page: parseInt(page),
                total_pages: Math.ceil(total / parseInt(limit)),
                total_records: total,
                per_page: parseInt(limit)
            }
        });
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ 
            success: false,
            message: 'Lỗi server khi lấy lịch sử kho',
            error: err.message 
        });
    }
};

// @desc    Check and notify low stock products
// @route   GET /api/sanpham/inventory/low-stock-alert
// @access  Private/Admin
const checkLowStockAlert = async (req, res) => {
    try {
        const threshold = parseInt(req.query.threshold) || 5;
        
        const lowStockProducts = await SanPham.find({ 
            soluongkho: { $lte: threshold, $gte: 0 } 
        })
        .select('masp tensp soluongkho dongia maso')
        .populate({
            path: 'maso',
            select: 'tennsp',
            model: 'NhomSanPham',
            foreignField: 'maso'
        })
        .sort({ soluongkho: 1 });

        const outOfStockProducts = await SanPham.find({ soluongkho: 0 })
        .select('masp tensp soluongkho dongia maso')
        .populate({
            path: 'maso',
            select: 'tennsp',
            model: 'NhomSanPham',
            foreignField: 'maso'
        });

        const alerts = {
            lowStock: lowStockProducts.filter(p => p.soluongkho > 0),
            outOfStock: outOfStockProducts,
            totalLowStock: lowStockProducts.filter(p => p.soluongkho > 0).length,
            totalOutOfStock: outOfStockProducts.length
        };

        res.json({
            success: true,
            threshold: threshold,
            alerts: alerts
        });

        // Emit real-time alert to admin if there are low stock items
        const io = req.app ? req.app.get('socketio') : null;
        if (io && (alerts.totalLowStock > 0 || alerts.totalOutOfStock > 0)) {
            emitToUserType(io, 'admin', 'inventory_low_stock_alert', {
                alerts: alerts,
                timestamp: new Date()
            });
        }

    } catch (err) {
        console.error(err.message);
        res.status(500).json({ 
            success: false,
            message: 'Lỗi server khi kiểm tra hàng tồn kho',
            error: err.message 
        });
    }
};

module.exports = {
  getAllSanPham,
  getSanPhamById,
  getSanPhamByNhomId,
  createSanPham,
  updateSanPham,
  deleteSanPham,
  searchSanPham,
  getLatestSanPham,
  updateSanPhamInventory,
  getInventoryStatus,
  getInventoryHistory, // Export the new controller
  checkLowStockAlert // Export the low stock alert controller
};