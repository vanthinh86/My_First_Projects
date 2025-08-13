package com.example.appbanquanao;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SocketManager {
    private static final String TAG = "SocketManager";
    private static final String SOCKET_URL = "http://10.0.2.2:5000";
    
    private static SocketManager instance;
    private Socket socket;
    private boolean isConnected = false;
    private List<SocketEventListener> listeners = new ArrayList<>();
    
    // Listener interface for real-time updates
    public interface SocketEventListener {
        void onSanPhamCreated(JSONObject data);
        void onSanPhamUpdated(JSONObject data);
        void onSanPhamDeleted(JSONObject data);
        void onSanPhamInventoryUpdated(JSONObject data);
        void onNhomSanPhamCreated(JSONObject data);
        void onNhomSanPhamUpdated(JSONObject data);
        void onNhomSanPhamDeleted(JSONObject data);
        void onDatHangCreated(JSONObject data);
        void onDatHangStatusUpdated(JSONObject data);
        void onDatHangPaymentMethodUpdated(JSONObject data);
        void onDatHangPaymentStatusUpdated(JSONObject data);
        // Chi Tiết Đơn Hàng events
        void onChiTietDonHangFetched(JSONObject data);
        void onChiTietDonHangCreated(JSONObject data);
        void onChiTietDonHangUpdated(JSONObject data);
        void onChiTietDonHangStatusUpdated(JSONObject data);
        void onChiTietDonHangDeleted(JSONObject data);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }
    
    private SocketManager() {}
    
    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }
    
    public void initialize(Context context) {
        try {
            IO.Options options = new IO.Options();
            options.timeout = 20000;
            options.reconnection = true;
            options.reconnectionAttempts = 5;
            options.reconnectionDelay = 1000;
            
            socket = IO.socket(SOCKET_URL, options);
            
            setupSocketListeners(context);
            
        } catch (URISyntaxException e) {
            Log.e(TAG, "Failed to initialize socket", e);
        }
    }
    
    private void setupSocketListeners(Context context) {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "Socket connected");
                isConnected = true;
                
                // Join appropriate room based on user type
                SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                String userType = prefs.getString("userType", "user"); // default to user
                String userId = prefs.getString("userId", "");
                
                Log.d(TAG, "User type from SharedPreferences: " + userType);
                Log.d(TAG, "User ID from SharedPreferences: " + userId);
                
                joinRoom(userType, userId);
                
                notifyListeners(listener -> listener.onConnected());
            }
        });
        
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "Socket disconnected");
                isConnected = false;
                notifyListeners(listener -> listener.onDisconnected());
            }
        });
        
        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "Socket connection error: " + args[0]);
                notifyListeners(listener -> listener.onError(args[0].toString()));
            }
        });
        
        // Product events
        socket.on("sanpham_created", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Product created: " + data.toString());
                    notifyListeners(listener -> listener.onSanPhamCreated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling sanpham_created", e);
                }
            }
        });
        
        socket.on("sanpham_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Product updated: " + data.toString());
                    notifyListeners(listener -> listener.onSanPhamUpdated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling sanpham_updated", e);
                }
            }
        });

        socket.on("sanpham_inventory_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Product inventory updated: " + data.toString());
                    notifyListeners(listener -> listener.onSanPhamInventoryUpdated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling sanpham_inventory_updated", e);
                }
            }
        });
        
        socket.on("sanpham_deleted", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Product deleted: " + data.toString());
                    notifyListeners(listener -> listener.onSanPhamDeleted(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling sanpham_deleted", e);
                }
            }
        });
        
        // Category events
        socket.on("nhomsanpham_created", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Category created: " + data.toString());
                    notifyListeners(listener -> listener.onNhomSanPhamCreated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling nhomsanpham_created", e);
                }
            }
        });
        
        socket.on("nhomsanpham_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Category updated: " + data.toString());
                    notifyListeners(listener -> listener.onNhomSanPhamUpdated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling nhomsanpham_updated", e);
                }
            }
        });
        
        socket.on("nhomsanpham_deleted", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Category deleted: " + data.toString());
                    notifyListeners(listener -> listener.onNhomSanPhamDeleted(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling nhomsanpham_deleted", e);
                }
            }
        });
        
        // Order events
        socket.on("dathang_created", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Order created: " + data.toString());
                    notifyListeners(listener -> listener.onDatHangCreated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling dathang_created", e);
                }
            }
        });
        
        socket.on("dathang_status_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Order status updated: " + data.toString());
                    notifyListeners(listener -> listener.onDatHangStatusUpdated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling dathang_status_updated", e);
                }
            }
        });

        socket.on("dathang_payment_method_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Payment method updated: " + data.toString());
                    notifyListeners(listener -> listener.onDatHangPaymentMethodUpdated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling dathang_payment_method_updated", e);
                }
            }
        });

        socket.on("dathang_payment_status_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Payment status updated: " + data.toString());
                    notifyListeners(listener -> listener.onDatHangPaymentStatusUpdated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling dathang_payment_status_updated", e);
                }
            }
        });

        socket.on("dathang_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Order updated (generic): " + data.toString());
                    notifyListeners(listener -> listener.onDatHangStatusUpdated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling dathang_updated", e);
                }
            }
        });

        // Chi Tiết Đơn Hàng events
        socket.on("chitietdonhang_fetched", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Chi tiết đơn hàng fetched: " + data.toString());
                    notifyListeners(listener -> listener.onChiTietDonHangFetched(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling chitietdonhang_fetched", e);
                }
            }
        });

        socket.on("chitietdonhang_created", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Chi tiết đơn hàng created: " + data.toString());
                    notifyListeners(listener -> listener.onChiTietDonHangCreated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling chitietdonhang_created", e);
                }
            }
        });

        socket.on("chitietdonhang_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Chi tiết đơn hàng updated: " + data.toString());
                    notifyListeners(listener -> listener.onChiTietDonHangUpdated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling chitietdonhang_updated", e);
                }
            }
        });

        socket.on("chitietdonhang_status_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Chi tiết đơn hàng status updated: " + data.toString());
                    notifyListeners(listener -> listener.onChiTietDonHangStatusUpdated(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling chitietdonhang_status_updated", e);
                }
            }
        });

        socket.on("chitietdonhang_deleted", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Chi tiết đơn hàng deleted: " + data.toString());
                    notifyListeners(listener -> listener.onChiTietDonHangDeleted(data));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling chitietdonhang_deleted", e);
                }
            }
        });
        
        // Listen for join confirmation
        socket.on("joined", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String room = data.getString("room");
                    String userId = data.getString("userId");
                    Log.d(TAG, "Successfully joined room: " + room + " with userId: " + userId);
                } catch (Exception e) {
                    Log.e(TAG, "Error handling joined event", e);
                }
            }
        });
    }
    
    private void joinRoom(String userType, String userId) {
        try {
            JSONObject data = new JSONObject();
            data.put("userType", userType);
            data.put("userId", userId);
            socket.emit("join", data);
            Log.d(TAG, "Joined room: " + userType + " with userId: " + userId);
        } catch (JSONException e) {
            Log.e(TAG, "Error joining room", e);
        }
    }
    
    public void connect() {
        if (socket != null && !isConnected) {
            socket.connect();
        }
    }
    
    public void disconnect() {
        if (socket != null && isConnected) {
            socket.disconnect();
        }
    }
    
    public void addListener(SocketEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(SocketEventListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners(ListenerAction action) {
        for (SocketEventListener listener : listeners) {
            try {
                action.execute(listener);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }
    
    private interface ListenerAction {
        void execute(SocketEventListener listener);
    }
    
    public boolean isConnected() {
        return isConnected;
    }
}
