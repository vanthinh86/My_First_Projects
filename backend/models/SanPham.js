const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const SanPhamSchema = new Schema({
    masp: {
        type: Number,
        required: true,
        unique: true
    },
    tensp: {
        type: String,
        required: true
    },
    dongia: {
        type: Number,
        required: true
    },
    mota: {
        type: String
    },
    ghichu: {
        type: String
    },
    soluongkho: {
        type: Number,
        default: 0
    },
    soLuongDaBan: {
        type: Number,
        default: 0
    },
    maso: {
        type: Number,
        ref: 'NhomSanPham'
    },
    anh: {
        type: Buffer,
        required: true
    }
}, {
    timestamps: true
});

module.exports = mongoose.models.SanPham || mongoose.model('SanPham', SanPhamSchema, 'sanpham');