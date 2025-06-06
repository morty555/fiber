// vue.config.js
module.exports = {
  

  devServer: {
    allowedHosts:'*',
   
  },
  configureWebpack: {
    resolve: {
      fallback: {
        fs: false,  // 不使用 fs
        path: require.resolve("path-browserify"),
        crypto: require.resolve("crypto-browserify"),
         stream: require.resolve('stream-browserify'),
      },
    },
  },
}


