const { emitToUserType } = require('./socketEmitter');

// Helper function to check and emit low stock alerts
const checkAndEmitLowStockAlert = async (io, masp, tensp, soluongkho, threshold = 5) => {
    try {
        if (soluongkho <= threshold) {
            const alertLevel = soluongkho === 0 ? 'out_of_stock' : 'low_stock';
            
            emitToUserType(io, 'admin', 'inventory_alert', {
                type: alertLevel,
                product: {
                    masp: masp,
                    tensp: tensp,
                    soluongkho: soluongkho
                },
                message: soluongkho === 0 ? 
                    `Sản phẩm "${tensp}" đã hết hàng!` : 
                    `Sản phẩm "${tensp}" sắp hết hàng (còn ${soluongkho})`,
                timestamp: new Date()
            });
            
            console.log(`🚨 Low stock alert sent for product ${masp}: ${tensp} (${soluongkho} remaining)`);
        }
    } catch (error) {
        console.error('Error checking low stock alert:', error);
    }
};

module.exports = {
    checkAndEmitLowStockAlert
};
