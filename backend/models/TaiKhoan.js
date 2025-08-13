const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const crypto = require('crypto');

const TaiKhoanSchema = new mongoose.Schema({
    tendn: {
        type: String,
        required: true,
        unique: true
    },
    matkhau: {
        type: String,
        required: true
    },
    quyen: {
        type: String,
        enum: ['admin', 'user'],
        default: 'user'
    },
});

// Hash password before saving
TaiKhoanSchema.pre('save', async function(next) {
    if (!this.isModified('matkhau')) {
        return next();
    }
    const salt = await bcrypt.genSalt(10);
    this.matkhau = await bcrypt.hash(this.matkhau, salt);
    next();
});

// Compare password
TaiKhoanSchema.methods.comparePassword = async function(enteredPassword) {
    return await bcrypt.compare(enteredPassword, this.matkhau);
};

// Remove password from output
TaiKhoanSchema.methods.toJSON = function() {
    const userObject = this.toObject();
    delete userObject.matkhau;
    return userObject;
}

module.exports = mongoose.model('TaiKhoan', TaiKhoanSchema, 'taikhoan');