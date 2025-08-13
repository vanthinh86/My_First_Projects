require('dotenv').config();
const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const http = require('http');
const socketio = require('socket.io');

const app = express();
const server = http.createServer(app);

// Initialize Socket.io with CORS settings
const io = socketio(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST", "PUT", "DELETE"]
    }
});

// Make io accessible to our routers
app.set('socketio', io);

// Middleware
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

// Serve static files for testing
app.use(express.static('public'));

// Connect to MongoDB
mongoose.connect(process.env.MONGO_URI)
.then(() => console.log('MongoDB connected'))
.catch(err => console.log(err));

app.get('/', (req, res) => {
    res.send('API is running...');
});

// Socket.io connection handling
io.on('connection', (socket) => {
    console.log('Client connected:', socket.id);
    
    // Join room based on user type (admin or user)
    socket.on('join', (data) => {
        const { userType, userId } = data;
        socket.join(userType); // 'admin' or 'user'
        socket.userId = userId;
        socket.userType = userType;
        console.log(`User ${userId} joined room: ${userType}`);
        socket.emit('joined', { room: userType, userId: userId });
    });
    
    // Alternative join event for web clients
    socket.on('join_room', (data) => {
        const { userType, userId } = data;
        const roomName = userType || 'admin'; // default to admin if not specified
        socket.join(roomName);
        socket.userId = userId || 'web_user';
        socket.userType = roomName;
        console.log(`Web user ${socket.userId} joined room: ${roomName}`);
        socket.emit('joined', { room: roomName, userId: socket.userId });
    });
    
    socket.on('disconnect', () => {
        console.log('Client disconnected:', socket.id);
    });
});

// Define Routes
app.use('/api/auth', require('./routes/auth'));
app.use('/api/nhomsanpham', require('./routes/nhomSanPham'));
app.use('/api/sanpham', require('./routes/sanPham'));
app.use('/api/dathang', require('./routes/datHang'));
app.use('/api/chitietdonhang', require('./routes/chiTietDonHang'));
app.use('/api/test', require('./routes/test')); // Test routes for debugging
app.use('/api/inventory-test', require('./routes/inventoryTest')); // Inventory test routes

const PORT = process.env.PORT || 5000;

server.listen(PORT, '0.0.0.0', () => console.log(`Server running on port ${PORT}`));