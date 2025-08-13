const jwt = require('jsonwebtoken');
const TaiKhoan = require('../models/TaiKhoan');

exports.protect = async (req, res, next) => {
    let token;

    if (
        req.headers.authorization &&
        req.headers.authorization.startsWith('Bearer')
    ) {
        token = req.headers.authorization.split(' ')[1];
    }

    if (!token) {
        return res.status(401).json({ msg: 'Not authorized, no token' });
    }

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.user = await TaiKhoan.findById(decoded.user.id).select('-matkhau');
        next();
    } catch (err) {
        res.status(401).json({ msg: 'Token is not valid' });
    }
};

exports.admin = (req, res, next) => {
    if (req.user && req.user.quyen === 'admin') {
        next();
    } else {
        res.status(403).json({ msg: 'Not authorized as an admin' });
    }
};