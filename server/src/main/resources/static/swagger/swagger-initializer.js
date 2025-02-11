window.onload = function () {
  //<editor-fold desc="Changeable Configuration Block">

  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  window.ui = SwaggerUIBundle({
    url: "/swagger/openapi3.yaml",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout",
    requestInterceptor: function (req) {
      const token = sessionStorage.getItem("accessToken");
      if (token) {
        req.headers["Authorization"] = "Bearer " + token;
      }
      return req;
    },

    responseInterceptor: function (res) {
      if (res.url.includes('/v1/auth/login') && res.body?.data) {
        sessionStorage.setItem('accessToken', res.body.data.accessToken);
      }
      return res;
    }
  });

  //</editor-fold>
};
