const PROXY_CONFIG = [
  {
    context: [
      "/**"
    ],
    "target": "http://localhost:8089",
    "secure": false,
    "bypass": function (req, res, proxyOptions) {
      if (req.url.includes("/__admin/webapp")) {
        console.log("Skipping proxy for browser request: " + req.url);
        return "/__admin/webapp/index.html";
      }
      // req.headers["X-Custom-Header"] = "yes";
    }
  }
];

module.exports = PROXY_CONFIG;
