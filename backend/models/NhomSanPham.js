const mongoose = require('mongoose');

const NhomSanPhamSchema = new mongoose.Schema({
    maso: {
        type: Number,
        required: true,
        unique: true
    },
    tennsp: {
        type: String,
        required: true
    },
    anh: {
        type: Buffer,
        required: true
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('NhomSanPham', NhomSanPhamSchema, 'nhomsanpham');