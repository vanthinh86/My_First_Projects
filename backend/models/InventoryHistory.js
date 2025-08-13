const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const InventoryHistorySchema = new Schema({
    masp: {
        type: Number,
        required: true,
        ref: 'SanPham'
    },
    tensp: {
        type: String,
        required: true
    },
    previous_quantity: {
        type: Number,
        required: true
    },
    new_quantity: {
        type: Number,
        required: true
    },
    change_amount: {
        type: Number,
        required: true
    },
    change_type: {
        type: String,
        enum: ['payment', 'cancel', 'manual', 'order_status', 'update', 'manual_update'],
        required: true
    },
    reason: {
        type: String,
        required: true
    },
    order_id: {
        type: Number,
        ref: 'DatHang'
    },
    admin_user: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'TaiKhoan'
    },
    timestamp: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('InventoryHistory', InventoryHistorySchema, 'inventory_history');
