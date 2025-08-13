const express = require('express');
const router = express.Router();

// Test endpoint to emit payment method update event
router.post('/test-payment-update', (req, res) => {
    try {
        console.log('🧪 Test payment method update endpoint called');
        
        const io = req.app.get('socketio');
        if (!io) {
            return res.status(500).json({ error: 'Socket.io not found' });
        }

        const testData = {
            id_dathang: 'TEST123',
            phuongthucthanhtoan: 1,
            paymentMethodText: 'Chuyển khoản',
            tenkh: 'Test Customer',
            msg: 'Test: Khách hàng Test Customer đã cập nhật phương thức thanh toán đơn hàng TEST123 thành: Chuyển khoản'
        };

        console.log('🔔 Emitting test payment method update:', testData);
        
        // Get room info
        const adminRoom = io.sockets.adapter.rooms.get('admin');
        const adminCount = adminRoom ? adminRoom.size : 0;
        console.log(`👥 Admin clients connected: ${adminCount}`);

        io.to('admin').emit('dathang_payment_method_updated', testData);
        
        res.json({
            success: true,
            message: 'Test event emitted',
            adminClientsCount: adminCount,
            eventData: testData
        });

    } catch (error) {
        console.error('❌ Error in test endpoint:', error);
        res.status(500).json({ error: error.message });
    }
});

// Test endpoint to check socket rooms
router.get('/test-rooms', (req, res) => {
    try {
        const io = req.app.get('socketio');
        if (!io) {
            return res.status(500).json({ error: 'Socket.io not found' });
        }

        const rooms = {};
        const sockets = {};
        
        // Get all rooms
        io.sockets.adapter.rooms.forEach((socketIds, roomName) => {
            rooms[roomName] = {
                clientCount: socketIds.size,
                socketIds: Array.from(socketIds)
            };
        });

        // Get all connected sockets
        io.sockets.sockets.forEach((socket, socketId) => {
            sockets[socketId] = {
                id: socket.id,
                rooms: Array.from(socket.rooms),
                userType: socket.userType || 'unknown',
                userId: socket.userId || 'unknown'
            };
        });

        res.json({
            success: true,
            totalConnections: io.sockets.sockets.size,
            rooms: rooms,
            sockets: sockets
        });

    } catch (error) {
        console.error('❌ Error getting room info:', error);
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;
