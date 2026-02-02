module.exports = {
  "/api/chat/stream": {
    target: "http://localhost:8080",
    secure: false,
    changeOrigin: true,
    onProxyRes: function(proxyRes, req, res) {
      // Disable buffering for SSE
      proxyRes.headers['X-Accel-Buffering'] = 'no';
      proxyRes.headers['Cache-Control'] = 'no-cache';
    }
  },
  "/api": {
    target: "http://localhost:8080",
    secure: false,
    logLevel: "debug",
    changeOrigin: true
  }
};
