const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const ChiTietDonHangSchema = new Schema({
    id_chitiet: {
        type: Number,
        required: true,
        unique: true
    },
    id_dathang: {
        type: Number,
        ref: 'DatHang'
    },
    masp: {
        type: Number,
        ref: 'SanPham'
    },
    soluong: {
        type: Number,
        required: true
    },
    dongia: {
        type: Number,
        required: true
    },
    anh: {
        type: Buffer,
        required: true
    },
    trangthai: {
        type: Number,
        required: true
    }
});

module.exports = mongoose.model('ChiTietDonHang', ChiTietDonHangSchema, 'Chitietdonhang');