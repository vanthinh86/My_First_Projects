const NhomSanPham = require('../models/NhomSanPham');
const SanPham = require('../models/SanPham');
const mongoose = require('mongoose');
const { emitToAll } = require('../utils/socketEmitter');

// @desc    Get all NhomSanPham
// @route   GET /api/nhomsanpham
// @access  Public
exports.getAllNhomSanPham = async (req, res) => {
    try {
        const nhomSanPhams = await NhomSanPham.find().sort({ createdAt: -1 }); // Sắp xếp theo thời gian tạo mới nhất trước
        const transformedNhomSanPhams = nhomSanPhams.map(nsp => {
            const nspObject = nsp.toObject();
            if (nspObject.anh) {
                nspObject.anh = nspObject.anh.toString('base64');
            }
            return nspObject;
        });
        res.json(transformedNhomSanPhams);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

// @desc    Get NhomSanPham by id
// @route   GET /api/nhomsanpham/:id
// @access  Public
exports.getNhomSanPhamById = async (req, res) => {
    try {
        const query = mongoose.Types.ObjectId.isValid(req.params.id)
            ? { _id: req.params.id }
            : { maso: parseInt(req.params.id, 10) };

        const nhomSanPham = await NhomSanPham.findOne(query);

        if (!nhomSanPham) {
            return res.status(404).json({ msg: 'NhomSanPham not found' });
        }
        const nspObject = nhomSanPham.toObject();
        if (nspObject.anh) {
            nspObject.anh = nspObject.anh.toString('base64');
        }
        res.json(nspObject);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

// @desc    Create a NhomSanPham
// @route   POST /api/nhomsanpham
// @access  Private/Admin
exports.createNhomSanPham = async (req, res) => {
    const { tennsp, anh } = req.body;

    if (!tennsp || !anh || !anh.data) {
        return res.status(400).json({ message: 'Vui lòng điền đầy đủ thông tin' });
    }

    try {
        // Tìm maso lớn nhất hiện có
        const lastNhomSanPham = await NhomSanPham.findOne().sort({ maso: -1 });
        const newMaso = lastNhomSanPham ? lastNhomSanPham.maso + 1 : 1;

        // Chuyển đổi base64 thành Buffer
        const imageBuffer = Buffer.from(anh.data, 'base64');

        const newNhomSanPham = new NhomSanPham({
            maso: newMaso,
            tennsp,
            anh: imageBuffer
        });

        const nhomSanPham = await newNhomSanPham.save();
        
        // Convert image buffer to Base64 for real-time update
        const nhomSanPhamObj = nhomSanPham.toObject();
        if (nhomSanPhamObj.anh) {
            nhomSanPhamObj.anh = nhomSanPhamObj.anh.toString('base64');
        }
        
        // Emit real-time update to all connected clients
        const io = req.app.get('socketio');
        emitToAll(io, 'nhomsanpham_created', nhomSanPhamObj);
        
        res.status(201).json({ message: "Thêm nhóm sản phẩm thành công", nhomSanPham: nhomSanPhamObj });
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

// @desc    Update a NhomSanPham
// @route   PUT /api/nhomsanpham/:id
// @access  Private/Admin
exports.updateNhomSanPham = async (req, res) => {
    const { maso, tennsp, anh } = req.body;

    // Validate required fields - không cho phép trường trống
    if (!tennsp || tennsp.trim() === '') {
        return res.status(400).json({ msg: 'Tên nhóm sản phẩm không được để trống' });
    }

    // Build NhomSanPham object
    const nhomSanPhamFields = {};
    if (maso) nhomSanPhamFields.maso = maso;
    nhomSanPhamFields.tennsp = tennsp.trim();
    if (anh && anh.data) {
        const imageBuffer = Buffer.from(anh.data, 'base64');
        nhomSanPhamFields.anh = imageBuffer;
    }

    try {
        const query = mongoose.Types.ObjectId.isValid(req.params.id)
            ? { _id: req.params.id }
            : { maso: parseInt(req.params.id, 10) };

        const updatedNhomSanPham = await NhomSanPham.findOneAndUpdate(
            query,
            { $set: nhomSanPhamFields },
            { new: true }
        );

        if (!updatedNhomSanPham) {
            return res.status(404).json({ msg: 'NhomSanPham not found' });
        }

        // Convert image buffer to Base64 for real-time update
        const nhomSanPhamObj = updatedNhomSanPham.toObject();
        if (nhomSanPhamObj.anh) {
            nhomSanPhamObj.anh = nhomSanPhamObj.anh.toString('base64');
        }
        
        // Emit real-time update to all connected clients
        const io = req.app.get('socketio');
        emitToAll(io, 'nhomsanpham_updated', nhomSanPhamObj);

        res.json(nhomSanPhamObj);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};


// @desc    Delete a NhomSanPham
// @route   DELETE /api/nhomsanpham/:id
// @access  Private/Admin
exports.deleteNhomSanPham = async (req, res) => {
    try {
        const query = mongoose.Types.ObjectId.isValid(req.params.id)
            ? { _id: req.params.id }
            : { maso: parseInt(req.params.id, 10) };

        // Find the group to get its maso
        const nhomSanPhamToDelete = await NhomSanPham.findOne(query);

        if (!nhomSanPhamToDelete) {
            return res.status(404).json({ msg: 'NhomSanPham not found' });
        }

        // Check if there are any products in this group
        const productCount = await SanPham.countDocuments({ maso: nhomSanPhamToDelete.maso });

        if (productCount > 0) {
            return res.status(400).json({ msg: 'Không thể xóa nhóm sản phẩm đã có sản phẩm' });
        }

        // If no products, proceed with deletion
        await NhomSanPham.findOneAndDelete(query);

        // Emit real-time update to all connected clients
        const io = req.app.get('socketio');
        emitToAll(io, 'nhomsanpham_deleted', { maso: nhomSanPhamToDelete.maso });

        res.json({ msg: 'NhomSanPham removed' });
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};