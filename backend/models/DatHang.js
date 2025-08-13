const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const DatHangSchema = new Schema({
    user: {
        type: mongoose.Schema.Types.ObjectId,
        required: true,
        ref: 'TaiKhoan'
    },
    id_dathang: {
        type: Number,
        required: true,
        unique: true
    },
    tenkh: {
        type: String,
        required: true
    },
    diachi: {
        type: String,
        required: true
    },
    sdt: {
        type: String,
        required: true
    },
    tongthanhtoan: {
        type: Number,
        required: true
    },
    ngaydathang: {
        type: Date,
        default: Date.now
    },
    trangthai: {
        type: Number,
        required: true
    },
    trangthaithanhtoan: {
        type: Number,
        required: true,
        default: 0 // 0: chưa thanh toán, 1: đã thanh toán
    },
    phuongthucthanhtoan: {
        type: Number,
        required: true,
        default: 0 // 0: tiền mặt, 1: chuyển khoản
    },
    ngaycapnhat: {
        type: Date,
        default: Date.now
    }
});

module.exports = mongoose.model('DatHang', DatHangSchema, 'Dathang');