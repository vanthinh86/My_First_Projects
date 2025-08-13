const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const TaiKhoan = require('../models/TaiKhoan');
const crypto = require('crypto');
const { emitToAll } = require('../utils/socketEmitter');

exports.register = async (req, res) => {
    const { tendn, matkhau, quyen } = req.body;

    try {
        let user = await TaiKhoan.findOne({ tendn });

        if (user) {
            return res.status(400).json({ msg: 'User already exists' });
        }

        user = new TaiKhoan({
            tendn,
            matkhau,
            quyen
        });

        await user.save();

        // Emit real-time update for new user registration (admin notification)
        const io = req.app.get('socketio');
        if (io) {
            emitToAll(io, 'taikhoan_created', {
                tendn: user.tendn,
                quyen: user.quyen
            });
        }

        const payload = {
            user: {
                id: user.id,
                quyen: user.quyen
            }
        };

        jwt.sign(
            payload,
            process.env.JWT_SECRET,
            { expiresIn: '365d' },
            (err, token) => {
                if (err) throw err;
                res.json({
                    msg: 'TaiKhoan created successfully',
                    token,
                    quyen: user.quyen,
                    tendn: user.tendn
                });
            }
        );
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ msg: 'Server error' });
    }
};

exports.login = async (req, res) => {
    const { tendn, matkhau } = req.body;

    try {
        let user = await TaiKhoan.findOne({ tendn });

        if (!user) {
            return res.status(400).json({ msg: 'Invalid Credentials' });
        }

        const isMatch = await user.comparePassword(matkhau);

        if (!isMatch) {
            return res.status(400).json({ msg: 'Invalid Credentials' });
        }

        const payload = {
            user: {
                id: user.id,
                quyen: user.quyen
            }
        };

        jwt.sign(
            payload,
            process.env.JWT_SECRET,
            { expiresIn: '365d' },
            (err, token) => {
                if (err) throw err;
                res.json({
                    token,
                    quyen: user.quyen,
                    tendn: user.tendn
                });
            }
        );
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ msg: 'Server error' });
    }
};

exports.changePassword = async (req, res) => {
    const { tendn, matkhau } = req.body;

    try {
        let user = await TaiKhoan.findOne({ tendn });

        if (!user) {
            return res.status(400).json({ msg: 'User not found' });
        }

        user.matkhau = matkhau;
        await user.save();

        res.json({ msg: 'Password updated successfully' });
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ msg: 'Server error' });
    }
};

exports.getUsers = async (req, res) => {
    try {
        const users = await TaiKhoan.find().select('-matkhau');
        res.json(users);
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ msg: 'Server error' });
    }
};

exports.getRoles = (req, res) => {
    res.json(['admin', 'user']);
};
exports.updateUser = async (req, res) => {
    const { tendn, matkhau, quyen } = req.body;
    const oldTendn = req.params.tendn;

    try {
        let user = await TaiKhoan.findOne({ tendn: oldTendn });

        if (!user) {
            return res.status(404).json({ msg: 'User not found' });
        }

        user.tendn = tendn;
        user.matkhau = matkhau;
        user.quyen = quyen;

        await user.save();
        res.json(user);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

exports.deleteUser = async (req, res) => {
    try {
        const user = await TaiKhoan.findOneAndDelete({ tendn: req.params.tendn });

        if (!user) {
            return res.status(404).json({ msg: 'User not found' });
        }

        res.json({ msg: 'TaiKhoan removed' });
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};