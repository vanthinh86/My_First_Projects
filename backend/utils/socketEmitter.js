// Utility function to emit real-time updates to all connected clients
const emitToAll = (io, event, data) => {
    // Emit to both admin and user rooms
    io.to('admin').emit(event, data);
    io.to('user').emit(event, data);
};

// Emit to specific user type
const emitToUserType = (io, userType, event, data) => {
    console.log(`📡 Emitting '${event}' to '${userType}' room:`, data);
    
    // Get all sockets in the room to check how many clients will receive the event
    const room = io.sockets.adapter.rooms.get(userType);
    const clientCount = room ? room.size : 0;
    console.log(`👥 Number of '${userType}' clients connected: ${clientCount}`);
    
    io.to(userType).emit(event, data);
    console.log(`✅ Event '${event}' emitted to '${userType}' room`);
};

// Emit to all except sender
const emitToAllExceptSender = (io, senderSocketId, event, data) => {
    io.except(senderSocketId).emit(event, data);
};

module.exports = {
    emitToAll,
    emitToUserType,
    emitToAllExceptSender
};
